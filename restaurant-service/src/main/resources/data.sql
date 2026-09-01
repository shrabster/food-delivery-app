INSERT INTO restaurants (id, name, address) VALUES
    (1, 'Golden Dragon', '12 Bamboo St'),
    (2, 'Pasta Bella', '48 Trattoria Ave')
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, price) VALUES
    (1, 1, 'Kung Pao Chicken', 11.50),
    (2, 1, 'Spring Rolls', 5.00),
    (3, 1, 'Fried Rice', 7.25),
    (4, 2, 'Spaghetti Carbonara', 13.00),
    (5, 2, 'Margherita Pizza', 10.50),
    (6, 2, 'Tiramisu', 6.00)
ON CONFLICT (id) DO NOTHING;
