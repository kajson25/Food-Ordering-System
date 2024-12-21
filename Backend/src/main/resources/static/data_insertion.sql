-- Inserting Users (Characters from Naruto)
INSERT INTO users (first_name, last_name, email, password, is_admin) VALUES
                                                                         ('Naruto', 'Uzumaki', 'naruto@konoha.com', 'Rasengan123', FALSE),
                                                                         ('Sasuke', 'Uchiha', 'sasuke@konoha.com', 'Sharingan456', FALSE),
                                                                         ('Sakura', 'Haruno', 'sakura@konoha.com', 'Healing789', FALSE),
                                                                         ('Kakashi', 'Hatake', 'kakashi@konoha.com', 'Chidori000', TRUE),
                                                                         ('Shikamaru', 'Nara', 'shikamaru@konoha.com', 'ShadowBind321', FALSE);

-- Inserting Dishes
INSERT INTO dishes (name, description, price) VALUES
                                                  ('Ramen', 'Delicious bowl of miso ramen', 10.99),
                                                  ('Dango', 'Sweet rice dumplings on a stick', 3.50),
                                                  ('Onigiri', 'Rice ball with seaweed and filling', 2.99),
                                                  ('Soba Noodles', 'Buckwheat noodles in soup', 8.75),
                                                  ('Tempura', 'Deep-fried battered vegetables and shrimp', 12.00);

-- Inserting Orders
INSERT INTO orders (status, created_by, active) VALUES
                                                    ('ORDERED', 1, TRUE), -- Naruto's order
                                                    ('PREPARING', 2, TRUE), -- Sasuke's order
                                                    ('DELIVERED', 3, FALSE), -- Sakura's order
                                                    ('CANCELED', 4, FALSE); -- Kakashi's order

-- Inserting Items (Order-Dish relationships)
INSERT INTO items (order_id, dish_id, quantity) VALUES
                                                    (1, 1, 2), -- Naruto ordered 2 bowls of Ramen
                                                    (1, 2, 3), -- Naruto ordered 3 Dango
                                                    (2, 3, 5), -- Sasuke ordered 5 Onigiri
                                                    (3, 4, 1), -- Sakura ordered 1 Soba Noodle
                                                    (4, 5, 2); -- Kakashi ordered 2 Tempura

-- Inserting Error Messages
INSERT INTO error_messages (date, order_id, operation, message) VALUES
                                                                    ('2024-12-21', 4, 'CANCEL', 'Order canceled due to payment failure'),
                                                                    ('2024-12-20', 3, 'DELIVER', 'Order delivered successfully'),
                                                                    ('2024-12-19', 2, 'PREPARE', 'Order is being prepared');
