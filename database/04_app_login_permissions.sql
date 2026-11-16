-- Snap & Pack: database permissions. Run as an administrator, with the newest tables already created.
-- It is safe to run again, and MUST be run again after any script that adds tables
-- (after 03_security_and_settings.sql and again after 05_roles_payments_notifications.sql).
-- Scripts from 06 onwards give the permissions for their own tables themselves.
--
-- It creates two database roles and puts the backend's login (clearance_app) in the first:
--
--   shop_app_role       what the running website needs, table by table, and nothing more:
--                         * accounts, orders and payments can never be deleted
--                         * a saved order line's product name, price and quantity can never be changed
--                         * the activity record can only be added to and read, never changed or erased
--                         * no creating, altering or dropping tables
--   shop_readonly_role  for reports: can only read products, orders, order lines and payments.
--                       It cannot see accounts, password hashes or reset links.
--
-- If your backend uses a different login, replace clearance_app everywhere below before running.
-- Creating tables stays with the administrator login you run these scripts with; the website's
-- login is never used for that.

USE SnapAndPack;
GO

IF DATABASE_PRINCIPAL_ID('shop_app_role') IS NULL
    CREATE ROLE shop_app_role;
IF DATABASE_PRINCIPAL_ID('shop_readonly_role') IS NULL
    CREATE ROLE shop_readonly_role;
GO

-- The login needs a user inside this database. (Skipped if it already has one.)
IF DATABASE_PRINCIPAL_ID('clearance_app') IS NULL
    CREATE USER clearance_app FOR LOGIN clearance_app;
GO

-- Take away the broad roles, if the login still has them.
IF IS_ROLEMEMBER('db_datareader', 'clearance_app') = 1
    ALTER ROLE db_datareader DROP MEMBER clearance_app;
IF IS_ROLEMEMBER('db_datawriter', 'clearance_app') = 1
    ALTER ROLE db_datawriter DROP MEMBER clearance_app;
IF IS_ROLEMEMBER('db_owner', 'clearance_app') = 1
    ALTER ROLE db_owner DROP MEMBER clearance_app;
GO

-- ---- what the website may do ----

-- Accounts: read, create, update. Never delete (accounts are switched off instead).
GRANT SELECT, INSERT, UPDATE ON dbo.users TO shop_app_role;

-- Products and their colours and sizes: full control, because staff add, edit and remove them.
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.products TO shop_app_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product_colors TO shop_app_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product_sizes TO shop_app_role;

-- Orders: read, create, update the status. Never delete.
GRANT SELECT, INSERT, UPDATE ON dbo.orders TO shop_app_role;

-- Order lines: read and create only. The one column that may change is the link to the product,
-- which the database clears when a product is deleted. Everything else on a saved line is fixed.
GRANT SELECT, INSERT ON dbo.order_items TO shop_app_role;
GRANT UPDATE (product_id) ON dbo.order_items TO shop_app_role;

-- Password reset links: created, read, and removed once used or replaced.
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.password_reset_tokens TO shop_app_role;

-- Site text: read, create, update.
GRANT SELECT, INSERT, UPDATE ON dbo.site_settings TO shop_app_role;

-- Activity record: add and read only.
GRANT SELECT, INSERT ON dbo.audit_log TO shop_app_role;

-- Payments: read, create, update the result. Never delete.
IF OBJECT_ID('dbo.payments') IS NOT NULL
    GRANT SELECT, INSERT, UPDATE ON dbo.payments TO shop_app_role;

-- Notifications: read, create, mark as seen.
IF OBJECT_ID('dbo.notifications') IS NOT NULL
    GRANT SELECT, INSERT, UPDATE ON dbo.notifications TO shop_app_role;

-- Email confirmation links: created, read, removed once used or replaced.
IF OBJECT_ID('dbo.email_verification_tokens') IS NOT NULL
    GRANT SELECT, INSERT, DELETE ON dbo.email_verification_tokens TO shop_app_role;

-- Recovery codes for two-step sign-in: created, read, marked as used, removed when reset.
IF OBJECT_ID('dbo.mfa_recovery_codes') IS NOT NULL
    GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.mfa_recovery_codes TO shop_app_role;
GO

-- ---- what a reporting login may do ----
GRANT SELECT ON dbo.products TO shop_readonly_role;
GRANT SELECT ON dbo.orders TO shop_readonly_role;
GRANT SELECT ON dbo.order_items TO shop_readonly_role;
IF OBJECT_ID('dbo.payments') IS NOT NULL
    GRANT SELECT ON dbo.payments TO shop_readonly_role;
GO

-- Put the website's login in its role.
ALTER ROLE shop_app_role ADD MEMBER clearance_app;
GO
