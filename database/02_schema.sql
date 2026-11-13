-- Snap & Pack: step 2. Run this once, with the SnapAndPack database selected
-- (in SSMS: pick SnapAndPack in the database drop-down, or run "USE SnapAndPack;" first).
-- It creates the tables. The Java backend fills in the first admin and the starting products
-- the first time it starts.

CREATE TABLE users (
    id            BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name          NVARCHAR(100) NOT NULL,
    email         NVARCHAR(255) NOT NULL,
    password_hash NVARCHAR(100) NOT NULL,
    role          NVARCHAR(30)  NOT NULL,
    active        BIT           NOT NULL,
    created_at    DATETIME2     NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'PRODUCT_MANAGER', 'ORDER_MANAGER', 'SUPPORT', 'CUSTOMER'))
);

CREATE TABLE products (
    id       BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name     NVARCHAR(150)  NOT NULL,
    category NVARCHAR(20)   NOT NULL,
    gender   NVARCHAR(20)   NOT NULL,
    price    DECIMAL(10, 2) NOT NULL,
    stock    INT            NOT NULL,
    image    NVARCHAR(500)  NULL,
    CONSTRAINT ck_products_category CHECK (category IN ('Bag', 'Cap')),
    CONSTRAINT ck_products_gender CHECK (gender IN ('Men', 'Women', 'Unisex', 'Kids')),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0)
);

CREATE TABLE product_colors (
    product_id BIGINT       NOT NULL,
    position   INT          NOT NULL,
    color      NVARCHAR(50) NOT NULL,
    CONSTRAINT pk_product_colors PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_colors_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE TABLE product_sizes (
    product_id BIGINT       NOT NULL,
    position   INT          NOT NULL,
    size       NVARCHAR(50) NOT NULL,
    CONSTRAINT pk_product_sizes PRIMARY KEY (product_id, position),
    CONSTRAINT fk_product_sizes_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

CREATE TABLE orders (
    id         BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    user_id    BIGINT         NOT NULL,
    phone      NVARCHAR(30)   NOT NULL,
    address    NVARCHAR(500)  NOT NULL,
    total      DECIMAL(12, 2) NOT NULL,
    status     NVARCHAR(20)   NOT NULL,
    created_at DATETIME2      NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE order_items (
    id           BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NULL,
    product_name NVARCHAR(150)  NOT NULL,
    color        NVARCHAR(50)   NOT NULL,
    size         NVARCHAR(50)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    -- Deleting a product keeps the order line; it just loses its link to the product.
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0)
);

CREATE INDEX ix_orders_user ON orders (user_id);
CREATE INDEX ix_order_items_order ON order_items (order_id);
