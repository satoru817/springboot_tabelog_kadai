CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL -- Example values: ROLE_PAID_USER, ROLE_UNPAID_USER, ROLE_ADMIN
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL, -- 管理者、無料会員、有料会員
    name VARCHAR(255) NOT NULL UNIQUE,
    name_for_reservation VARCHAR(50), -- NULLを許す。
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    stripe_customer_id VARCHAR(255),
    postal_code VARCHAR(10),
    address VARCHAR(255),
    phone_number VARCHAR(20),
    enabled BOOLEAN,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
    user_id INT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_user_id FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS login_attempts (
    login_attempt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    attempt_count INT DEFAULT 0,
    last_attempt TIMESTAMP,
    blocked_until TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cards (
    card_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    stripe_card_id VARCHAR(255) NOT NULL,
    brand VARCHAR(50),
    last4 VARCHAR(4),
    exp_month TINYINT,
    exp_year SMALLINT,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (user_id, stripe_card_id),
    CONSTRAINT fk_card_user_id FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    stripe_subscription_id VARCHAR(255) NOT NULL,
    card_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_card_id FOREIGN KEY (card_id) REFERENCES cards(card_id)
);

