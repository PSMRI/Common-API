/*
* AMRIT – Accessible Medical Records via Integrated Technology
* Integrated EHR (Electronic Health Records) Solution
*
* Copyright (C) "Piramal Swasthya Management and Research Institute"
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Mints HS256 JWTs that are accepted by the Jitsi/prosody token-auth module
 * running on the video-conferencing host. This is intentionally separate from
 * {@link JwtUtil} (which mints application session tokens) because the secret,
 * claim set, and expiration policy are completely different.
 *
 * Claims produced (matches what devops configured on prosody):
 *   aud  -> jitsi.app.id           (e.g. "piramal_vc")
 *   iss  -> jitsi.app.id           (e.g. "piramal_vc")
 *   sub  -> jitsi.sub              (must always be "meet.jitsi")
 *   room -> the room name to admit the bearer into
 *   exp  -> now + jitsi.token.ttl.seconds
 *   context.user.{name,email} -> displayed in the Jitsi UI
 */
@Component
public class JitsiJwtUtil {

    // Fallback chains let either dot-form (jitsi.app.id=...) or upper-form
    // (JITSI_APP_ID=...) work in any property source, including .properties
    // files which Spring does NOT relaxed-bind for @Value.
    @Value("${jitsi.app.id:${JITSI_APP_ID:}}")
    private String appId;

    @Value("${jitsi.app.secret:${JITSI_APP_SECRET:}}")
    private String appSecret;

    @Value("${jitsi.sub:${JITSI_SUB:meet.jitsi}}")
    private String sub;

    @Value("${jitsi.token.ttl.seconds:${JITSI_TOKEN_TTL_SECONDS:3600}}")
    private long ttlSeconds;

    private SecretKey getSigningKey() {
        if (appSecret == null || appSecret.isEmpty()) {
            throw new IllegalStateException("jitsi.app.secret is not configured");
        }
        return Keys.hmacShaKeyFor(appSecret.getBytes());
    }

    /**
     * Build a Jitsi room JWT.
     *
     * @param room      the exact room name the bearer will join (must match the URL path)
     * @param userName  display name shown in the Jitsi UI
     * @param userEmail email shown in the Jitsi UI (used for gravatar etc.)
     * @return signed compact JWT string
     */
    public String generateRoomToken(String room, String userName, String userEmail) {
        if (room == null || room.isEmpty()) {
            throw new IllegalArgumentException("room is required to mint a Jitsi token");
        }

        long nowMs = System.currentTimeMillis();
        Date expiry = new Date(nowMs + (ttlSeconds * 1000L));

        Map<String, Object> user = new HashMap<>();
        user.put("name", userName != null ? userName : "Guest");
        user.put("email", userEmail != null ? userEmail : "");

        Map<String, Object> context = new HashMap<>();
        context.put("user", user);

        return Jwts.builder()
                .claim("aud", appId)
                .issuer(appId)
                .subject(sub)
                .claim("room", room)
                .claim("context", context)
                .expiration(expiry)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
}
