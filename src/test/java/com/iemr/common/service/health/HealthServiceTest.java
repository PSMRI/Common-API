package com.iemr.common.service.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers the /health probe and the background MySQL diagnostics that feed its severity.
 *
 * <p>The service is built with empty providers so no scheduler thread starts, and the
 * data source and Redis factory are then injected directly. That keeps the diagnostic
 * cycle under the test's control instead of racing a background thread.
 */
class HealthServiceTest {

	private DataSource dataSource;
	private RedisConnectionFactory redisConnectionFactory;
	private Connection connection;
	private Statement statement;

	private HealthService service;

	@SuppressWarnings("unchecked")
	private static ObjectProvider<Object> emptyProvider() {
		ObjectProvider<Object> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(null);
		return provider;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeEach
	void setUp() throws SQLException {
		service = new HealthService((ObjectProvider) emptyProvider(), (ObjectProvider) emptyProvider());

		dataSource = mock(DataSource.class);
		redisConnectionFactory = mock(RedisConnectionFactory.class);
		connection = mock(Connection.class);
		statement = mock(Statement.class);

		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
	}

	private void withDataSource() {
		ReflectionTestUtils.setField(service, "dataSource", dataSource);
	}

	private void withRedis() {
		ReflectionTestUtils.setField(service, "redisConnectionFactory", redisConnectionFactory);
	}

	/** Clears the 25 second dedup guard so an explicit diagnostic run is not skipped. */
	private void allowDiagnosticRun() {
		((AtomicLong) ReflectionTestUtils.getField(service, "lastDiagnosticRunAt")).set(0);
	}

	private void runDiagnostics() {
		allowDiagnosticRun();
		ReflectionTestUtils.invokeMethod(service, "runAdvancedMySQLDiagnostics");
	}

	private String cachedSeverity() {
		return String.valueOf(ReflectionTestUtils.invokeGetterMethod(
				ReflectionTestUtils.getField(service, "cachedDbSeverity"), "get"));
	}

	/** A single-row result set answering {@code column} with {@code value}. */
	private ResultSet singleRow(String column, long value) throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(true, false);
		when(rs.getInt(column)).thenReturn((int) value);
		when(rs.getLong(column)).thenReturn(value);
		return rs;
	}

	private ResultSet emptyResultSet() throws SQLException {
		ResultSet rs = mock(ResultSet.class);
		when(rs.next()).thenReturn(false);
		return rs;
	}

