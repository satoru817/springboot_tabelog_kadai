CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- Example values: ROLE_PAID_USER, ROLE_UNPAID_USER, ROLE_ADMIN
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    name_for_reservation VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    stripe_customer_id VARCHAR(255),
    postal_code VARCHAR(10),
    address VARCHAR(255),
    phone_number VARCHAR(20),
    profile_image VARCHAR(255),
    enabled BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT--ユーザーが削除されてもroleは削除しない
);

CREATE TABLE IF NOT EXISTS verification_tokens (
    user_id INT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
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
    CONSTRAINT fk_card_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
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
    CONSTRAINT fk_subscription_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_card_id FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE CASCADE
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
    email VARCHAR(255) NOT NULL UNIQUE,
    postal_code VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    monday_opening_time TIME,
    monday_closing_time TIME,
    tuesday_opening_time TIME,
    tuesday_closing_time TIME,
    wednesday_opening_time TIME,
    wednesday_closing_time TIME,
    thursday_opening_time TIME,
    thursday_closing_time TIME,
    friday_opening_time TIME,
    friday_closing_time TIME,
    saturday_opening_time TIME,
    saturday_closing_time TIME,
    sunday_opening_time TIME,
    sunday_closing_time TIME
);

CREATE TABLE IF NOT EXISTS category_restaurants (
    category_restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    category_id INT NOT NULL,
    UNIQUE(restaurant_id,category_id),
    CONSTRAINT fk_category_restaurant_restaurant_id FOREIGN KEY(restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_category_restaurant_category_id FOREIGN KEY(category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS restaurant_images (
    restaurant_image_id INT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id INT NOT NULL,
    image_name VARCHAR(255) NOT NULL,
    UNIQUE(restaurant_id,image_name),
    CONSTRAINT fk_restaurant_image_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS favorites (
    favorite_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (restaurant_id,user_id),
    CONSTRAINT fk_favorite_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    user_id INT NOT NULL,
    date DATETIME NOT NULL,
    number_of_people INT NOT NULL,
    comment VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    star_count INT NOT NULL,
    content VARCHAR(255),
    is_visible BOOLEAN DEFAULT TRUE,
    hidden_reason VARCHAR(255),
    hidden_at DATETIME,
    hidden_by INT,
    UNIQUE KEY(reservation_id),
    CHECK(star_count > 0 AND star_count < 6),
    CONSTRAINT fk_review_reservation_id FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_hidden_by FOREIGN KEY (hidden_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS review_photos (
    review_photo_id INT AUTO_INCREMENT PRIMARY KEY,
    review_id INT NOT NULL,
    image_name VARCHAR(255) NOT NULL,
    UNIQUE KEY(review_id,image_name),
    CONSTRAINT fk_review_photo_review_id FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE--レビューが削除されたら関連するreview_photoも削除
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

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount INT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'JPY',
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    card_id BIGINT,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    metadata JSON,
    error_message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,--決済記録のある顧客は削除できない
    CONSTRAINT fk_payment_card_id FOREIGN KEY (card_id) REFERENCES cards(card_id) ON DELETE SET NULL--カードが削除されても決済記録は保持
);