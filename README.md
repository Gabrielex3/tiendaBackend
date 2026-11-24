# 🛒 SneakerX - Backend de Tienda de Sneakers

Backend de una tienda de sneakers desarrollado con arquitectura de microservicios usando Spring Boot y Spring Cloud.

## 🏗️ Arquitectura del Sistema

```
                ┌─────────────┐
   Frontend ────→│  API Gateway │  (localhost:8082)
                │  lb://      │
                └──────┬──────┘
                       │ consulta
                ┌──────▼──────┐
                │  Eureka     │  (localhost:8761)
                │  Server     │
                └──┬──────┬───┘
                   │      │ registra
             ┌─────▼─┐  ┌─▼──────────┐
             │ User  │  │ Product    │
             │Service│  │ Service    │
             │ 8080  │  │  8081      │
             └───────┘  └────────────┘
```

## 📦 Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **Eureka Server** | 8761 | Registro y descubrimiento de servicios |
| **User Service** | 8080 | Gestión de usuarios, autenticación y JWT |
| **Product Service** | 8081 | Catálogo de productos y carrito de compras |
| **API Gateway** | 8082 | Puerta de entrada única para el frontend |
| **Payment Service** | 8083 | Gestión de pagos |
| **Contact Service** | 8084 | Servicio de contacto con soporte de IA |

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot** y **Spring Cloud**
- **Eureka** para descubrimiento de servicios
- **API Gateway** como punto de entrada único
- **MySQL 8.0** como base de datos
- **Docker** y **Docker Compose** para despliegue
- **JWT** para autenticación
- **Maven** como gestor de dependencias

## 🚀 Inicio Rápido

### Requisitos

- Docker y Docker Compose instalados
- Java 21 (para desarrollo local sin Docker)
- Maven 3.6+

### Opción 1: Ejecutar con Docker Compose

```bash
docker-compose up --build
```

Los servicios se iniciarán en el siguiente orden:
1. MySQL (base de datos)
2. Eureka Server (registro de servicios)
3. User Service, Product Service, Payment Service, Contact Service
4. API Gateway
5. Frontend (si está disponible)

### Opción 2: Ejecutar individualmente

```bash
# Compilar todos los módulos
mvn clean install

# Ejecutar cada servicio desde su respectivo directorio
cd eureka-server && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

## 📡 Endpoints

Todos los endpoints se acceden a través del **API Gateway** en `http://localhost:8082`.

### Base URL
```
http://localhost:8082/api/v1/sneakerx
```

### Autenticación
- `POST /user/auth/register` - Registrar usuario
- `POST /user/auth/login` - Iniciar sesión

### Productos
- `GET /product/products` - Listar productos
- `GET /product/products/{id}` - Obtener producto por ID

### Carrito
- `GET /product/cart` - Obtener carrito del usuario
- `POST /product/cart/add` - Agregar producto al carrito

### Admin
- Endpoints administrativos protegidos con rol ADMIN

> 📖 Para documentación completa de endpoints, consulta [INTEGRACION_FRONTEND.md](INTEGRACION_FRONTEND.md).

## 🗄️ Bases de Datos

| Base de Datos | Servicio |
|--------------|----------|
| `tienda_usuarios` | User Service |
| `tienda_productos` | Product Service |
| `tienda_pagos` | Payment Service |
| `tienda_contacto` | Contact Service |

## 🔐 Variables de Entorno

Las variables de entorno se configuran en `docker-compose.yml`:

- `DB_HOST`, `DB_PORT` - Conexión a MySQL
- `DB_USERNAME`, `DB_PASSWORD` - Credenciales de base de datos
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - URL del servidor Eureka
- `JWT_SECRET` - Clave secreta para tokens JWT

## 📁 Estructura del Proyecto

```
tiendaBackend/
├── api-gateway/         # Puerta de entrada API
├── contact-service/     # Servicio de contacto
├── eureka-server/       # Registro de servicios
├── payment-service/     # Servicio de pagos
├── product-service/     # Servicio de productos
├── user-service/        # Servicio de usuarios
├── docker-compose.yml   # Configuración Docker
├── init-db.sql          # Script de inicialización BD
├── pom.xml              # Configuración Maven padre
└── INTEGRACION_FRONTEND.md # Guía de integración
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo específico
cd user-service && mvn test
```

## 📝 Notas

- El frontend se despliega en el puerto `5173` (Vite dev server) o `80` (Nginx en Docker)
- Los visitantes pueden usar carritos temporales con session ID
- La autenticación usa tokens JWT con expiración configurable

## Datos de demostración con Flyway

Al arrancar, Flyway aplica una sola vez las migraciones pendientes de cada servicio:

| Servicio | Migración | Datos |
| --- | --- | --- |
| Usuarios | `V6__insertar_usuarios_demo.sql` | 20 clientes ficticios y 1 administrador |
| Productos | `V11__insertar_productos_demo.sql` | 20 productos y stock descontado de las compras de prueba |
| Productos | `V12__insertar_blogs_demo.sql` | 5 publicaciones |
| Pagos | `V1__create_pagos.sql`, `V2__insertar_compras_demo.sql` | Tablas, 10 compras y 20 líneas de compra |

Administrador: `admin@sneakersx.cl`, contraseña de prueba `password123`.
Clientes: `cliente01@example.com` a `cliente20@example.com`, con la misma contraseña
de prueba. Las contraseñas están almacenadas con BCrypt. Los productos usan nombres
ficticios e imágenes proporcionadas para la demostración, algunas repetidas en distinta
resolución. Los pagos son registros de prueba, sin cobros bancarios.

Cada producto comienza con 30 unidades y descuenta las cantidades incluidas en las
compras. El total de cada pago es la suma de cantidad por precio unitario, de acuerdo
con el servicio de pagos. Los servicios migran únicamente sus propias bases.

Los reinicios conservan los datos y no vuelven a insertar ni descontar stock.
No ejecutar el [SQL manual](scripts/insertar_datos_demo.sql) después de Flyway:
es una alternativa para una base vacía sin las migraciones de carga.

Consulta [las instrucciones](scripts/INSTRUCCIONES.txt) y ejecuta
[la verificación](scripts/verificar_datos_demo.sql) para comprobar cantidades,
relaciones, totales y stock inmediatamente después de la carga.
