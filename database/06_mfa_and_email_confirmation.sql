-- Snap & Pack: step 6. Run this once, as an administrator, with the SnapAndPack database selected,
-- after 05_roles_payments_notifications.sql. It is safe for a database that already holds data.
-- This script also gives the website's login its permissions on the new tables (see the end),
-- so there is no need to run 04 again after it.
--
-- It adds:
--   * users.email_verified            whether the account's email address has been confirmed
--   * users.mfa_enabled / mfa_secret  two-step sign-in with an authenticator app
--   * email_verification_tokens       one-time "confirm your email" links (only a hash is stored)
--   * mfa_recovery_codes              one-time codes for signing in without the phone (only hashes)

-- Accounts that already exist are treated as confirmed: that is what DEFAULT 1 does here.
-- New accounts are always saved by the website with their real value (not confirmed).
ALTER TABLE users ADD email_verified BIT NOT NULL DEFAULT 1;

-- Two-step sign-in starts switched off for everyone.
ALTER TABLE users ADD mfa_enabled BIT NOT NULL DEFAULT 0;
ALTER TABLE users ADD mfa_secret NVARCHAR(255) NULL;
ALTER TABLE users ADD mfa_last_step BIGINT NULL;

CREATE TABLE email_verification_tokens (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    token_hash CHAR(64)  NOT NULL,
    expires_at DATETIME2 NOT NULL,
    created_at DATETIME2 NOT NULL,
    CONSTRAINT uq_email_verification_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_email_verification_tokens_expiry CHECK (expires_at > created_at)
);
CREATE INDEX ix_email_verification_tokens_user ON email_verification_tokens (user_id);

CREATE TABLE mfa_recovery_codes (
    id        BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    code_hash CHAR(64)  NOT NULL,
    used_at   DATETIME2 NULL,
    CONSTRAINT fk_mfa_recovery_codes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX ix_mfa_recovery_codes_user ON mfa_recovery_codes (user_id);

-- ==== PERMISSIONS (SQL Server) ====
GO
-- Gives the website's login what it needs on the two new tables. It finds the database user that
-- belongs to the login clearance_app, whatever that user is called, and also the shop_app_role
-- if script 04 created it. If your backend uses a different login, change the name on the next line.
DECLARE @login sysname = N'clearance_app';
DECLARE @user sysname;
SELECT @user = dp.name
FROM sys.database_principals AS dp
JOIN sys.server_principals AS sp ON sp.sid = dp.sid
WHERE sp.name = @login;

DECLARE @grants nvarchar(max) =
    N'GRANT SELECT, INSERT, DELETE ON dbo.email_verification_tokens TO {who}; ' +
    N'GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.mfa_recovery_codes TO {who};';
DECLARE @sql nvarchar(max);

IF @user IS NULL
    PRINT 'WARNING: the login ' + @login + ' has no user in this database, so no permissions were given to it.';
ELSE
BEGIN
    SET @sql = REPLACE(@grants, N'{who}', QUOTENAME(@user));
    EXEC (@sql);
    PRINT 'Permissions on the new tables given to ' + @user + '.';
END

IF DATABASE_PRINCIPAL_ID('shop_app_role') IS NOT NULL
BEGIN
    SET @sql = REPLACE(@grants, N'{who}', N'[shop_app_role]');
    EXEC (@sql);
    PRINT 'Permissions on the new tables given to shop_app_role.';
END
GO
