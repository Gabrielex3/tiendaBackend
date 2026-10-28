# 🛒 SneakerX - Guía de Integración Frontend

> Guía completa para conectar tu frontend con el backend de la tienda.  
> Todos los endpoints se acceden a través del **API Gateway** en `http://localhost:8082`.

---

## 📋 Tabla de Contenidos

1. [Arquitectura del Sistema](#1-arquitectura-del-sistema)
2. [Configuración Inicial](#2-configuración-inicial)
3. [Autenticación de Usuarios](#3-autenticación-de-usuarios)
4. [Productos](#4-productos)
5. [Carrito de Compras](#5-carrito-de-compras)
6. [Carrito como Visitante](#6-carrito-como-visitante)
7. [Transferir Carrito Temporal](#7-transferir-carrito-temporal)
8. [Admin (Opcional)](#8-admin-opcional)
9. [Ejemplo de Flujo Completo](#9-ejemplo-de-flujo-completo)
10. [Modelos de Datos](#10-modelos-de-datos)

---

## 1. Arquitectura del Sistema

```
                    ┌─────────────┐
   Frontend ────────→│  API Gateway │  (localhost:8082)
                    │  lb://      │
                    └──────┬──────┘
                           │ consulta
                    ┌──────▼──────┐
                    │  Eureka     │  (localhost:8761)
                    │  Server     │
                    └──┬──────┬───┘
                       │      │ registra
                ┌──────▼─┐  ┌─▼────────┐
                │ User   │  │ Product  │
                │Service │  │ Service  │
                │ 8080   │  │  8081    │
                └────────┘  └──────────┘
```

- **API Gateway** (`localhost:8082`): Puerta de entrada única. Tu frontend solo habla con el Gateway.
- **Eureka Server** (`localhost:8761`): Registro de servicios (no interactúas directamente).
- **User Service** (`localhost:8080`): Usuarios, login, JWT.
- **Product Service** (`localhost:8081`): Productos y carrito.

---

## 2. Configuración Inicial

### Base URL

```javascript
const API_BASE = 'http://localhost:8082/api/v1/sneakerx';
```

### Variables de Estado Recomendadas

```javascript
// Estado global de tu app (ejemplo con React context, Redux, etc.)
const appState = {
  userId: null,           // ID del usuario logueado
  jwtToken: null,         // Token JWT
  sessionId: null,        // UUID temporal para visitantes
  user: null,             // Datos del usuario
  cart: null,             // Carrito actual
};
```

### Generar Session ID (para visitantes)

```javascript
// Al cargar la app, genera un sessionId si no existe
function getOrCreateSessionId() {
  let sessionId = localStorage.getItem('sneakerx_sessionId');
  if (!sessionId) {
    sessionId = crypto.randomUUID(); // o cualquier UUID
    localStorage.setItem('sneakerx_sessionId', sessionId);
  }
  return sessionId;
}
```

---

## 3. Autenticación de Usuarios

### 3.1 Registrar Usuario

```http
POST {API_BASE}/user/auth/register
Content-Type: application/json
```

**Body:**
```json
{
  "username": "carlos_sneaker",
  "password": "password123",
  "email": "carlos@sneakerx.com",
  "phone": "+56912345678",
  "address": "Av. Principal 123",
  "comuna": "Providencia"
}
```

**Respuesta:** `201 Created` — Usuario creado.

**Campos requeridos:** `username`, `password` (mínimo 8 caracteres), `email`.  
**Campos opcionales:** `phone`, `address`, `comuna`.

**Ejemplo JavaScript:**
```javascript
async function register(userData) {
  const response = await fetch(`${API_BASE}/user/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(userData),
  });
  if (!response.ok) throw new Error('Error al registrar');
  return response.json();
}
```

---

### 3.2 Login

```http
POST {API_BASE}/user/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "username": "carlos_sneaker",
  "password": "password123"
}
```

**Respuesta:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "carlos_sneaker"
}
```

**Ejemplo JavaScript:**
```javascript
async function login(username, password) {
  const response = await fetch(`${API_BASE}/user/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) throw new Error('Credenciales inválidas');
  const data = await response.json();
  
  // Guarda el token y el userId
  localStorage.setItem('sneakerx_jwt', data.token);
  localStorage.setItem('sneakerx_userId', data.userId);
  return data;
}
```

---

### 3.3 Obtener Perfil del Usuario

```http
GET {API_BASE}/user/usuarios/me
Authorization: Bearer {jwt_token}
```

**Respuesta:** `200 OK`
```json
{
  "id": 1,
  "username": "carlos_sneaker",
  "email": "carlos@sneakerx.com",
  "phone": "+56912345678",
  "address": "Av. Principal 123",
  "comuna": "Providencia",
  "role": "USER"
}
```

**Ejemplo JavaScript:**
```javascript
async function getProfile(token) {
  const response = await fetch(`${API_BASE}/user/usuarios/me`, {
    headers: { 'Authorization': `Bearer ${token}` },
  });
  if (!response.ok) throw new Error('No autorizado');
  return response.json();
}
```

---

## 4. Productos

### 4.1 Listar Todos los Productos

```http
GET {API_BASE}/producto/productos
```

**Respuesta:** `200 OK` — Array de productos.
```json
[
  {
    "id": 1,
    "nombre": "Nike Air Max 90",
    "descripcion": "Comodidad clásica con amortiguación Air visible.",
    "precio": 1599.00,
    "stock": 50,
    "imagenUrl": "nike-air-max-90.jpg",
    "categoria": "Casual",
    "activo": true
  },
  // ... más productos
]
```

**Ejemplo JavaScript:**
```javascript
async function getProducts() {
  const response = await fetch(`${API_BASE}/producto/productos`);
  return response.json();
}
```

---

### 4.2 Filtrar por Categoría

```http
GET {API_BASE}/producto/productos?categoria=Running
```

**Respuesta:** Solo productos de la categoría "Running".

---

### 4.3 Buscar por Nombre

```http
GET {API_BASE}/producto/productos?nombre=Nike
```

**Respuesta:** Productos cuyo nombre contiene "Nike" (case insensitive).

---

### 4.4 Obtener Producto por ID

```http
GET {API_BASE}/producto/productos/{id}
```

**Respuesta:** `200 OK` — Un solo producto.

---

### 4.5 Ejemplo: Listado con Filtros

```javascript
async function getProducts(filters = {}) {
  const params = new URLSearchParams();
  if (filters.categoria) params.append('categoria', filters.categoria);
  if (filters.nombre) params.append('nombre', filters.nombre);
  
  const url = `${API_BASE}/producto/productos?${params.toString()}`;
  const response = await fetch(url);
  return response.json();
}

// Uso:
getProducts({ categoria: 'Running' });
getProducts({ nombre: 'Nike' });
getProducts({ categoria: 'Casual', nombre: 'Vans' });
```

---

## 5. Carrito de Compras

> **Requiere usuario autenticado.** Usa el header `X-User-Id` con el ID del usuario.

### 5.1 Ver Carrito del Usuario

```http
GET {API_BASE}/carrito
X-User-Id: {user_id}
```

**Respuesta:** `200 OK`
```json
{
  "id": 1,
  "usuarioId": 1,
  "sessionId": null,
  "items": [
    {
      "id": 1,
      "producto": {
        "id": 1,
        "nombre": "Nike Air Max 90",
        "precio": 1599.00,
        "imagenUrl": "nike-air-max-90.jpg"
      },
      "cantidad": 1,
      "precioUnitario": 1599.00,
      "subtotal": 1599.00
    }
  ]
}
```

**Nota:** Si el carrito no existe, se crea automáticamente vacío.

---

### 5.2 Agregar Item al Carrito

```http
POST {API_BASE}/carrito/items
Content-Type: application/json
X-User-Id: {user_id}
```

**Body:**
```json
{
  "productoId": 1,
  "cantidad": 2
}
```

**Respuesta:** `200 OK` — Carrito actualizado con todos los items.

**Comportamiento:**
- Si el producto ya existe en el carrito, se **suma** la cantidad.
- Si es nuevo, se **agrega** como item nuevo.
- El `precioUnitario` se toma del producto en el momento de agregar.

---

### 5.3 Actualizar Cantidad de un Item

```http
PUT {API_BASE}/carrito/items/{itemId}?cantidad={nueva_cantidad}
X-User-Id: {user_id}
```

**Respuesta:** `200 OK` — Carrito actualizado.

---

### 5.4 Eliminar un Item del Carrito

```http
DELETE {API_BASE}/carrito/items/{itemId}
X-User-Id: {user_id}
```

**Respuesta:** `200 OK` — Carrito sin el item eliminado.

---

### 5.5 Vaciar Carrito Completo

```http
DELETE {API_BASE}/carrito
X-User-Id: {user_id}
```

**Respuesta:** `200 OK` — Carrito vacío.

---

### 5.6 Ejemplo JavaScript: Carrito con Usuario

```javascript
async function getCart(userId) {
  const response = await fetch(`${API_BASE}/carrito`, {
    headers: { 'X-User-Id': String(userId) },
  });
  return response.json();
}

async function addToCart(userId, productoId, cantidad) {
  const response = await fetch(`${API_BASE}/carrito/items`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId),
    },
    body: JSON.stringify({ productoId, cantidad }),
  });
  return response.json();
}

async function updateCartItem(userId, itemId, cantidad) {
  const response = await fetch(
    `${API_BASE}/carrito/items/${itemId}?cantidad=${cantidad}`,
    {
      method: 'PUT',
      headers: { 'X-User-Id': String(userId) },
    }
  );
  return response.json();
}

async function removeCartItem(userId, itemId) {
  const response = await fetch(`${API_BASE}/carrito/items/${itemId}`, {
    method: 'DELETE',
    headers: { 'X-User-Id': String(userId) },
  });
  return response.json();
}

async function clearCart(userId) {
  const response = await fetch(`${API_BASE}/carrito`, {
    method: 'DELETE',
    headers: { 'X-User-Id': String(userId) },
  });
  return response.json();
}
```

---

## 6. Carrito como Visitante

> **Sin cuenta.** Usa el header `X-Session-Id` con un UUID generado por el frontend.

### 6.1 Ver Carrito del Visitante

```http
GET {API_BASE}/carrito/sesion
X-Session-Id: {session_id}
```

---

### 6.2 Agregar Item (Visitante)

```http
POST {API_BASE}/carrito/sesion/items
Content-Type: application/json
X-Session-Id: {session_id}
```

**Body:**
```json
{
  "productoId": 2,
  "cantidad": 1
}
```

---

### 6.3 Actualizar Cantidad (Visitante)

```http
PUT {API_BASE}/carrito/sesion/items/{itemId}?cantidad={nueva_cantidad}
X-Session-Id: {session_id}
```

---

### 6.4 Eliminar Item (Visitante)

```http
DELETE {API_BASE}/carrito/sesion/items/{itemId}
X-Session-Id: {session_id}
```

---

### 6.5 Vaciar Carrito (Visitante)

```http
DELETE {API_BASE}/carrito/sesion
X-Session-Id: {session_id}
```

---

### 6.6 Ejemplo JavaScript: Carrito de Visitante

```javascript
async function getGuestCart(sessionId) {
  const response = await fetch(`${API_BASE}/carrito/sesion`, {
    headers: { 'X-Session-Id': sessionId },
  });
  return response.json();
}

async function addToGuestCart(sessionId, productoId, cantidad) {
  const response = await fetch(`${API_BASE}/carrito/sesion/items`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Session-Id': sessionId,
    },
    body: JSON.stringify({ productoId, cantidad }),
  });
  return response.json();
}

