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
package com.iemr.common.service.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider; // <--- VITAL IMPORT
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check service for Common-API.
 * Verifies application liveness and dependency health (DB, Redis).
 *
 * @author vaishnavbhosale
 */
@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

    private static final String COMPONENT_DATABASE = "database";
    private static final String COMPONENT_REDIS = "redis";

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    // --- CORRECT CONSTRUCTOR START ---
    public HealthService(ObjectProvider<DataSource> dataSourceProvider,
                         ObjectProvider<RedisConnectionFactory> redisProvider) {
        // This allows them to be null without crashing the app
        this.dataSource = dataSourceProvider.getIfAvailable();
        this.redisConnectionFactory = redisProvider.getIfAvailable();
    }
    // --- CORRECT CONSTRUCTOR END ---

    public Map<String, Object> checkHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> components = new HashMap<>();

        boolean dbUp = checkDatabase(components);
        boolean redisUp = checkRedis(components);

        boolean overallUp = dbUp && redisUp;

        response.put("status", overallUp ? "UP" : "DOWN");
        response.put("components", components);

        return response;
    }

    private boolean checkDatabase(Map<String, String> components) {
        if (dataSource == null) {
            components.put(COMPONENT_DATABASE, "NOT_CONFIGURED");
            return true;
        }

        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            statement.execute("SELECT 1");
            components.put(COMPONENT_DATABASE, "UP");
            return true;

        } catch (Exception e) {
            logger.error("Database health check failed", e);
            components.put(COMPONENT_DATABASE, "DOWN");
            return false;
        }
    }

    private boolean checkRedis(Map<String, String> components) {
        if (redisConnectionFactory == null) {
            components.put(COMPONENT_REDIS, "NOT_CONFIGURED");
            return true;
        }

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            components.put(COMPONENT_REDIS, "UP");
            return true;

        } catch (Exception e) {
            logger.error("Redis health check failed", e);
            components.put(COMPONENT_REDIS, "DOWN");
            return false;
        }
    }
}