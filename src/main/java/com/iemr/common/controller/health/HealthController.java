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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

/**
 * Health check controller for Common-API.
 * <p>
 * Verifies application liveness and connectivity to underlying
 * runtime dependencies such as Database and Redis.
 * </p>
 *
 * @author vaishnavbhosale
 */
package com.iemr.common.controller.health;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @GetMapping("/health")
    public Map<String, Object> health() {

        Map<String, Object> response = new HashMap<>();
        Map<String, String> components = new HashMap<>();

        boolean dbUp = checkDatabase(components);
        boolean redisUp = checkRedis(components);

        response.put("status", (dbUp && redisUp) ? "UP" : "DOWN");
        response.put("components", components);

        return response;
    }

    private boolean checkDatabase(Map<String, String> components) {
        if (dataSource == null) {
            components.put("database", "NOT_CONFIGURED");
            return true;
        }

        try (Connection connection = dataSource.getConnection()) {
            components.put("database", "UP");
            return true;
        } catch (Exception e) {
            components.put("database", "DOWN");
            return false;
        }
    }

    private boolean checkRedis(Map<String, String> components) {
        if (redisConnectionFactory == null) {
            components.put("redis", "NOT_CONFIGURED");
            return true;
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            components.put("redis", "UP");
            return true;
        } catch (Exception e) {
            components.put("redis", "DOWN");
            return false;
        }
    }
}
