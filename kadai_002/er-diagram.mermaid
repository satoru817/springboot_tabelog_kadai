erDiagram
    roles ||--o{ users : has
    users ||--o{ verification_tokens : has
    users ||--o{ cards : has
    users ||--o{ subscriptions : has
    users ||--o{ favorites : creates
    users ||--o{ reservations : makes
    
    restaurants ||--o{ category_restaurants : has
    categories ||--o{ category_restaurants : belongs_to
    restaurants ||--o{ restaurant_images : has
    restaurants ||--o{ favorites : has
    restaurants ||--o{ reservations : receives
    
    reservations ||--o| reviews : has
    reviews ||--o{ review_photos : has

    login_attempts {
        BIGINT login_attempt_id PK
        VARCHAR ip_address
        INT attempt_count
        TIMESTAMP last_attempt
        TIMESTAMP blocked_until
    }

    roles {
        INT id PK
        VARCHAR name
    }

    users {
        INT user_id PK
        INT role_id FK
        VARCHAR name
        VARCHAR name_for_reservation
        VARCHAR password
        VARCHAR email
        VARCHAR stripe_customer_id
        VARCHAR postal_code
        VARCHAR address
        VARCHAR phone_number
        VARCHAR profile_image
        BOOLEAN enabled
        DATETIME created_at
        DATETIME updated_at
    }

    verification_tokens {
        INT user_id FK
        VARCHAR token
        DATETIME created_at
        DATETIME updated_at
    }

    cards {
        BIGINT card_id PK
        INT user_id FK
        VARCHAR stripe_card_id
        VARCHAR brand
        VARCHAR last4
        TINYINT exp_month
        SMALLINT exp_year
        BOOLEAN is_default
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    subscriptions {
        BIGINT subscription_id PK
        INT user_id FK
        VARCHAR stripe_subscription_id
        BIGINT card_id FK
        VARCHAR status
        DATE start_date
        DATE end_date
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    categories {
        INT category_id PK
        VARCHAR category_name
    }

    restaurants {
        INT restaurant_id PK
        VARCHAR name
        VARCHAR description
        INT capacity
        VARCHAR email
        VARCHAR postal_code
        VARCHAR address
        VARCHAR phone_number
        TIME monday_opening_time
        TIME monday_closing_time
        TIME tuesday_opening_time
        TIME tuesday_closing_time
        TIME wednesday_opening_time
        TIME wednesday_closing_time
        TIME thursday_opening_time
        TIME thursday_closing_time
        TIME friday_opening_time
        TIME friday_closing_time
        TIME saturday_opening_time
        TIME saturday_closing_time
        TIME sunday_opening_time
        TIME sunday_closing_time
    }

    category_restaurants {
        INT category_restaurant_id PK
        INT restaurant_id FK
        INT category_id FK
    }

    restaurant_images {
        INT restaurant_image_id PK
        INT restaurant_id FK
        VARCHAR image_name
    }

    favorites {
        INT favorite_id PK
        INT restaurant_id FK
        INT user_id FK
        DATETIME created_at
        DATETIME updated_at
    }

    reservations {
        INT reservation_id PK
        INT restaurant_id FK
        INT user_id FK
        DATETIME date
        INT number_of_people
        VARCHAR comment
        DATETIME created_at
        DATETIME updated_at
    }

    reviews {
        INT review_id PK
        INT reservation_id FK
        INT star_count
        VARCHAR content
    }

    review_photos {
        INT review_photo_id PK
        INT review_id FK
        VARCHAR image_name
    }

    company_info {
        INT company_info_id PK
        VARCHAR name
        VARCHAR postal_code
        VARCHAR address
        VARCHAR phone_number
        VARCHAR description
        DATETIME created_at
        DATETIME updated_at
    }
