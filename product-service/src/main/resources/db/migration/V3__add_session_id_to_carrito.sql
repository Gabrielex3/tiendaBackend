ALTER TABLE carrito
    DROP INDEX uk_carrito_usuario;

ALTER TABLE carrito
    MODIFY usuario_id BIGINT NULL,
    ADD COLUMN session_id VARCHAR(64) NULL;

ALTER TABLE carrito
    ADD CONSTRAINT uk_carrito_usuario UNIQUE (usuario_id);
