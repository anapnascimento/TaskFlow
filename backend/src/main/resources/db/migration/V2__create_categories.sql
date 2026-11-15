CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    deleted_at DATETIME(6) NULL,
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_categories_user_id (user_id),
    UNIQUE INDEX uk_categories_user_active_name (user_id, (CASE WHEN deleted_at IS NULL THEN LOWER(name) END))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
