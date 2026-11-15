CREATE TABLE activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    metadata JSON NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_activity_logs_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    INDEX idx_activity_logs_user_created_at (user_id, created_at),
    INDEX idx_activity_logs_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
