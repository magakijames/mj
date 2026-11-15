-- Snap & Pack: step 5. Run this once, as an administrator, with the SnapAndPack database selected,
-- after 03_security_and_settings.sql. It is safe for a database that already holds data.
-- AFTER THIS SCRIPT, RUN 04_app_login_permissions.sql AGAIN, so the backend's login can use the new tables.
--
-- It adds:
--   * the new staff roles (SUPER_ADMIN, ADMIN, INVENTORY_MANAGER, ORDER_MANAGER, SUPPORT)
--   * room for the stronger Argon2id password hashes
--   * sign-out support (sessions_valid_after)
--   * payments        M-Pesa payment requests and their results
--   * notifications   messages shown to users inside the site

-- ---- roles: the old ADMIN becomes SUPER_ADMIN, the old PRODUCT_MANAGER becomes INVENTORY_MANAGER ----
ALTER TABLE users DROP CONSTRAINT ck_users_role;
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'ADMIN';
UPDATE users SET role = 'INVENTORY_MANAGER' WHERE role = 'PRODUCT_MANAGER';
ALTER TABLE users ADD CONSTRAINT ck_users_role
    CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'INVENTORY_MANAGER', 'ORDER_MANAGER', 'SUPPORT', 'CUSTOMER'));

-- ---- password hashes: Argon2id hashes are longer than the BCrypt ones stored so far ----
ALTER TABLE users DROP CONSTRAINT ck_users_password_hash;
ALTER TABLE users ALTER COLUMN password_hash NVARCHAR(255) NOT NULL;
ALTER TABLE users ADD CONSTRAINT ck_users_password_hash
    CHECK (password_hash LIKE '{argon2}%' OR password_hash LIKE '$2%');

-- ---- sign-out: tokens issued before this moment are refused ----
ALTER TABLE users ADD sessions_valid_after DATETIME2 NULL;

-- ---- orders: whether a confirmed payment has been received, and its M-Pesa receipt ----
ALTER TABLE orders ADD payment_status NVARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (payment_status IN ('UNPAID', 'PAID'));
ALTER TABLE orders ADD payment_reference NVARCHAR(50) NULL;

-- ---- M-Pesa payment requests ----
CREATE TABLE payments (
    id                  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_id            BIGINT         NOT NULL,
    amount              DECIMAL(12, 2) NOT NULL,
    phone               NVARCHAR(20)   NOT NULL,
    status              NVARCHAR(20)   NOT NULL,
    merchant_request_id NVARCHAR(100)  NULL,
    checkout_request_id NVARCHAR(100)  NOT NULL,
    mpesa_receipt       NVARCHAR(50)   NULL,
    result_code         INT            NULL,
    result_description  NVARCHAR(255)  NULL,
    created_at          DATETIME2      NOT NULL,
    completed_at        DATETIME2      NULL,
    -- One row per request sent to Safaricom, so the same report can never be applied to two payments.
    CONSTRAINT uq_payments_checkout_request UNIQUE (checkout_request_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT ck_payments_phone CHECK (phone LIKE '254%')
);
CREATE INDEX ix_payments_order ON payments (order_id);

-- ---- messages shown to users inside the site ----
CREATE TABLE notifications (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    title      NVARCHAR(150) NOT NULL,
    body       NVARCHAR(500) NOT NULL,
    link       NVARCHAR(300) NULL,
    read_at    DATETIME2     NULL,
    created_at DATETIME2     NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_notifications_title_not_empty CHECK (title <> '')
);
CREATE INDEX ix_notifications_user ON notifications (user_id, id);
