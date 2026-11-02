package com.iemr.common.service.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.iemr.common.data.users.User;
import com.iemr.common.repository.users.IEMRUserRepositoryCustom;
import com.iemr.common.repository.users.UserRoleMappingRepository;
import com.iemr.common.utils.exception.IEMRException;

/**
 * Covers administrative account locking, unlocking and lock reporting.
 *
 * <p>The service distinguishes an account locked by failed logins (which carries a lock
 * timestamp and expires on its own) from one deactivated by an administrator (which does
 * not), so both are driven through each operation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IEMRAdminUserServiceImplAccountLockTest {

	private static final Long USER_ID = 42L;
	private static final int LOCK_DURATION_HOURS = 24;

	@Mock
	private IEMRUserRepositoryCustom iEMRUserRepositoryCustom;
	@Mock
	private UserRoleMappingRepository userRoleMappingRepository;

	private IEMRAdminUserServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new IEMRAdminUserServiceImpl();
		ReflectionTestUtils.setField(service, "iEMRUserRepositoryCustom", iEMRUserRepositoryCustom);
		ReflectionTestUtils.setField(service, "userRoleMappingRepository", userRoleMappingRepository);
		ReflectionTestUtils.setField(service, "accountLockDurationHours", LOCK_DURATION_HOURS);
		ReflectionTestUtils.setField(service, "failedLoginAttempt", "5");
		when(iEMRUserRepositoryCustom.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	/** A user in the requested lock state. */
	private User user(Boolean deleted, Timestamp lockTimestamp, Integer failedAttempts) {
		User user = new User();
		user.setUserID(USER_ID);
		user.setUserName("asha");
		user.setStatusID(1);
		user.setDeleted(deleted);
		user.setLockTimestamp(lockTimestamp);
		user.setFailedAttempt(failedAttempts);
		return user;
	}

	private void userExists(User user) {
		when(iEMRUserRepositoryCustom.findById(USER_ID)).thenReturn(Optional.ofNullable(user));
	}

	private static Timestamp hoursAgo(int hours) {
		return new Timestamp(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(hours));
	}

	@Nested
	@DisplayName("lockUserAccount")
	class LockUserAccount {

		@Test
		@DisplayName("locks an active account and stamps the lock time")
		void locksAnActiveAccount() throws Exception {
			User user = user(false, null, 0);
			userExists(user);

			assertThat(service.lockUserAccount(USER_ID)).isTrue();
			assertThat(user.getDeleted()).isTrue();
			assertThat(user.getLockTimestamp()).isNotNull();
			assertThat(user.getFailedAttempt()).isEqualTo(5);
			verify(iEMRUserRepositoryCustom).save(user);
		}

		@Test
		@DisplayName("locks an account whose deleted flag has never been set")
		void locksAnAccountWithNoDeletedFlag() throws Exception {
			userExists(user(null, null, null));

			assertThat(service.lockUserAccount(USER_ID)).isTrue();
		}

		@Test
		@DisplayName("reports an already locked account without touching it")
		void reportsAnAlreadyLockedAccount() throws Exception {
			User user = user(true, hoursAgo(1), 5);
			userExists(user);

			assertThat(service.lockUserAccount(USER_ID)).isFalse();
			verify(iEMRUserRepositoryCustom, never()).save(any(User.class));
		}

		@Test
		@DisplayName("refuses to lock an account an administrator has deactivated")
		void refusesToLockADeactivatedAccount() {
			userExists(user(true, null, 0));

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.lockUserAccount(USER_ID))
					.withMessageContaining("deactivated by administrator");
		}

		@Test
		@DisplayName("refuses to lock a user that does not exist")
		void refusesUnknownUser() {
			userExists(null);

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.lockUserAccount(USER_ID))
					.withMessageContaining("User not found with ID: 42");
		}

		@Test
		@DisplayName("reports a repository failure as a lock failure")
		void reportsRepositoryFailure() {
			when(iEMRUserRepositoryCustom.findById(anyLong())).thenThrow(new IllegalStateException("db down"));

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.lockUserAccount(USER_ID))
					.withMessageContaining("Error locking user account");
		}

		@Test
		@DisplayName("falls back to a threshold of five when the configured value is unusable")
		void fallsBackToDefaultThreshold() throws Exception {
			ReflectionTestUtils.setField(service, "failedLoginAttempt", "not-a-number");
			User user = user(false, null, 0);
			userExists(user);

			service.lockUserAccount(USER_ID);

			assertThat(user.getFailedAttempt()).isEqualTo(5);
		}

		@Test
		@DisplayName("honours a configured failed attempt threshold")
		void honoursConfiguredThreshold() throws Exception {
			ReflectionTestUtils.setField(service, "failedLoginAttempt", "3");
			User user = user(false, null, 0);
			userExists(user);

			service.lockUserAccount(USER_ID);

			assertThat(user.getFailedAttempt()).isEqualTo(3);
		}
	}

	@Nested
	@DisplayName("unlockUserAccount")
	class UnlockUserAccount {

		@Test
		@DisplayName("unlocks an account locked by failed logins and clears its counters")
		void unlocksALockedAccount() throws Exception {
			User user = user(true, hoursAgo(1), 5);
			userExists(user);

			assertThat(service.unlockUserAccount(USER_ID)).isTrue();
			assertThat(user.getDeleted()).isFalse();
			assertThat(user.getFailedAttempt()).isZero();
			assertThat(user.getLockTimestamp()).isNull();
			verify(iEMRUserRepositoryCustom).save(user);
		}

		@Test
		@DisplayName("refuses to unlock an account an administrator has deactivated")
		void refusesToUnlockADeactivatedAccount() {
			userExists(user(true, null, 0));

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.unlockUserAccount(USER_ID))
					.withMessageContaining("Use user management to reactivate");
		}

		@Test
		@DisplayName("reports an account that was not locked")
		void reportsAnUnlockedAccount() throws Exception {
			userExists(user(false, null, 0));

			assertThat(service.unlockUserAccount(USER_ID)).isFalse();
			verify(iEMRUserRepositoryCustom, never()).save(any(User.class));
		}

		@Test
		@DisplayName("refuses to unlock a user that does not exist")
		void refusesUnknownUser() {
			userExists(null);

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.unlockUserAccount(USER_ID))
					.withMessageContaining("User not found with ID: 42");
		}

		@Test
		@DisplayName("reports a repository failure as an unlock failure")
		void reportsRepositoryFailure() {
			when(iEMRUserRepositoryCustom.findById(anyLong())).thenThrow(new IllegalStateException("db down"));

			assertThatExceptionOfType(IEMRException.class).isThrownBy(() -> service.unlockUserAccount(USER_ID))
					.withMessageContaining("Error unlocking user account");
		}
	}

	@Nested
	@DisplayName("getUserLockStatusJson")
	class UserLockStatus {

		@Test
		@DisplayName("reports an active account as unlocked")
		void reportsAnActiveAccount() throws Exception {
			userExists(user(false, null, 2));

			JSONObject status = new JSONObject(service.getUserLockStatusJson(USER_ID));

			assertThat(status.getLong("userId")).isEqualTo(USER_ID);
			assertThat(status.getString("userName")).isEqualTo("asha");
			assertThat(status.getInt("failedAttempts")).isEqualTo(2);
			assertThat(status.getInt("statusID")).isEqualTo(1);
			assertThat(status.getBoolean("isLocked")).isFalse();
			assertThat(status.getBoolean("isLockedDueToFailedAttempts")).isFalse();
			assertThat(status.getBoolean("lockExpired")).isFalse();
			assertThat(status.isNull("lockTimestamp")).isTrue();
			assertThat(status.isNull("remainingTime")).isTrue();
		}

		@Test
		@DisplayName("reports no failed attempts when the counter has never been set")
		void reportsZeroFailedAttempts() throws Exception {
			userExists(user(false, null, null));

			assertThat(new JSONObject(service.getUserLockStatusJson(USER_ID)).getInt("failedAttempts")).isZero();
		}

		@Test
		@DisplayName("reports a live lock with the time remaining and the unlock time")
		void reportsALiveLock() throws Exception {
			userExists(user(true, hoursAgo(2), 5));

			JSONObject status = new JSONObject(service.getUserLockStatusJson(USER_ID));

			assertThat(status.getBoolean("isLocked")).isTrue();
			assertThat(status.getBoolean("isLockedDueToFailedAttempts")).isTrue();
			assertThat(status.getBoolean("lockExpired")).isFalse();
			assertThat(status.getString("remainingTime")).contains("hours");
			assertThat(status.getString("unlockTime")).isNotBlank();
			assertThat(status.getString("lockTimestamp")).isNotBlank();
		}

		@Test
		@DisplayName("reports an expired lock as awaiting the next login")
		void reportsAnExpiredLock() throws Exception {
			userExists(user(true, hoursAgo(LOCK_DURATION_HOURS + 1), 5));

			JSONObject status = new JSONObject(service.getUserLockStatusJson(USER_ID));

			assertThat(status.getBoolean("lockExpired")).isTrue();
			assertThat(status.getString("remainingTime")).isEqualTo("Lock expired - will unlock on next login");
			assertThat(status.has("unlockTime")).isFalse();
		}

		@Test
		@DisplayName("reports an administrator deactivation as locked but not by failed attempts")
		void reportsADeactivatedAccount() throws Exception {
			userExists(user(true, null, 0));

			JSONObject status = new JSONObject(service.getUserLockStatusJson(USER_ID));

			assertThat(status.getBoolean("isLocked")).isTrue();
			assertThat(status.getBoolean("isLockedDueToFailedAttempts")).isFalse();
		}

		@Test
		@DisplayName("refuses to report on a user that does not exist")
		void refusesUnknownUser() {
			userExists(null);

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> service.getUserLockStatusJson(USER_ID))
					.withMessageContaining("User not found with ID: 42");
		}

		@Test
		@DisplayName("reports a repository failure as a lookup failure")
		void reportsRepositoryFailure() {
			when(iEMRUserRepositoryCustom.findById(anyLong())).thenThrow(new IllegalStateException("db down"));

			assertThatExceptionOfType(IEMRException.class)
					.isThrownBy(() -> service.getUserLockStatusJson(USER_ID))
					.withMessageContaining("Error fetching user lock status");
		}

		@ParameterizedTest(name = "{0} hours into a {1} hour lock reports {2}")
		@CsvSource({ "1, 24, hours", "12, 24, hours", "22, 24, hours" })
		@DisplayName("the remaining time is reported in the largest unit that still applies")
		void formatsRemainingTime(int elapsedHours, int lockHours, String expectedUnit) throws Exception {
			ReflectionTestUtils.setField(service, "accountLockDurationHours", lockHours);
			userExists(user(true, hoursAgo(elapsedHours), 5));

			assertThat(new JSONObject(service.getUserLockStatusJson(USER_ID)).getString("remainingTime"))
					.contains(expectedUnit);
		}

		@Test
		@DisplayName("a lock with under an hour left is reported in minutes")
		void formatsMinutes() throws Exception {
			ReflectionTestUtils.setField(service, "accountLockDurationHours", 1);
			userExists(user(true, new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30)), 5));

			assertThat(new JSONObject(service.getUserLockStatusJson(USER_ID)).getString("remainingTime"))
					.contains("minutes");
		}
	}

	@Nested
	@DisplayName("hasAdminPrivileges")
	class AdminPrivileges {

		private void rolesFor(String... roleNames) {
			Set<Object[]> resultSet = new HashSet<>();
			for (String roleName : roleNames) {
				// The projection the service reads: provider service map, service, role, then
				// the remaining columns it copies onto the mapping.
				Object[] row = new Object[20];
				row[0] = 1;
				row[1] = "Service";
				row[2] = 1;
				row[3] = roleName;
				resultSet.add(row);
			}
			when(userRoleMappingRepository.getUserRoleMappingForUser(USER_ID)).thenReturn(resultSet);
		}

		@ParameterizedTest(name = "role \"{0}\" is an administrator")
		@ValueSource(strings = { "Admin", "admin", " SUPERVISOR ", "ProviderAdmin" })
		@DisplayName("recognises the administrative roles")
		void recognisesAdminRoles(String roleName) throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doReturn(java.util.List.of(mappingWithRole(roleName))).when(spy)
					.getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isTrue();
		}

		@Test
		@DisplayName("does not treat an ordinary role as administrative")
		void ordinaryRoleIsNotAdmin() throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doReturn(java.util.List.of(mappingWithRole("ANM"))).when(spy)
					.getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isFalse();
		}

		@Test
		@DisplayName("reports no privileges when the user has no role mapping")
		void noRoleMappings() throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doReturn(java.util.List.of()).when(spy).getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isFalse();
		}

		@Test
		@DisplayName("reports no privileges when the role mapping is unavailable")
		void nullRoleMappings() throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doReturn(null).when(spy).getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isFalse();
		}

		@Test
		@DisplayName("reports no privileges when a mapping carries no role")
		void mappingWithoutARole() throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doReturn(java.util.List.of(new com.iemr.common.data.users.UserServiceRoleMapping()))
					.when(spy).getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isFalse();
		}

		@Test
		@DisplayName("reports no privileges when the role lookup fails")
		void roleLookupFailure() throws Exception {
			IEMRAdminUserServiceImpl spy = org.mockito.Mockito.spy(service);
			org.mockito.Mockito.doThrow(new IEMRException("Contact Administrator")).when(spy)
					.getUserServiceRoleMapping(USER_ID);

			assertThat(spy.hasAdminPrivileges(USER_ID)).isFalse();
		}

		private com.iemr.common.data.users.UserServiceRoleMapping mappingWithRole(String roleName) {
			com.iemr.common.data.users.UserServiceRoleMapping mapping =
					new com.iemr.common.data.users.UserServiceRoleMapping();
			com.iemr.common.data.users.Role role = new com.iemr.common.data.users.Role();
			for (String field : new String[] { "roleName", "RoleName" }) {
				try {
					ReflectionTestUtils.setField(role, field, roleName);
				} catch (IllegalArgumentException ignored) {
					// the entity does not declare this spelling
				}
			}
			ReflectionTestUtils.setField(mapping, "m_Role", role);
			return mapping;
		}
	}
}
