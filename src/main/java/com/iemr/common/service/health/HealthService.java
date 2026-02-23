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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class HealthService {

    private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

    private static final String LOG_EVENT_STUCK_PROCESS  = "MYSQL_STUCK_PROCESS";
    private static final String LOG_EVENT_LOCK_WAIT      = "MYSQL_LOCK_WAIT";
    private static final String LOG_EVENT_DEADLOCK       = "MYSQL_DEADLOCK";
    private static final String LOG_EVENT_SLOW_QUERIES   = "MYSQL_SLOW_QUERIES";
    private static final String LOG_EVENT_CONN_USAGE     = "MYSQL_CONNECTION_USAGE";
    private static final String LOG_EVENT_POOL_EXHAUSTED = "MYSQL_POOL_EXHAUSTED";
    private static final long RESPONSE_TIME_SLOW_MS    = 2000; // > 2s → SLOW
    private static final int  STUCK_PROCESS_THRESHOLD  = 5;    // > 5 stuck → WARNING
    private static final int  STUCK_PROCESS_SECONDS    = 30;   // process age in seconds
    private static final int  CONNECTION_USAGE_WARNING = 80;   // > 80% → WARNING
    private static final int  CONNECTION_USAGE_CRITICAL= 95;   // > 95% → CRITICAL
    private static final long DIAGNOSTIC_INTERVAL_SEC  = 30;   // background run interval
    private static final long DIAGNOSTIC_GUARD_SEC     = 25;   // safety dedup guard
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    private final ScheduledExecutorService diagnosticScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mysql-diagnostic-thread");
            t.setDaemon(true);
            return t;
        });

    private final AtomicLong lastDiagnosticRunAt = new AtomicLong(0);
    private final AtomicReference<String> cachedDbSeverity =
        new AtomicReference<>("INFO");
    private final AtomicLong previousDeadlockCount = new AtomicLong(0);
    public HealthService(ObjectProvider<DataSource> dataSourceProvider,
                         ObjectProvider<RedisConnectionFactory> redisProvider) {
        this.dataSource = dataSourceProvider.getIfAvailable();
        this.redisConnectionFactory = redisProvider.getIfAvailable();

        // Start background diagnostics only if DB is configured.
        // Initial delay = 0 so the first run happens at startup.
        if (this.dataSource != null) {
            diagnosticScheduler.scheduleAtFixedRate(
                this::runAdvancedMySQLDiagnostics,
                0,
                DIAGNOSTIC_INTERVAL_SEC,
                TimeUnit.SECONDS
            );
        }
    }

    @PreDestroy
    public void shutdownDiagnostics() {
        logger.info("[HEALTH_SERVICE_SHUTDOWN] Shutting down diagnostic scheduler...");
        diagnosticScheduler.shutdown();
        try {
            if (!diagnosticScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("[HEALTH_SERVICE_SHUTDOWN] Diagnostic scheduler did not terminate gracefully");
                diagnosticScheduler.shutdownNow();
            }
            logger.info("[HEALTH_SERVICE_SHUTDOWN] Diagnostic scheduler shut down successfully");
        } catch (InterruptedException e) {
            logger.error("[HEALTH_SERVICE_SHUTDOWN] Interrupted while shutting down scheduler", e);
            diagnosticScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // PUBLIC — Called by the /health controller
    public Map<String, Object> checkHealth() {
        Map<String, Object> response = new LinkedHashMap<>();

        Map<String, Object> mysqlResult = checkDatabaseConnectivity();
        Map<String, Object> redisResult = checkRedisConnectivity();

        String mysqlStatus = (String) mysqlResult.get("status");
        String redisStatus = (String) redisResult.get("status");

        boolean overallUp = !"DOWN".equals(mysqlStatus) && !"DOWN".equals(redisStatus);

        response.put("status",    overallUp ? "UP" : "DOWN");
        response.put("checkedAt", Instant.now().toString());
        
        // Expose only status and severity, keep diagnostics internal
        Map<String, Object> mysqlSummary = new LinkedHashMap<>();
        mysqlSummary.put("status", mysqlResult.get("status"));
        mysqlSummary.put("severity", mysqlResult.get("severity"));
        
        Map<String, Object> redisSummary = new LinkedHashMap<>();
        redisSummary.put("status", redisResult.get("status"));
        redisSummary.put("severity", redisResult.get("severity"));
        
        response.put("mysql",     mysqlSummary);
        response.put("redis",     redisSummary);

        return response;
    }
    // Runs only SELECT 1 with a hard 3-second timeout.
    private Map<String, Object> checkDatabaseConnectivity() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (dataSource == null) {
            result.put("status",         "NOT_CONFIGURED");
            result.put("severity",       "INFO");
            return result;
        }

        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.setQueryTimeout(3); // Hard cap — /health must never block > 3s
            stmt.execute("SELECT 1");

            // If SELECT 1 succeeds, use cached severity from background diagnostics
            String severity = cachedDbSeverity.get();
            result.put("status",   resolveDatabaseStatus(severity));
            result.put("severity", severity);

        } catch (Exception e) {
            // Log connection failure as a structured event
            logger.error(
                "[MYSQL_CONNECT_FAILED] MySQL connectivity check failed | error=\"{}\"",
                e.getMessage()
            );

            result.put("status",   "DOWN");
            result.put("severity", "CRITICAL");
        }

        return result;
    }

    private Map<String, Object> checkRedisConnectivity() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (redisConnectionFactory == null) {
            result.put("status",   "NOT_CONFIGURED");
            result.put("severity", "INFO");
            return result;
        }

        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            conn.ping();
            result.put("status",   "UP");
            result.put("severity", "OK");

        } catch (Exception e) {
            logger.error(
                "[REDIS_CONNECT_FAILED] Redis connectivity check failed | error=\"{}\"",
                e.getMessage()
            );

            result.put("status",   "DOWN");
            result.put("severity", "CRITICAL");
        }

        return result;
    }

    private void runAdvancedMySQLDiagnostics() {
        // Dedup guard: skip if last run was within the past 25 seconds
        long now = System.currentTimeMillis();
        if (now - lastDiagnosticRunAt.get() < TimeUnit.SECONDS.toMillis(DIAGNOSTIC_GUARD_SEC)) {
            return;
        }
        lastDiagnosticRunAt.set(now);

        String worstSeverity = "INFO"; // Escalates during checks, never descends

        try (Connection conn = dataSource.getConnection()) {

            // CHECK 1 — Stuck / Long-Running Processes
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM information_schema.PROCESSLIST " +
                    "WHERE TIME > " + STUCK_PROCESS_SECONDS + " AND COMMAND != 'Sleep'")) {
                
                if (rs.next()) {
                    int stuckCount = rs.getInt("cnt");
                    if (stuckCount > 0) {
                        logger.warn(
                            "[{}] Stuck MySQL processes detected | count={} | thresholdSeconds={}",
                            LOG_EVENT_STUCK_PROCESS, stuckCount, STUCK_PROCESS_SECONDS
                        );
                        if (stuckCount > STUCK_PROCESS_THRESHOLD) {
                            worstSeverity = escalate(worstSeverity, "WARNING");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("[MYSQL_DIAGNOSTIC_ERROR] Stuck process check failed | error=\"{}\"",
                    e.getMessage());
            }

            // CHECK 2 — InnoDB Long-Running Transactions (MYSQL_LONG_TX)
            // Note: INNODB_TRX shows all active transactions. True lock-wait detection via
            // INNODB_LOCK_WAITS requires PERFORMANCE_SCHEMA enabled and explicit permissions.
            // This query flags transactions older than STUCK_PROCESS_SECONDS as potentially problematic.
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM information_schema.INNODB_TRX " +
                    "WHERE TIME_TO_SEC(TIMEDIFF(NOW(), trx_started)) > " + STUCK_PROCESS_SECONDS)) {
                
                if (rs.next()) {
                    int lockCount = rs.getInt("cnt");
                    if (lockCount > 0) {
                        logger.error(
                            "[{}] InnoDB long-running transaction detected | count={} | thresholdSeconds={}",
                            LOG_EVENT_LOCK_WAIT, lockCount, STUCK_PROCESS_SECONDS
                        );
                        worstSeverity = escalate(worstSeverity, "CRITICAL");
                    }
                }
            } catch (Exception e) {
                logger.error("[MYSQL_DIAGNOSTIC_ERROR] Long transaction check failed | error=\"{}\"",
                    e.getMessage());
            }

            // CHECK 3 — InnoDB Deadlocks (Delta Tracking to avoid permanent WARNING)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Innodb_deadlocks'")) {
                
                if (rs.next()) {
                    long currentDeadlocks = rs.getLong("Value");
                    long previousDeadlocks = previousDeadlockCount.getAndSet(currentDeadlocks);
                    
                    // Only warn if deadlocks have *increased* since last run
                    if (currentDeadlocks > previousDeadlocks) {
                        long deltaDeadlocks = currentDeadlocks - previousDeadlocks;
                        logger.warn(
                            "[{}] InnoDB deadlocks detected since last run | deltaCount={} | cumulativeCount={}",
                            LOG_EVENT_DEADLOCK, deltaDeadlocks, currentDeadlocks
                        );
                        worstSeverity = escalate(worstSeverity, "WARNING");
                    }
                }
            } catch (Exception e) {
                logger.error("[MYSQL_DIAGNOSTIC_ERROR] Deadlock check failed | error=\"{}\"",
                    e.getMessage());
            }

            // CHECK 4 — Slow Queries
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Slow_queries'")) {
                
                if (rs.next()) {
                    long slowQueries = rs.getLong("Value");
                    if (slowQueries > 0) {
                        logger.warn(
                            "[{}] Slow queries detected | cumulativeCount={}",
                            LOG_EVENT_SLOW_QUERIES, slowQueries
                        );
                        worstSeverity = escalate(worstSeverity, "WARNING");
                    }
                }
            } catch (Exception e) {
                logger.error("[MYSQL_DIAGNOSTIC_ERROR] Slow query check failed | error=\"{}\"",
                    e.getMessage());
            }

            // CHECK 5 — Server Connection Usage
            try (Statement stmt = conn.createStatement()) {
                int threadsConnected = 0;
                int maxConnections   = 0;

                try (ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
                    if (rs.next()) threadsConnected = rs.getInt("Value");
                }

                try (ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'")) {
                    if (rs.next()) maxConnections = rs.getInt("Value");
                }

                if (maxConnections > 0) {
                    int usagePct = (int) ((threadsConnected * 100.0) / maxConnections);

                    if (usagePct >= CONNECTION_USAGE_CRITICAL) {
                        logger.error(
                            "[{}] MySQL connection pool near exhaustion | threadsConnected={} | maxConnections={} | usagePercent={}",
                            LOG_EVENT_POOL_EXHAUSTED, threadsConnected, maxConnections, usagePct
                        );
                        worstSeverity = escalate(worstSeverity, "CRITICAL");

                    } else if (usagePct > CONNECTION_USAGE_WARNING) {
                        logger.warn(
                            "[{}] MySQL connection usage is high | threadsConnected={} | maxConnections={} | usagePercent={}",
                            LOG_EVENT_CONN_USAGE, threadsConnected, maxConnections, usagePct
                        );
                        worstSeverity = escalate(worstSeverity, "WARNING");
                    }
                }
            } catch (Exception e) {
                logger.error("[MYSQL_DIAGNOSTIC_ERROR] Connection usage check failed | error=\"{}\"",
                    e.getMessage());
            }

        } catch (Exception e) {
            // Cannot open connection for diagnostics — treat as CRITICAL
            logger.error(
                "[MYSQL_DIAGNOSTIC_ERROR] Could not open connection for diagnostics | error=\"{}\"",
                e.getMessage()
            );
            worstSeverity = "CRITICAL";
        }

        // Persist computed severity so /health can read it instantly
        cachedDbSeverity.set(worstSeverity);

        logger.debug(
            "[MYSQL_DIAGNOSTIC_COMPLETE] Background diagnostic cycle complete | severity={}",
            worstSeverity
        );
    }
    private String resolveDatabaseStatus(String severity) {
        return switch (severity) {
            case "CRITICAL" -> "DOWN";
            case "WARNING"  -> "DEGRADED";
            default         -> "UP";
        };
    }
    private String escalate(String current, String candidate) {
        return severityRank(candidate) > severityRank(current) ? candidate : current;
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 2;
            case "WARNING"  -> 1;
            default         -> 0;
        };
    }
}