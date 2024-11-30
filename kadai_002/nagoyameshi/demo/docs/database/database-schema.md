```mermaid


erDiagram
    users ||--o{ cards : has
    users ||--o| subscriptions : has
    users ||--o{ favorites : creates
    users ||--o{ reservations : makes
    users ||--o{ reviews : writes
    users }|--|| roles : has
    users ||--o| verification_tokens : has

    restaurants ||--o{ favorites : receives
    restaurants ||--o{ reservations : receives
    restaurants ||--o{ restaurant_images : has
    restaurants ||--o{ category_restaurants : has
    
    categories ||--o{ category_restaurants : belongs_to

    reservations ||--o| reviews : has
    reviews ||--o{ review_photos : has

    cards ||--o{ subscriptions : used_in
    cards ||--o{ payments : used_in

    users ||--o{ payments : makes

    users {
        int user_id PK
        int role_id FK
        string name "NOT NULL UNIQUE"
        string name_for_reservation "NOT NULL"
        string password "NOT NULL"
        string email "NOT NULL UNIQUE"
        string stripe_customer_id
        string postal_code
        string address
        string phone_number
        string profile_image
        boolean enabled "DEFAULT FALSE"
        datetime created_at "NOT NULL DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "NOT NULL DEFAULT CURRENT_TIMESTAMP"
    }

    roles {
        int id PK
        string name "NOT NULL UNIQUE"
    }

    verification_tokens {
        int user_id "FK NOT NULL UNIQUE"
        string token "NOT NULL"
        datetime created_at "NOT NULL DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    login_attempts {
        bigint login_attempt_id PK
        string ip_address "NOT NULL"
        int attempt_count "DEFAULT 0"
        timestamp last_attempt
        timestamp blocked_until
    }

    cards {
        bigint card_id PK
        int user_id "FK NOT NULL"
        string stripe_card_id "NOT NULL"
        string brand
        string last4
        tinyint exp_month
        smallint exp_year
        boolean is_default "DEFAULT FALSE"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
        timestamp updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    subscriptions {
        bigint subscription_id PK
        int user_id "FK NOT NULL UNIQUE"
        string stripe_subscription_id "NOT NULL"
        bigint card_id "FK NOT NULL"
        string status "NOT NULL"
        date start_date
        date end_date
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
        timestamp updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    categories {
        int category_id PK
        string category_name "NOT NULL"
    }

    restaurants {
        int restaurant_id PK
        string name "NOT NULL"
        string description "NOT NULL"
        int capacity "NOT NULL"
        string email "NOT NULL UNIQUE"
        string postal_code "NOT NULL"
        string address "NOT NULL"
        string phone_number "NOT NULL UNIQUE"
        time monday_opening_time
        time monday_closing_time
        time tuesday_opening_time
        time tuesday_closing_time
        time wednesday_opening_time
        time wednesday_closing_time
        time thursday_opening_time
        time thursday_closing_time
        time friday_opening_time
        time friday_closing_time
        time saturday_opening_time
        time saturday_closing_time
        time sunday_opening_time
        time sunday_closing_time
    }

    category_restaurants {
        int category_restaurant_id PK
        int restaurant_id "FK NOT NULL"
        int category_id "FK NOT NULL"
    }

    restaurant_images {
        int restaurant_image_id PK
        int restaurant_id "FK NOT NULL"
        string image_name "NOT NULL"
    }

    favorites {
        int favorite_id PK
        int restaurant_id "FK NOT NULL"
        int user_id "FK NOT NULL"
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    reservations {
        int reservation_id PK
        int restaurant_id "FK NOT NULL"
        int user_id "FK NOT NULL"
        datetime date "NOT NULL"
        int number_of_people "NOT NULL"
        string comment
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    reviews {
        int review_id PK
        int reservation_id "FK NOT NULL"
        int star_count "NOT NULL"
        string content
        boolean is_visible "DEFAULT TRUE"
        string hidden_reason
        datetime hidden_at
        int hidden_by "FK"
    }

    review_photos {
        int review_photo_id PK
        int review_id "FK NOT NULL"
        string image_name "NOT NULL"
    }

    company_info {
        int company_info_id PK
        string name "NOT NULL"
        string postal_code
        string address
        string phone_number
        string description
        datetime created_at "DEFAULT CURRENT_TIMESTAMP"
        datetime updated_at "DEFAULT CURRENT_TIMESTAMP"
    }

    payments {
        bigint payment_id PK
        int user_id "FK NOT NULL"
        int amount "NOT NULL"
        string currency "NOT NULL DEFAULT 'JPY'"
        string stripe_payment_intent_id "NOT NULL UNIQUE"
        bigint card_id "FK"
        string status "NOT NULL"
        string description
        json metadata
        string error_message
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
        timestamp updated_at "DEFAULT CURRENT_TIMESTAMP"
    }
