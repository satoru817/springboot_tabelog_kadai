CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- Example values: ROLE_PAID_USER, ROLE_UNPAID_USER, ROLE_ADMIN
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
    enabled BOOLEAN DEFAULT FALSE,
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
    user_id INT NOT NULL UNIQUE,
    stripe_subscription_id VARCHAR(255) NOT NULL,
    card_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_card_id FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS restaurants (
    restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    postal_code VARCHAR(50) NOT NULL,
);

CREATE TABLE  IF NOT EXISTS category_restaurants(--１つのレストランが複数のカテゴリーを持てるようにする
    category_restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    category_id INT NOT NULL,
    UNIQUE(restaurant_id,category_id),
    FOREIGN KEY(restaurant_id) REFERENCES restaurants(restaurant_id),
    FOREIGN KEY(category_id) REFERENCES category(category_id)
);

CREATE TABLE restaurant_images (--１つのレストランが複数のイメージを持てるようにする。
    restaurant_image_id INT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id INT NOT NULL,
    image_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id)
);


CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    user_id INT NOT NULL,
    date DATETIME NOT NULL,
    number_of_people INT NOT NULL,
    comment VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    star_count INT NOT NULL,
    content VARCHAR(255),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

CREATE TABLE IF NOT EXISTS company_info (
    company_info_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    postal_code VARCHAR(50),
    address VARCHAR(255),
    phone_number VARCHAR(50),
    description VARCHAR(2047),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
