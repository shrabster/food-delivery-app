INSERT INTO couriers (id, name, available) VALUES
    (1, 'Sam Rivera', true),
    (2, 'Jamie Chen', true),
    (3, 'Alex Okafor', true)
ON CONFLICT (id) DO NOTHING;
