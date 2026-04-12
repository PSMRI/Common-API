-- Add lock_timestamp column to m_user table for time-bound account locking
-- When a user gets locked after 5 failed login attempts, this stores when
-- the lock happened. After 24 hours, the account auto-unlocks on next login.

ALTER TABLE m_user ADD COLUMN lock_timestamp DATETIME NULL DEFAULT NULL;
