-- Usuario de prueba para la tienda SneakerX
-- Contraseña: password123
INSERT INTO users (username, password, email, phone, address, comuna, rut, imagen_url, created_at, role)
VALUES
    ('admin', '$2b$10$2HuRwdrB8PBr5wiKU/qmz.gemEa1N2A2Ydz4jTV0V.xfkjwxyot0.', 'admin@sneakerx.com', '+56911112222', 'Av. Libertador Bernardo O''Higgins 1950', 'Santiago', '11.111.111-1', NULL, NOW(), 'ADMIN'),
    ('carlos_sneaker', '$2b$10$2HuRwdrB8PBr5wiKU/qmz.gemEa1N2A2Ydz4jTV0V.xfkjwxyot0.', 'carlos@sneakerx.com', '+56922223333', 'Av. Providencia 1234', 'Providencia', '22.222.222-2', NULL, NOW(), 'USER'),
    ('maria_runner', '$2b$10$2HuRwdrB8PBr5wiKU/qmz.gemEa1N2A2Ydz4jTV0V.xfkjwxyot0.', 'maria@sneakerx.com', '+56933334444', 'Av. Apoquindo 5678', 'Las Condes', '33.333.333-3', NULL, NOW(), 'USER');
