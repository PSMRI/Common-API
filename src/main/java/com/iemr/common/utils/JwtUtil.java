package com.iemr.common.utils;

import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String SECRET_KEY;

	@Value("${jwt.access.expiration}")
	private long ACCESS_EXPIRATION_TIME;

	@Value("${jwt.refresh.expiration}")
	private long REFRESH_EXPIRATION_TIME;

	private SecretKey getSigningKey() {
		if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
			throw new IllegalStateException("JWT secret key is not set in application.properties");
		}
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}

	public String generateToken(String username, String userId) {
		return buildToken(username, userId, "access", ACCESS_EXPIRATION_TIME);
	}

	public String generateRefreshToken(String username, String userId) {
		return buildToken(username, userId, "refresh", REFRESH_EXPIRATION_TIME);
	}

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

	public Claims validateToken(String token) {
		try {
			return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
					.getPayload();

		} catch (ExpiredJwtException ex) {
			// Handle expired token specifically if needed
		} catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
			// Log specific error types
		}
		return null;
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}


	public long getRefreshTokenExpiration() {
		return REFRESH_EXPIRATION_TIME;
	}

	public String getUserIdFromToken(String token) {
		return getAllClaimsFromToken(token).get("userId", String.class);
	}

	// Additional helper methods
	public String getJtiFromToken(String token) {
		return getAllClaimsFromToken(token).getId();
	}

	public String getUsernameFromToken(String token) {
		return getAllClaimsFromToken(token).getSubject();
	}
}