async function removeFromGuestCart(sessionId, itemId) {
  const response = await fetch(
    `${API_BASE}/carrito/sesion/items/${itemId}`,
    {
      method: 'DELETE',
      headers: { 'X-Session-Id': sessionId },
    }
  );
  return response.json();
}
```

---

## 7. Transferir Carrito Temporal

> Cuando un visitante se registra o inicia sesión, transfiere su carrito temporal al usuario autenticado.

```http
POST {API_BASE}/carrito/transferir
X-Session-Id: {session_id}
X-User-Id: {user_id}
```

**Respuesta:** `200 OK` — Carrito fusionado.
```json
{
  "id": 1,
  "usuarioId": 1,
  "sessionId": null,
  "items": [
    // Items del carrito del usuario + items del carrito temporal
  ]
}
```

**Comportamiento:**
- Los items del carrito temporal se **fusionan** con los del carrito del usuario.
- Si un producto ya existe en ambos, se **suma** la cantidad.
- El carrito temporal se **elimina** después de la transferencia.

**Ejemplo JavaScript:**
```javascript
async function transferCart(sessionId, userId) {
  const response = await fetch(`${API_BASE}/carrito/transferir`, {
    method: 'POST',
    headers: {
      'X-Session-Id': sessionId,
      'X-User-Id': String(userId),
    },
  });
  const cart = await response.json();
  
  // Limpia el sessionId temporal
  localStorage.removeItem('sneakerx_sessionId');
  
  return cart;
}
```

---

## 8. Admin (Opcional)

> Endpoints de administración. Requieren autenticación JWT con rol `ADMIN`.

### 8.1 Crear Producto

```http
POST {API_BASE}/producto/admin/productos
Content-Type: application/json
```

**Body:**
```json
{
  "nombre": "Asics Gel-Kayano 30",
  "descripcion": "Estabilidad y confort para corredores de larga distancia.",
  "precio": 2899.00,
  "stock": 60,
  "imagenUrl": "asics-kayano.jpg",
  "categoria": "Running"
}
```

---

### 8.2 Actualizar Producto

```http
PUT {API_BASE}/producto/admin/productos/{id}
Content-Type: application/json
```

**Body:** (mismo formato que crear, solo los campos que quieras cambiar)

---

### 8.3 Eliminar Producto (Eliminación Lógica)

```http
DELETE {API_BASE}/producto/admin/productos/{id}
```

> El producto se marca como `activo = false`, no se elimina de la BD.

---

### 8.4 Listar Usuarios

```http
GET {API_BASE}/user/admin/usuarios
Authorization: Bearer {jwt_token}
```

---

## 9. Ejemplo de Flujo Completo

### Flujo 1: Visitante → Carrito → Registro → Transferencia

```javascript
// 1. Al cargar la app, genera sessionId
const sessionId = getOrCreateSessionId();

