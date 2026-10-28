# MySQL y Flyway

La aplicación usa tu servidor MariaDB en `localhost:3306` y la base
`tienda_backend` por defecto. Usa el conector JDBC de MariaDB y el módulo
`flyway-mysql`, que también admite MariaDB.

Antes del primer inicio, crea una base vacía desde tu cliente MySQL:

```sql
CREATE DATABASE tienda_backend CHARACTER SET utf8mb4;
```

Completa `DB_USERNAME`, `DB_PASSWORD` y `DB_NAME` en el archivo `.env` de la raíz.
Spring lo carga como un archivo de propiedades mediante `spring.config.import`.
Usa `CLAVE=valor`, sin `export` ni comillas alrededor de los valores. No ejecutes
el archivo con `source`: lo lee Spring directamente. Si una contraseña contiene
barras invertidas, escríbelas duplicadas (`\\`) según el formato `.properties`.

El `.env` local incluye una clave JWT aleatoria. Si necesitas crear otro entorno,
copia `.env.example` a `.env` y genera una clave con:

```bash
openssl rand -base64 32
```

Pega el resultado en `JWT_SECRET`. Inicia la aplicación desde la raíz del proyecto
con `bash mvnw spring-boot:run`. En IntelliJ, configura esa raíz como directorio de
trabajo. Las variables de entorno del proceso tienen prioridad sobre el `.env`.

Conserva el mismo `JWT_SECRET` entre reinicios si quieres que los tokens existentes
sigan siendo válidos. Debe ser una clave Base64 de al menos 32 bytes aleatorios.
No guardes contraseñas ni claves reales en el repositorio.

`DB_HOST` y `DB_PORT` permiten cambiar el servidor y el puerto. El usuario de MySQL
necesita permisos para aplicar migraciones y leer/escribir datos en esa base.

Al iniciar, Flyway ejecuta `db/migration/V1__create_users.sql` y registra la versión
en `flyway_schema_history`. Luego Hibernate valida que el esquema coincida con
las entidades. No crea ni actualiza tablas por su cuenta.

Si la base ya contiene tablas, revisa el esquema existente antes de aplicar la
primera migración; esta configuración está pensada para una base vacía.
Para cambios posteriores, agrega `V2__descripcion.sql`, `V3__descripcion.sql`, etc.
No edites una migración que ya fue aplicada.
