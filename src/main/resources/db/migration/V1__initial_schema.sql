CREATE TABLE clientes (
    id VARCHAR(255) PRIMARY KEY,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE zonas (
    id VARCHAR(255) PRIMARY KEY,
    soporte_refrigeracion BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido VARCHAR(255) NOT NULL,
    cliente_id VARCHAR(255) NOT NULL,
    zona_id VARCHAR(255) NOT NULL,
    fecha_entrega DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    requiere_refrigeracion BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pedidos_numero_pedido UNIQUE (numero_pedido),
    CONSTRAINT chk_pedidos_estado CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ENTREGADO')),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_pedidos_zona FOREIGN KEY (zona_id) REFERENCES zonas (id)
);

CREATE INDEX idx_pedidos_estado_fecha_entrega ON pedidos (estado, fecha_entrega);

CREATE TABLE cargas_idempotencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(255) NOT NULL,
    archivo_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cargas_idempotencia_key_hash UNIQUE (idempotency_key, archivo_hash)
);
