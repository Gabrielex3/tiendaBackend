ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'VENDEDOR', 'ADMIN') NOT NULL DEFAULT 'USER';

INSERT INTO users (username, password, email, phone, address, comuna, rut, imagen_url, created_at, role)
VALUES ('vendedor', '$2b$10$2HuRwdrB8PBr5wiKU/qmz.gemEa1N2A2Ydz4jTV0V.xfkjwxyot0.', 'vendedor@sneakerx.com', '+56944445555', 'Av. Irarrázaval 2500', 'Ñuñoa', '44.444.444-4', NULL, NOW(), 'VENDEDOR');