create table if not exists roles (
    id int not null auto_increment primary key,
    name varchar(50) not null-- Example values: ROLE_PAID_USER, ROLE_UNPAID_USER,ROLE_ADMIN
);

create table if not exists users (
    id int not null auto_increment primary key,
    role_id int not null,--管理者、無料会員、有料会員
    name varchar(255) not null unique,
    name_for_reservation varchar(50),--nullを許す。
    password varchar(255) not null,
    email varchar(255) not null unique,
    postal_code varchar(10),
    address varchar(255),
    phone_number varchar(20),
    enabled boolean not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    foreign key(role_id) references roles(id),
    unique(email), -- Ensure unique email addresses
    unique(name)
);

create table if not exists verification_tokens (
    user_id int not null unique,
    token varchar(255) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime default current_timestamp on update current_timestamp,
    foreign key (user_id) references users(id)
);

create table if not exists login_attempts(
    id bigint auto_increment primary key,
    ip_address varchar(45)not null,
    attempt_count int default 0,
    last_attempt timestamp,
    blocked_until timestamp
);
