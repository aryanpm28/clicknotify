CREATE DATABASE IF NOT EXISTS clicknotify;
USE clicknotify;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telegram_chat_id VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    notification_status VARCHAR(50) DEFAULT 'PENDING',
    created_at DATETIME
);
