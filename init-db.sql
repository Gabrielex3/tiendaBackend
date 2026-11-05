CREATE DATABASE IF NOT EXISTS tienda_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tienda_productos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tienda_pagos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS tienda_contacto CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON tienda_usuarios.* TO 'tienda_app'@'%';
GRANT ALL PRIVILEGES ON tienda_productos.* TO 'tienda_app'@'%';
GRANT ALL PRIVILEGES ON tienda_pagos.* TO 'tienda_app'@'%';
GRANT ALL PRIVILEGES ON tienda_contacto.* TO 'tienda_app'@'%';
FLUSH PRIVILEGES;