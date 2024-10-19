-- Insert roles (管理者、無料会員、有料会員)
INSERT IGNORE INTO roles (id,name) VALUES (1,'ROLE_ADMIN');
INSERT IGNORE INTO roles (id,name) VALUES (2,'ROLE_PAID_USER');
INSERT IGNORE INTO roles (id,name) VALUES (3,'ROLE_UNPAID_USER');

-- Insert users (各ユーザーは異なるrole_idを持つ)
-- 'password123'を10回のBcryptでハッシュ化したものを例として使用

INSERT IGNORE INTO users (user_id, role_id, name, name_for_reservation, password, email, postal_code, address, phone_number, enabled)
VALUES
(1, 1, 'admin1', 'Admin One', '$2a$10$jknqQssxT9J5n6Q0nqimFeXh.Pqlu1.8vOhj8RBL.emt5XyW4X0c2', 'admin1@example.com', '1234567', '123 Admin Street', '012-3456-7890', true),
(2, 1, 'admin2', 'Admin Two', '$2a$10$3x7OrzhcGp4wDooryoMOT.o.9gSjzNj/jo9oy7ZayhMb14sBFfs1e', 'admin2@example.com', '2345678', '234 Admin Street', '012-3456-7891', true),
(3, 2, 'user1', 'User One', '$2a$10$3ZAlgOq2/DnbquZ6aheVEuzO7Sth3tW11BNAeM3zk5zGXnDxDhKVm', 'user1@example.com', '3456789', '345 User Lane', '012-3456-7892', true),
(4, 2, 'user2', 'User Two', '$2a$10$Jv2kmj95PePZJBKH.bALh.flBU1lc7KUnfSI8fRsXuuCtoUVmDBtC', 'user2@example.com', '4567890', '456 User Lane', '012-3456-7893', true),
(5, 2, 'user3', NULL, '$2a$10$kpv.hWQQN6v0kkQ8zW9y1uVVvrmL8DybTMGR6hM3XdT5iZyWG7kvu', 'user3@example.com', '5678901', '567 User Lane', '012-3456-7894', true),
(6, 3, 'paid1', 'Paid User One', '$2a$10$L1Jet8x/i5B9/OIs3RroReI.nN7JFlPWDe11hH22IHj20/66pwziq', 'paid1@example.com', '6789012', '678 Paid Street', '012-3456-7895', true),
(7, 3, 'paid2', 'Paid User Two', '$2a$10$KAugbx2zGHKjWWAtLnL9dOpeyi1UyfY5rXRxpaj1E8kw7aWBMm7hK', 'paid2@example.com', '7890123', '789 Paid Street', '012-3456-7896', true),
(8, 3, 'paid3', NULL, '$2a$10$aWHRsrkKXEJCrwiARO34ue3wrIpm2Npca/1k1CNQ0xAHf7nzFAQXG', 'paid3@example.com', '8901234', '890 Paid Street', '012-3456-7897', true),
(9, 2, 'user4', 'User Four', '$2a$10$grD31u7s0L1t78SVWanVfeVD6k8EQj88Jzrka.JiwFTmy9JvqS.MS', 'user4@example.com', '9012345', '901 User Lane', '012-3456-7898', true),
(10, 1, 'admin3', NULL, '$2a$10$02.Bd7VMHJ1Y96z4q..d0.IFZdYgpNMtWe4cxB5HVKFsGAIW5PSly', 'admin3@example.com', '0123456', '012 Admin Street', '012-3456-7899', true);
