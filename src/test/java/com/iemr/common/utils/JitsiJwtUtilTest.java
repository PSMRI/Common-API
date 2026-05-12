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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JitsiJwtUtilTest {

    // Same secret format as the one devops gave us (HS256, length must be >=32 bytes for Keys.hmacShaKeyFor)
    private static final String APP_ID = "piramal_vc";
    private static final String APP_SECRET = "5b9883418be6f228ffe3ceaa74dd3d3b91737733a4a85c5e82fc584ad449850b";
    private static final String SUB = "meet.jitsi";

    private JitsiJwtUtil util;

    @BeforeEach
    void setUp() {
        util = new JitsiJwtUtil();
        ReflectionTestUtils.setField(util, "appId", APP_ID);
        ReflectionTestUtils.setField(util, "appSecret", APP_SECRET);
        ReflectionTestUtils.setField(util, "sub", SUB);
        ReflectionTestUtils.setField(util, "ttlSeconds", 3600L);
    }

    @Test
    void generateRoomToken_producesAllRequiredClaims() {
        String token = util.generateRoomToken("piramal-meeting-Ab3xQ9pK", "Dr. Asha", "asha@piramalswasthya.org", false);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 dot-separated parts");

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(APP_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(APP_ID, claims.getIssuer());
        assertTrue(claims.getAudience().contains(APP_ID));
        assertEquals(SUB, claims.getSubject());
        assertEquals("piramal-meeting-Ab3xQ9pK", claims.get("room", String.class));

        @SuppressWarnings("unchecked")
        Map<String, Object> context = claims.get("context", Map.class);
        assertNotNull(context);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        assertNotNull(user);
        assertEquals("Dr. Asha", user.get("name"));
        assertEquals("asha@piramalswasthya.org", user.get("email"));
        assertEquals(false, user.get("moderator"));

        Date exp = claims.getExpiration();
        assertNotNull(exp);
        assertTrue(exp.after(new Date()), "exp should be in the future");
    }

    @Test
    void generateRoomToken_moderatorClaimTrueForAgent() {
        String token = util.generateRoomToken("piramal-meeting-Ab3xQ9pK", "Dr. Asha", "asha@piramalswasthya.org", true);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(APP_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        Map<String, Object> context = claims.get("context", Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        assertEquals(true, user.get("moderator"));
    }

    @Test
    void generateRoomToken_fallsBackToGuestWhenUserNameNull() {
        String token = util.generateRoomToken("piramal-meeting-xyz", null, null, false);

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(APP_SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        Map<String, Object> context = claims.get("context", Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) context.get("user");
        assertEquals("Guest", user.get("name"));
        assertEquals("", user.get("email"));
    }

    @Test
    void generateRoomToken_rejectsEmptyRoom() {
        assertThrows(IllegalArgumentException.class,
                () -> util.generateRoomToken("", "Dr. Asha", "asha@piramalswasthya.org", false));
    }

    @Test
    void generateRoomToken_rejectsNullRoom() {
        assertThrows(IllegalArgumentException.class,
                () -> util.generateRoomToken(null, "Dr. Asha", "asha@piramalswasthya.org", false));
    }

    @Test
    void generateRoomToken_failsWhenAppSecretMissing() {
        ReflectionTestUtils.setField(util, "appSecret", "");
        assertThrows(IllegalStateException.class,
                () -> util.generateRoomToken("piramal-meeting-xyz", "Dr. Asha", "asha@piramalswasthya.org", false));
    }
}
