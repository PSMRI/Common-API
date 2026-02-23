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

    // Event log constants
    private static final String LOG_EVENT_STUCK_PROCESS  = "MYSQL_STUCK_PROCESS";
    private static final String LOG_EVENT_LOCK_WAIT      = "MYSQL_LOCK_WAIT";
    private static final String LOG_EVENT_DEADLOCK       = "MYSQL_DEADLOCK";
    private static final String LOG_EVENT_SLOW_QUERIES   = "MYSQL_SLOW_QUERIES";
    private static final String LOG_EVENT_CONN_USAGE     = "MYSQL_CONNECTION_USAGE";
    private static final String LOG_EVENT_POOL_EXHAUSTED = "MYSQL_POOL_EXHAUSTED";

    // Response field constants
    private static final String FIELD_STATUS   = "status";
    private static final String FIELD_SEVERITY = "severity";
    private static final String FIELD_MYSQL    = "mysql";
    private static final String FIELD_REDIS    = "redis";
    private static final String FIELD_CHECKED_AT = "checkedAt";

    // Severity constants
    private static final String SEVERITY_CRITICAL = "CRITICAL";
    private static final String SEVERITY_WARNING  = "WARNING";
    private static final String SEVERITY_OK       = "OK";
    private static final String SEVERITY_INFO     = "INFO";

    // Database query constants
    private static final String STATUS_VALUE = "Value";
    private static final String STATUS_UP     = "UP";
    private static final String STATUS_DOWN   = "DOWN";
    private static final String STATUS_DEGRADED = "DEGRADED";
    private static final String STATUS_NOT_CONFIGURED = "NOT_CONFIGURED";

    // Thresholds
    private static final long RESPONSE_TIME_SLOW_MS    = 2000; // > 2s → SLOW
    private static final int  STUCK_PROCESS_THRESHOLD  = 5;    // > 5 stuck → WARNING
    private static final int  STUCK_PROCESS_SECONDS    = 30;   // process age in seconds
    private static final int  LONG_TXN_WARNING_THRESHOLD  = 1;  // ≥1 long txn → WARNING
    private static final int  LONG_TXN_CRITICAL_THRESHOLD = 5;  // ≥5 long txns → CRITICAL
    private static final int  LONG_TXN_SECONDS         = 60;   // transaction age threshold
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
        new AtomicReference<>(SEVERITY_OK);
    private final AtomicLong previousDeadlockCount = new AtomicLong(0);
    private final AtomicLong previousSlowQueryCount = new AtomicLong(0);
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

        String mysqlStatus = (String) mysqlResult.get(FIELD_STATUS);
        String redisStatus = (String) redisResult.get(FIELD_STATUS);

        boolean overallUp = !STATUS_DOWN.equals(mysqlStatus) && !STATUS_DOWN.equals(redisStatus);

        response.put(FIELD_STATUS,     overallUp ? STATUS_UP : STATUS_DOWN);
        response.put(FIELD_CHECKED_AT, Instant.now().toString());
        
        // Expose only status and severity, keep diagnostics internal
        Map<String, Object> mysqlSummary = new LinkedHashMap<>();
        mysqlSummary.put(FIELD_STATUS, mysqlResult.get(FIELD_STATUS));
        mysqlSummary.put(FIELD_SEVERITY, mysqlResult.get(FIELD_SEVERITY));
        
        Map<String, Object> redisSummary = new LinkedHashMap<>();
        redisSummary.put(FIELD_STATUS, redisResult.get(FIELD_STATUS));
        redisSummary.put(FIELD_SEVERITY, redisResult.get(FIELD_SEVERITY));
        
        response.put(FIELD_MYSQL,     mysqlSummary);
        response.put(FIELD_REDIS,     redisSummary);

        return response;
    }
    // Runs only SELECT 1 with a hard 3-second timeout.
    private Map<String, Object> checkDatabaseConnectivity() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (dataSource == null) {
            result.put(FIELD_STATUS,   STATUS_NOT_CONFIGURED);
            result.put(FIELD_SEVERITY, SEVERITY_INFO);
            return result;
        }

        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.setQueryTimeout(3); // Hard cap — /health must never block > 3s
            stmt.execute("SELECT 1");

            // If SELECT 1 succeeds, use cached severity from background diagnostics
            String severity = cachedDbSeverity.get();
            result.put(FIELD_STATUS,   resolveDatabaseStatus(severity));
            result.put(FIELD_SEVERITY, severity);

        } catch (Exception e) {
            // Log connection failure as a structured event
            logger.error(
                "[MYSQL_CONNECT_FAILED] MySQL connectivity check failed | error=\"{}\"",
                e.getMessage()
            );

            result.put(FIELD_STATUS,   STATUS_DOWN);
            result.put(FIELD_SEVERITY, SEVERITY_CRITICAL);
        }

        return result;
    }

    private Map<String, Object> checkRedisConnectivity() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (redisConnectionFactory == null) {
            result.put(FIELD_STATUS,   STATUS_NOT_CONFIGURED);
            result.put(FIELD_SEVERITY, SEVERITY_INFO);
            return result;
        }

        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            conn.ping();
            result.put(FIELD_STATUS,   STATUS_UP);
            result.put(FIELD_SEVERITY, SEVERITY_OK);

        } catch (Exception e) {
            logger.error(
                "[REDIS_CONNECT_FAILED] Redis connectivity check failed | error=\"{}\"",
                e.getMessage()
            );

            result.put(FIELD_STATUS,   STATUS_DOWN);
            result.put(FIELD_SEVERITY, SEVERITY_CRITICAL);
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

        String worstSeverity = SEVERITY_OK;

        try (Connection conn = dataSource.getConnection()) {
            worstSeverity = escalate(worstSeverity, performStuckProcessCheck(conn));
            worstSeverity = escalate(worstSeverity, performLongTransactionCheck(conn));
            worstSeverity = escalate(worstSeverity, performDeadlockCheck(conn));
            worstSeverity = escalate(worstSeverity, performSlowQueryCheck(conn));
            worstSeverity = escalate(worstSeverity, performConnectionUsageCheck(conn));

        } catch (Exception e) {
            logger.error(
                "[MYSQL_DIAGNOSTIC_ERROR] Could not open connection for diagnostics | error=\"{}\"",
                e.getMessage()
            );
            worstSeverity = SEVERITY_CRITICAL;
        }

        cachedDbSeverity.set(worstSeverity);
        logger.debug(
            "[MYSQL_DIAGNOSTIC_COMPLETE] Background diagnostic cycle complete | severity={}",
            worstSeverity
        );
    }

    private String performStuckProcessCheck(Connection conn) {
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
                        return SEVERITY_WARNING;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[MYSQL_DIAGNOSTIC_ERROR] Stuck process check failed | error=\"{}\"",
                e.getMessage());
        }
        return SEVERITY_OK;
    }

    private String performLongTransactionCheck(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) AS cnt FROM information_schema.INNODB_TRX " +
                "WHERE TIME_TO_SEC(TIMEDIFF(NOW(), trx_started)) > " + LONG_TXN_SECONDS)) {
            
            if (rs.next()) {
                int lockCount = rs.getInt("cnt");
                if (lockCount >= LONG_TXN_WARNING_THRESHOLD) {
                    logger.warn(
                        "[{}] InnoDB long-running transaction(s) detected | count={} | thresholdSeconds={}",
                        LOG_EVENT_LOCK_WAIT, lockCount, LONG_TXN_SECONDS
                    );
                    // Graduated escalation: WARNING for 1-4, CRITICAL for 5+
                    return lockCount >= LONG_TXN_CRITICAL_THRESHOLD
                        ? SEVERITY_CRITICAL : SEVERITY_WARNING;
                }
            }
        } catch (Exception e) {
            logger.error("[MYSQL_DIAGNOSTIC_ERROR] Long transaction check failed | error=\"{}\"",
                e.getMessage());
        }
        return SEVERITY_OK;
    }

    private String performDeadlockCheck(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Innodb_deadlocks'")) {
            
            if (rs.next()) {
                long currentDeadlocks = rs.getLong(STATUS_VALUE);
                long previousDeadlocks = previousDeadlockCount.getAndSet(currentDeadlocks);
                
                if (currentDeadlocks > previousDeadlocks) {
                    long deltaDeadlocks = currentDeadlocks - previousDeadlocks;
                    logger.warn(
                        "[{}] InnoDB deadlocks detected since last run | deltaCount={} | cumulativeCount={}",
                        LOG_EVENT_DEADLOCK, deltaDeadlocks, currentDeadlocks
                    );
                    return SEVERITY_WARNING;
                }
            }
        } catch (Exception e) {
            logger.error("[MYSQL_DIAGNOSTIC_ERROR] Deadlock check failed | error=\"{}\"",
                e.getMessage());
        }
        return SEVERITY_OK;
    }

    private String performSlowQueryCheck(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Slow_queries'")) {
            
            if (rs.next()) {
                long slowQueries = rs.getLong(STATUS_VALUE);
                long previousSlow = previousSlowQueryCount.getAndSet(slowQueries);
                
                // Only warn if slow queries have *increased* since last run
                if (slowQueries > previousSlow) {
                    long delta = slowQueries - previousSlow;
                    logger.warn(
                        "[{}] New slow queries detected since last run | deltaCount={} | cumulativeCount={}",
                        LOG_EVENT_SLOW_QUERIES, delta, slowQueries
                    );
                    return SEVERITY_WARNING;
                }
            }
        } catch (Exception e) {
            logger.error("[MYSQL_DIAGNOSTIC_ERROR] Slow query check failed | error=\"{}\"",
                e.getMessage());
        }
        return SEVERITY_OK;
    }

    private String performConnectionUsageCheck(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            int threadsConnected = 0;
            int maxConnections   = 0;

            try (ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
                if (rs.next()) threadsConnected = rs.getInt(STATUS_VALUE);
            }

            try (ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'")) {
                if (rs.next()) maxConnections = rs.getInt(STATUS_VALUE);
            }

            if (maxConnections > 0) {
                int usagePct = (int) ((threadsConnected * 100.0) / maxConnections);

                if (usagePct >= CONNECTION_USAGE_CRITICAL) {
                    logger.error(
                        "[{}] MySQL connection pool near exhaustion | threadsConnected={} | maxConnections={} | usagePercent={}",
                        LOG_EVENT_POOL_EXHAUSTED, threadsConnected, maxConnections, usagePct
                    );
                    return SEVERITY_CRITICAL;

                } else if (usagePct > CONNECTION_USAGE_WARNING) {
                    logger.warn(
                        "[{}] MySQL connection usage is high | threadsConnected={} | maxConnections={} | usagePercent={}",
                        LOG_EVENT_CONN_USAGE, threadsConnected, maxConnections, usagePct
                    );
                    return SEVERITY_WARNING;
                }
            }
        } catch (Exception e) {
            logger.error("[MYSQL_DIAGNOSTIC_ERROR] Connection usage check failed | error=\"{}\"",
                e.getMessage());
        }
        return SEVERITY_OK;
    }
    private String resolveDatabaseStatus(String severity) {
        return switch (severity) {
            case SEVERITY_CRITICAL -> STATUS_DOWN;
            case SEVERITY_WARNING  -> STATUS_DEGRADED;
            default                -> STATUS_UP;
        };
    }
    private String escalate(String current, String candidate) {
        return severityRank(candidate) > severityRank(current) ? candidate : current;
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case SEVERITY_CRITICAL -> 2;
            case SEVERITY_WARNING  -> 1;
            default                -> 0;
        };
    }
}