	/** Stubs every diagnostic query with a result set that reports nothing of concern. */
	private void stubHealthyDiagnostics() throws SQLException {
		when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
			String sql = invocation.getArgument(0);
			if (sql.contains("max_connections")) {
				return singleRow("Value", 100);
			}
			if (sql.contains("Threads_connected")) {
				return singleRow("Value", 10);
			}
			return emptyResultSet();
		});
	}

	@Nested
	@DisplayName("checkHealth")
	class CheckHealth {

		@Test
		@DisplayName("reports both dependencies as not configured when neither is wired")
		void reportsNotConfigured() {
			Map<String, Object> health = service.checkHealth();

			assertThat(health).containsEntry("status", "UP");
			assertThat(health).containsKey("checkedAt");
			assertThat(mysql(health)).containsEntry("status", "NOT_CONFIGURED").containsEntry("severity", "INFO");
			assertThat(redis(health)).containsEntry("status", "NOT_CONFIGURED").containsEntry("severity", "INFO");
		}

		@Test
		@DisplayName("reports UP when the SELECT 1 probe and the Redis ping both succeed")
		void reportsUpWhenBothDependenciesRespond() throws SQLException {
			withDataSource();
			withRedis();
			RedisConnection redisConnection = mock(RedisConnection.class);
			when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);

			Map<String, Object> health = service.checkHealth();

			assertThat(health).containsEntry("status", "UP");
			assertThat(mysql(health)).containsEntry("status", "UP").containsEntry("severity", "OK");
			assertThat(redis(health)).containsEntry("status", "UP").containsEntry("severity", "OK");
			verify(statement).setQueryTimeout(3);
			verify(statement).execute("SELECT 1");
			verify(redisConnection).ping();
		}

		@Test
		@DisplayName("reports DOWN when the database probe fails")
		void reportsDownWhenDatabaseFails() throws SQLException {
			withDataSource();
			when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));

			Map<String, Object> health = service.checkHealth();

			assertThat(health).containsEntry("status", "DOWN");
			assertThat(mysql(health)).containsEntry("status", "DOWN").containsEntry("severity", "CRITICAL");
		}

		@Test
		@DisplayName("reports DOWN when the Redis ping fails")
		void reportsDownWhenRedisFails() {
			withRedis();
			when(redisConnectionFactory.getConnection()).thenThrow(new IllegalStateException("no redis"));

			Map<String, Object> health = service.checkHealth();

			assertThat(health).containsEntry("status", "DOWN");
			assertThat(redis(health)).containsEntry("status", "DOWN").containsEntry("severity", "CRITICAL");
		}

		@ParameterizedTest(name = "severity {0} surfaces as status {1}")
		@CsvSource({ "OK, UP", "WARNING, DEGRADED", "CRITICAL, DOWN" })
		@DisplayName("the cached diagnostic severity decides the reported database status")
		void cachedSeverityDrivesStatus(String severity, String expectedStatus) throws SQLException {
			withDataSource();
			ReflectionTestUtils.invokeMethod(ReflectionTestUtils.getField(service, "cachedDbSeverity"), "set",
					severity);

			Map<String, Object> health = service.checkHealth();

			assertThat(mysql(health)).containsEntry("status", expectedStatus).containsEntry("severity", severity);
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> mysql(Map<String, Object> health) {
			return (Map<String, Object>) health.get("mysql");
		}

		@SuppressWarnings("unchecked")
		private Map<String, Object> redis(Map<String, Object> health) {
			return (Map<String, Object>) health.get("redis");
		}
	}

	@Nested
	@DisplayName("background MySQL diagnostics")
	class Diagnostics {

		@Test
		@DisplayName("a clean database leaves the cached severity at OK")
		void healthyDatabaseStaysOk() throws SQLException {
			withDataSource();
			stubHealthyDiagnostics();

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("OK");
		}

		@Test
		@DisplayName("stuck processes above the threshold raise a warning")
		void stuckProcessesRaiseWarning() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("PROCESSLIST") ? singleRow("cnt", 6) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("WARNING");
		}

		@Test
		@DisplayName("stuck processes below the threshold are noted but not escalated")
		void stuckProcessesBelowThresholdStayOk() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("PROCESSLIST") ? singleRow("cnt", 2) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("OK");
		}

		@ParameterizedTest(name = "{0} long transactions escalate to {1}")
		@CsvSource({ "1, WARNING", "4, WARNING", "5, CRITICAL", "9, CRITICAL" })
		@DisplayName("long running transactions escalate by count")
		void longTransactionsEscalateByCount(int count, String expected) throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("INNODB_TRX") ? singleRow("cnt", count) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo(expected);
		}

		@Test
		@DisplayName("a rising deadlock counter raises a warning")
		void risingDeadlocksRaiseWarning() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("Innodb_deadlocks") ? singleRow("Value", 3) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("WARNING");
		}

		@Test
		@DisplayName("a deadlock counter that has not moved is not reported again")
		void steadyDeadlockCounterStaysOk() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("Innodb_deadlocks") ? singleRow("Value", 3) : emptyResultSet();
			});

			runDiagnostics();
			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("OK");
		}

		@Test
		@DisplayName("newly recorded slow queries raise a warning")
		void newSlowQueriesRaiseWarning() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				return sql.contains("Slow_queries") ? singleRow("Value", 12) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("WARNING");
		}

		@ParameterizedTest(name = "{0} of {1} connections escalates to {2}")
		@CsvSource({ "85, 100, WARNING", "96, 100, CRITICAL", "50, 100, OK" })
		@DisplayName("connection pool usage escalates by percentage")
		void connectionUsageEscalates(int connected, int max, String expected) throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				if (sql.contains("Threads_connected")) {
					return singleRow("Value", connected);
				}
				if (sql.contains("max_connections")) {
					return singleRow("Value", max);
				}
				return emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo(expected);
		}

		@Test
		@DisplayName("an unreadable max_connections value is ignored")
		void zeroMaxConnectionsIsIgnored() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				if (sql.contains("Threads_connected")) {
					return singleRow("Value", 10);
				}
				return sql.contains("max_connections") ? singleRow("Value", 0) : emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("OK");
		}

		@Test
		@DisplayName("the worst severity across all checks wins")
		void worstSeverityWins() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
				String sql = invocation.getArgument(0);
				if (sql.contains("PROCESSLIST")) {
					return singleRow("cnt", 6);
				}
				if (sql.contains("INNODB_TRX")) {
					return singleRow("cnt", 7);
				}
				return emptyResultSet();
			});

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("CRITICAL");
		}

		@Test
		@DisplayName("a failing individual check does not abort the cycle")
		void aFailingCheckDoesNotAbortTheCycle() throws SQLException {
			withDataSource();
			when(statement.executeQuery(anyString())).thenThrow(new SQLException("permission denied"));

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("OK");
		}

		@Test
		@DisplayName("a diagnostics connection failure is recorded as critical")
		void connectionFailureIsCritical() throws SQLException {
			withDataSource();
			when(dataSource.getConnection()).thenThrow(new SQLException("pool exhausted"));

			runDiagnostics();

			assertThat(cachedSeverity()).isEqualTo("CRITICAL");
		}

		@Test
		@DisplayName("a second run inside the dedup guard window is skipped")
		void dedupGuardSkipsRapidReruns() throws SQLException {
			withDataSource();
			stubHealthyDiagnostics();

			runDiagnostics();
			// No guard reset this time, so the run should be skipped outright.
			ReflectionTestUtils.invokeMethod(service, "runAdvancedMySQLDiagnostics");

			verify(dataSource).getConnection();
		}

		@Test
		@DisplayName("no diagnostic scheduler is started when the database is not configured")
		void noSchedulerWithoutDataSource() throws SQLException {
			runDiagnostics();

			verify(dataSource, never()).getConnection();
		}
	}

	@Test
	@DisplayName("shutdownDiagnostics stops the scheduler")
	void shutdownStopsTheScheduler() {
		service.shutdownDiagnostics();

		assertThat(((java.util.concurrent.ScheduledExecutorService) ReflectionTestUtils.getField(service,
				"diagnosticScheduler")).isShutdown()).isTrue();
	}
}