// 2. Visitante navega y ve productos
const productos = await getProducts();

// 3. Visitante agrega al carrito sin cuenta
await addToGuestCart(sessionId, 1, 1);  // Nike Air Max 90
await addToGuestCart(sessionId, 3, 1);  // Jordan 1 Retro

// 4. Visitante decide registrarse
await register({
  username: 'nuevo_usuario',
  password: 'miClave123',
  email: 'nuevo@email.com',
});

// 5. Login después del registro
const loginData = await login('nuevo_usuario', 'miClave123');
const userId = loginData.userId;
const token = loginData.token;

// 6. Transferir carrito temporal al usuario
await transferCart(sessionId, userId);

// 7. Ahora usa el carrito con userId
const cart = await getCart(userId);
```

---

### Flujo 2: Usuario Logueado → Carrito Directo

```javascript
// 1. Login
const loginData = await login('carlos_sneaker', 'password123');
const userId = loginData.userId;
const token = loginData.token;

// 2. Ver productos
const productos = await getProducts({ categoria: 'Running' });

// 3. Agregar al carrito
await addToCart(userId, 2, 1);  // Adidas Ultraboost
await addToCart(userId, 4, 2);  // New Balance 550

// 4. Ver carrito
const cart = await getCart(userId);

// 5. Actualizar cantidad
await updateCartItem(userId, cart.items[0].id, 3);

