-- Snap & Pack: step 4. Run this once, as an administrator, after 03_security_and_settings.sql.
--
-- It gives the login the backend uses (clearance_app) the smallest set of rights the site needs,
-- table by table, in place of the broad "read everything / write everything" roles.
-- If a fault in the site were ever abused, this limits what could be done to the data:
--   * accounts and orders can never be deleted
--   * order lines can never be deleted, and their product, price and quantity can never be changed
--   * the activity record can only be added to and read, never changed or erased
--   * the login cannot create, alter or drop tables
--
-- If your backend uses a different login, replace clearance_app everywhere below before running.

USE SnapAndPack;
GO

-- The login needs a user inside this database. (Skipped if it already has one.)
IF DATABASE_PRINCIPAL_ID('clearance_app') IS NULL
    CREATE USER clearance_app FOR LOGIN clearance_app;
GO

-- Take away the broad roles given earlier.
ALTER ROLE db_datareader DROP MEMBER clearance_app;
ALTER ROLE db_datawriter DROP MEMBER clearance_app;
GO

-- Accounts: read, create, update. Never delete (accounts are switched off instead).
GRANT SELECT, INSERT, UPDATE ON dbo.users TO clearance_app;

-- Products and their colours and sizes: full control, because staff add, edit and remove them.
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.products TO clearance_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product_colors TO clearance_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.product_sizes TO clearance_app;

-- Orders: read, create, update the status. Never delete.
GRANT SELECT, INSERT, UPDATE ON dbo.orders TO clearance_app;

-- Order lines: read and create only. The one column that may change is the link to the product,
-- which the database clears when a product is deleted. Everything else on a saved line is fixed.
GRANT SELECT, INSERT ON dbo.order_items TO clearance_app;
GRANT UPDATE (product_id) ON dbo.order_items TO clearance_app;

-- Password reset links: created, read, and removed once used or replaced.
GRANT SELECT, INSERT, UPDATE, DELETE ON dbo.password_reset_tokens TO clearance_app;

-- Site text: read, create, update.
GRANT SELECT, INSERT, UPDATE ON dbo.site_settings TO clearance_app;

-- Activity record: add and read only.
GRANT SELECT, INSERT ON dbo.audit_log TO clearance_app;
GO
