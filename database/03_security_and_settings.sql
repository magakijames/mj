-- Snap & Pack: step 3. Run this once, as an administrator, with the SnapAndPack database selected,
-- after 02_schema.sql. It is safe for a database that already holds products, accounts and orders.
--
-- It adds:
--   * sign-in protection columns on users (wrong-password count, temporary lock, password change time)
--   * password_reset_tokens  one-time "forgot password" links (only a hash of each link is stored)
--   * site_settings          the site text the admin can edit
--   * audit_log              a record of who changed what
--   * extra rules (CHECK constraints) so bad data is refused by the database itself

-- ---- users: sign-in protection ----
ALTER TABLE users ADD failed_logins INT NOT NULL DEFAULT 0 CHECK (failed_logins >= 0);
ALTER TABLE users ADD locked_until DATETIME2 NULL;
ALTER TABLE users ADD password_changed_at DATETIME2 NULL;

-- ---- extra rules on the existing tables ----
ALTER TABLE users ADD CONSTRAINT ck_users_name_not_empty CHECK (name <> '');
ALTER TABLE users ADD CONSTRAINT ck_users_email_shape CHECK (email LIKE '%_@_%' AND email NOT LIKE '% %');
ALTER TABLE users ADD CONSTRAINT ck_users_password_hash CHECK (password_hash LIKE '$2%');
ALTER TABLE products ADD CONSTRAINT ck_products_name_not_empty CHECK (name <> '');
ALTER TABLE product_colors ADD CONSTRAINT ck_product_colors_not_empty CHECK (color <> '');
ALTER TABLE product_sizes ADD CONSTRAINT ck_product_sizes_not_empty CHECK (size <> '');
ALTER TABLE orders ADD CONSTRAINT ck_orders_total CHECK (total >= 0);
ALTER TABLE orders ADD CONSTRAINT ck_orders_phone_not_empty CHECK (phone <> '');
ALTER TABLE orders ADD CONSTRAINT ck_orders_address_not_empty CHECK (address <> '');
ALTER TABLE order_items ADD CONSTRAINT ck_order_items_quantity_max CHECK (quantity <= 99);
ALTER TABLE order_items ADD CONSTRAINT ck_order_items_unit_price CHECK (unit_price >= 0);

-- ---- one-time password reset links ----
CREATE TABLE password_reset_tokens (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    token_hash CHAR(64)  NOT NULL,
    expires_at DATETIME2 NOT NULL,
    used_at    DATETIME2 NULL,
    created_at DATETIME2 NOT NULL,
    CONSTRAINT uq_password_reset_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_password_reset_tokens_expiry CHECK (expires_at > created_at)
);
CREATE INDEX ix_password_reset_tokens_user ON password_reset_tokens (user_id);

-- ---- site text the admin can edit ----
CREATE TABLE site_settings (
    setting_key   NVARCHAR(50)  NOT NULL PRIMARY KEY,
    setting_value NVARCHAR(500) NOT NULL,
    updated_at    DATETIME2     NOT NULL
);

-- ---- activity record ----
CREATE TABLE audit_log (
    id          BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    created_at  DATETIME2     NOT NULL,
    actor_id    BIGINT        NULL,
    actor_email NVARCHAR(255) NULL,
    action      NVARCHAR(50)  NOT NULL,
    details     NVARCHAR(500) NULL,
    CONSTRAINT ck_audit_log_action_not_empty CHECK (action <> '')
);
CREATE INDEX ix_audit_log_created ON audit_log (created_at);