// 6. Eliminar un item
await removeCartItem(userId, cart.items[1].id);

// 7. Ver perfil
const profile = await getProfile(token);
```

---

## 10. Modelos de Datos

### 10.1 Producto

```typescript
interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;          // BigDecimal, ej: 1599.00
  stock: number;
  imagenUrl: string;       // URL o nombre del archivo
  categoria: string;       // "Running", "Casual", "Basketball", etc.
  activo: boolean;
  createdAt: string;       // ISO 8601
  updatedAt: string | null;
}
```

### 10.2 CartItem

```typescript
interface CartItem {
  id: number;
  producto: Producto;
  cantidad: number;
  precioUnitario: number;  // Precio al momento de agregar
  subtotal: number;        // cantidad * precioUnitario
  createdAt: string;
}
```

### 10.3 Carrito

```typescript
interface Carrito {
  id: number;
  usuarioId: number | null;   // null si es carrito temporal
  sessionId: string | null;   // UUID si es carrito temporal
  items: CartItem[];
  createdAt: string;
  updatedAt: string | null;
}
```

### 10.4 Usuario

```typescript
interface Usuario {
  id: number;
  username: string;
  email: string;
  phone: string | null;
  address: string | null;
  comuna: string | null;
  role: 'USER' | 'ADMIN';
  createdAt: string;
}
```

### 10.5 Login Response

```typescript
interface LoginResponse {
  token: string;      // JWT token
  userId: number;
  username: string;
}
```

---

## 📌 Resumen de Endpoints

| Método | Endpoint | Autenticación | Descripción |
|--------|----------|---------------|-------------|
| `POST` | `/api/v1/sneakerx/user/auth/register` | Pública | Registrar usuario |
| `POST` | `/api/v1/sneakerx/user/auth/login` | Pública | Login |
| `GET` | `/api/v1/sneakerx/user/usuarios/me` | JWT | Perfil del usuario |
| `GET` | `/api/v1/sneakerx/producto/productos` | Pública | Listar productos |
| `GET` | `/api/v1/sneakerx/producto/productos?categoria=X` | Pública | Filtrar por categoría |
| `GET` | `/api/v1/sneakerx/producto/productos?nombre=X` | Pública | Buscar por nombre |
| `GET` | `/api/v1/sneakerx/producto/productos/{id}` | Pública | Producto por ID |
| `GET` | `/api/v1/sneakerx/carrito` | `X-User-Id` | Ver carrito (usuario) |
| `POST` | `/api/v1/sneakerx/carrito/items` | `X-User-Id` | Agregar item (usuario) |
| `PUT` | `/api/v1/sneakerx/carrito/items/{id}?cantidad=N` | `X-User-Id` | Actualizar cantidad |
| `DELETE` | `/api/v1/sneakerx/carrito/items/{id}` | `X-User-Id` | Eliminar item |
| `DELETE` | `/api/v1/sneakerx/carrito` | `X-User-Id` | Vaciar carrito |
| `GET` | `/api/v1/sneakerx/carrito/sesion` | `X-Session-Id` | Ver carrito (visitante) |
| `POST` | `/api/v1/sneakerx/carrito/sesion/items` | `X-Session-Id` | Agregar item (visitante) |
| `PUT` | `/api/v1/sneakerx/carrito/sesion/items/{id}?cantidad=N` | `X-Session-Id` | Actualizar (visitante) |
| `DELETE` | `/api/v1/sneakerx/carrito/sesion/items/{id}` | `X-Session-Id` | Eliminar (visitante) |
| `DELETE` | `/api/v1/sneakerx/carrito/sesion` | `X-Session-Id` | Vaciar (visitante) |
| `POST` | `/api/v1/sneakerx/carrito/transferir` | `X-Session-Id` + `X-User-Id` | Transferir carrito |

---

## 🔑 Headers de Autenticación

| Header | Valor | Uso |
|--------|-------|-----|
| `Authorization` | `Bearer {jwt_token}` | Perfil de usuario, admin |
| `X-User-Id` | `{user_id}` (número) | Carrito de usuario autenticado |
| `X-Session-Id` | `{uuid}` (string) | Carrito de visitante temporal |
| `X-Session-Id` + `X-User-Id` | Ambos headers | Transferir carrito temporal |

---

## ⚠️ Notas Importantes

1. **Precios**: El `precioUnitario` se fija al agregar el item al carrito. Si el precio del producto cambia después, el carrito mantiene el precio original.

2. **Stock**: El backend no valida el stock al agregar al carrito. Valida en el frontend mostrando el stock disponible.

3. **Eliminación lógica**: Los productos se eliminan de forma lógica (`activo = false`). No aparecen en las listas públicas, pero se preservan en el carrito.

4. **JWT Token**: Almacena el token en `localStorage` o `sessionStorage`. Úsalo en el header `Authorization` para endpoints protegidos.

5. **Session ID**: Genera el UUID en el frontend y guárdalo en `localStorage`. Persiste el carrito del visitante entre sesiones del navegador.

6. **Transferencia**: Después de transferir el carrito, elimina el `sessionId` del `localStorage` para que el usuario use su carrito autenticado.

7. **Carrito automático**: Si un usuario autenticado no tiene carrito, se crea automáticamente al hacer la primera petición.

8. **Fusión de carritos**: Al transferir, si un producto existe en ambos carritos, se suman las cantidades.

---

## 🚀 Orden de Inicio de Servicios

Para que todo funcione, inicia los servicios en este orden:

1. **Eureka Server** → Puerto 8761
2. **User Service** → Puerto 8080
3. **Product Service** → Puerto 8081
4. **API Gateway** → Puerto 8082

Tu frontend solo necesita comunicarse con el **API Gateway** (puerto 8082).
