-- ============================================================
-- ShangLuHua (商陆花) 数据库参考 Schema
-- 注意: 此文件不会被 Spring Boot 自动执行 (sql.init.mode=never)
-- 表结构由 Hibernate ddl-auto=update 自动管理
-- 此文件仅作为参考文档和 IDE JPA 控制台的表名解析使用
-- ============================================================

-- 管理用户
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(256) NOT NULL,
    display_name VARCHAR(255),
    role VARCHAR(31) DEFAULT 'ADMIN',
    status VARCHAR(31) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 客户
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255),
    wechat VARCHAR(255),
    level_name VARCHAR(255) DEFAULT 'Wholesale',
    debt_balance DECIMAL(21,2) DEFAULT 0.00,
    status VARCHAR(31) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品 SPU
CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    category VARCHAR(255),
    season VARCHAR(255),
    supplier_name VARCHAR(255),
    retail_price DECIMAL(21,2) DEFAULT 0.00,
    wholesale_price DECIMAL(21,2) DEFAULT 0.00,
    cost_price DECIMAL(21,2) DEFAULT 0.00,
    image_url VARCHAR(255),
    status VARCHAR(31) DEFAULT 'ON_SALE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品 SKU
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    spu_id BIGINT NOT NULL,
    sku_code VARCHAR(96) NOT NULL UNIQUE,
    barcode VARCHAR(255),
    color_name VARCHAR(255),
    size_name VARCHAR(255),
    retail_price DECIMAL(21,2) DEFAULT 0.00,
    wholesale_price DECIMAL(21,2) DEFAULT 0.00,
    cost_price DECIMAL(21,2) DEFAULT 0.00,
    status VARCHAR(31) DEFAULT 'ON_SALE',
    FOREIGN KEY (spu_id) REFERENCES product_spu(id)
);

-- 采购单
CREATE TABLE IF NOT EXISTS purchase_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(255),
    warehouse_code VARCHAR(255) DEFAULT 'MAIN',
    supplier_name VARCHAR(255),
    total_cost DECIMAL(21,2) DEFAULT 0.00,
    status VARCHAR(31) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL
);

-- 采购单项
CREATE TABLE IF NOT EXISTS purchase_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(21,2) DEFAULT 0.00,
    amount DECIMAL(21,2) DEFAULT 0.00,
    FOREIGN KEY (order_id) REFERENCES purchase_order(id),
    FOREIGN KEY (sku_id) REFERENCES product_sku(id)
);

-- 销售单
CREATE TABLE IF NOT EXISTS sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(255),
    warehouse_code VARCHAR(255) DEFAULT 'MAIN',
    customer_id BIGINT,
    total_amount DECIMAL(21,2) DEFAULT 0.00,
    paid_amount DECIMAL(21,2) DEFAULT 0.00,
    debt_amount DECIMAL(21,2) DEFAULT 0.00,
    status VARCHAR(31) DEFAULT 'DRAFT',
    contact_name VARCHAR(255),
    contact_phone VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- 销售单项
CREATE TABLE IF NOT EXISTS sales_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    spu_code_snapshot VARCHAR(255),
    product_name_snapshot VARCHAR(255),
    color_snapshot VARCHAR(255),
    size_snapshot VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(21,2) DEFAULT 0.00,
    cost_price_snapshot DECIMAL(21,2) DEFAULT 0.00,
    amount DECIMAL(21,2) DEFAULT 0.00,
    FOREIGN KEY (order_id) REFERENCES sales_order(id),
    FOREIGN KEY (sku_id) REFERENCES product_sku(id)
);

-- 库存余额
CREATE TABLE IF NOT EXISTS inventory_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(255) DEFAULT 'MAIN',
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    locked_quantity INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (warehouse_code, sku_id),
    FOREIGN KEY (sku_id) REFERENCES product_sku(id)
);

-- 库存台账
CREATE TABLE IF NOT EXISTS inventory_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(255),
    sku_id BIGINT NOT NULL,
    biz_type VARCHAR(31),
    biz_id BIGINT,
    change_quantity INT NOT NULL,
    before_quantity INT NOT NULL,
    after_quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sku_id) REFERENCES product_sku(id)
);
