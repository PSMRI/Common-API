package com.iemr.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration}")
    private long ACCESS_EXPIRATION_TIME;

    @Value("${jwt.refresh.expiration}")
    private long REFRESH_EXPIRATION_TIME;
    
    @Autowired
    private TokenDenylist tokenDenylist;  

    private SecretKey getSigningKey() {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalStateException("JWT secret key is not set in application.properties");
        }
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate an access token.
     *
     * @param username the username of the user
     * @param userId   the user ID
     * @return the generated JWT access token
     */
    public String generateToken(String username, String userId) {
        return buildToken(username, userId, "access", ACCESS_EXPIRATION_TIME);
    }

    /**
     * Generate a refresh token.
     *
     * @param username the username of the user
     * @param userId   the user ID
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(String username, String userId) {
        return buildToken(username, userId, "refresh", REFRESH_EXPIRATION_TIME);
    }

    /**
     * Build a JWT token with the specified parameters.
     *
     * @param username      the username of the user
     * @param userId        the user ID
     * @param tokenType     the type of the token (access or refresh)
     * @param expiration    the expiration time of the token in milliseconds
     * @return the generated JWT token
     */
    private String buildToken(String username, String userId, String tokenType, long expiration) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("token_type", tokenType)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate the JWT token, checking if it is expired and if it's blacklisted
     * @param token the JWT token
     * @return Claims if valid, null if invalid (expired or denylisted)
     */
    public Claims validateToken(String token) {
        // Check if the token is blacklisted (invalidated by force logout)
        if (tokenDenylist.isTokenDenylisted(getJtiFromToken(token))) {
            return null;  // Token is denylisted, so return null
        }

        // Check if the token is expired
        if (isTokenExpired(token)) {
            return null;  // Token is expired, so return null
        }

        // If token is not blacklisted and not expired, verify the token signature and return claims
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException ex) {

            return null;  // Token is expired, so return null
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            return null;  // Return null for any other JWT-related issue (invalid format, bad signature, etc.)
        }
    }

    /**
     * Check if the JWT token is expired
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = getAllClaimsFromToken(token).getExpiration();
        return expirationDate.before(new Date());
    }

    /**
     * Extract claims from the token
     * @param token the JWT token
     * @return all claims from the token
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();

    }

    /**
     * Extract a specific claim from the token using a function
     * @param token the JWT token
     * @param claimsResolver the function to extract the claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get the JWT ID (JTI) from the token
     * @param token the JWT token
     * @return the JWT ID
     */
    public String getJtiFromToken(String token) {
        return getAllClaimsFromToken(token).getId();
    }

    /**
     * Get the username from the token
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    /**
     * Get the user ID from the token
     * @param token the JWT token
     * @return the user ID
     */
    public String getUserIdFromToken(String token) {
        return getAllClaimsFromToken(token).get("userId", String.class);
    }

    /**
     * Get the expiration time of the refresh token
     * @return the expiration time in milliseconds
     */
    public long getRefreshTokenExpiration() {
        return REFRESH_EXPIRATION_TIME;
    }
}
