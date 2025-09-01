-- Criação da extensão UUID se não existir
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criação do schema se não existir
CREATE SCHEMA IF NOT EXISTS store_service;

-- Criação da tabela de produtos
CREATE TABLE store_service.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    store_id VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0
);

-- Índices para otimizar consultas
CREATE UNIQUE INDEX idx_store_sku ON store_service.products(store_id, sku);
CREATE INDEX idx_store_name ON store_service.products(store_id, name);
CREATE INDEX idx_active ON store_service.products(active);
CREATE INDEX idx_store_id ON store_service.products(store_id);

-- Criação da tabela de eventos falhados
CREATE TABLE store_service.failed_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id UUID NOT NULL,
    topic VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255),
    event_type VARCHAR(100) NOT NULL,
    event_payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para a tabela de eventos falhados
CREATE INDEX idx_failed_events_status ON store_service.failed_events(status);
CREATE INDEX idx_failed_events_next_retry ON store_service.failed_events(next_retry_at);
CREATE INDEX idx_failed_events_event_id ON store_service.failed_events(event_id);

-- Inserção de alguns dados de teste
INSERT INTO store_service.products (id, sku, name, description, price, quantity, reserved_quantity, store_id, active) VALUES
    (uuid_generate_v4(), 'NOTEBOOK-001', 'Notebook Lenovo Ideapad 3', 'Notebook Lenovo Ideapad 3 com 8GB RAM e SSD 256GB', 3200.00, 20, 0, 'STORE-001', true),
    (uuid_generate_v4(), 'LAPTOP-001', 'Dell Inspiron 15', 'Laptop Dell Inspiron 15 com 8GB RAM e SSD 256GB', 2500.00, 10, 0, 'STORE-001', true),
    (uuid_generate_v4(), 'MOUSE-001', 'Mouse Logitech M100', 'Mouse óptico USB com cabo', 35.90, 50, 5, 'STORE-001', true),
    (uuid_generate_v4(), 'KEYBOARD-001', 'Teclado Logitech K120', 'Teclado USB ABNT2', 89.90, 25, 2, 'STORE-001', true),
    (uuid_generate_v4(), 'MONITOR-001', 'Monitor Samsung 24"', 'Monitor LED Full HD 24 polegadas', 899.00, 8, 1, 'STORE-001', true),
    (uuid_generate_v4(), 'HEADSET-001', 'Headset Gamer HyperX', 'Headset Gamer com microfone', 299.90, 15, 0, 'STORE-001', true),
    
    -- Produtos para STORE-002
    (uuid_generate_v4(), 'LAPTOP-001', 'Dell Inspiron 15', 'Laptop Dell Inspiron 15 com 8GB RAM e SSD 256GB', 2500.00, 5, 1, 'STORE-002', true),
    (uuid_generate_v4(), 'TABLET-001', 'Samsung Galaxy Tab A', 'Tablet Android com tela 10 polegadas', 799.00, 12, 0, 'STORE-002', true),
    (uuid_generate_v4(), 'SMARTPHONE-001', 'iPhone 13', 'Smartphone Apple iPhone 13 128GB', 4999.00, 3, 1, 'STORE-002', true);
