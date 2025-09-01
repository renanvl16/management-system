-- Script de inicialização do banco de dados PostgreSQL
-- para o sistema de gestão de inventário

-- Garantir privilégios ao usuário
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Schema para Store Service
CREATE SCHEMA IF NOT EXISTS store_service;

-- Schema para Central Inventory Service  
CREATE SCHEMA IF NOT EXISTS central_service;

-- Tabela de produtos para o Store Service
CREATE TABLE IF NOT EXISTS store_service.products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id VARCHAR(50) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sku, store_id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_products_store_id ON store_service.products(store_id);
CREATE INDEX IF NOT EXISTS idx_products_sku ON store_service.products(sku);
CREATE INDEX IF NOT EXISTS idx_products_active ON store_service.products(active);

-- Tabela de inventário global para o Central Service
CREATE TABLE IF NOT EXISTS central_service.global_inventory (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) NOT NULL UNIQUE,
    total_quantity INTEGER NOT NULL DEFAULT 0,
    total_reserved INTEGER NOT NULL DEFAULT 0,
    stores_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de inventário por loja para o Central Service
CREATE TABLE IF NOT EXISTS central_service.store_inventory_summary (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id VARCHAR(50) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    last_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(store_id, sku)
);

-- Índices para o Central Service
CREATE INDEX IF NOT EXISTS idx_global_inventory_sku ON central_service.global_inventory(sku);
CREATE INDEX IF NOT EXISTS idx_store_inventory_store_id ON central_service.store_inventory_summary(store_id);
CREATE INDEX IF NOT EXISTS idx_store_inventory_sku ON central_service.store_inventory_summary(sku);

-- Dados de exemplo para testes
INSERT INTO store_service.products (store_id, sku, name, description, price, quantity, active) VALUES
('STORE-001', 'NOTEBOOK-001', 'Notebook Dell Inspiron 15', 'Notebook Dell Inspiron 15 com 8GB RAM e SSD 256GB', 2499.99, 10, true),
('STORE-001', 'MOUSE-001', 'Mouse Logitech MX Master 3', 'Mouse wireless ergonômico para produtividade', 349.99, 25, true),
('STORE-001', 'KEYBOARD-001', 'Teclado Mecânico Corsair K95', 'Teclado mecânico RGB para gamers', 699.99, 8, true),
('STORE-001', 'MONITOR-001', 'Monitor LG 27" 4K', 'Monitor IPS 27 polegadas resolução 4K', 1299.99, 5, true),
('STORE-001', 'HEADSET-001', 'Headset HyperX Cloud II', 'Headset gamer com som surround 7.1', 299.99, 15, true)
ON CONFLICT (sku, store_id) DO NOTHING;

-- Sincronizar dados iniciais no inventário global
INSERT INTO central_service.global_inventory (sku, total_quantity, stores_count) VALUES
('NOTEBOOK-001', 10, 1),
('MOUSE-001', 25, 1),
('KEYBOARD-001', 8, 1),
('MONITOR-001', 5, 1),
('HEADSET-001', 15, 1)
ON CONFLICT (sku) DO UPDATE SET
    total_quantity = central_service.global_inventory.total_quantity + EXCLUDED.total_quantity,
    stores_count = central_service.global_inventory.stores_count + EXCLUDED.stores_count;

-- Sincronizar dados do resumo por loja
INSERT INTO central_service.store_inventory_summary (store_id, sku, quantity) VALUES
('STORE-001', 'NOTEBOOK-001', 10),
('STORE-001', 'MOUSE-001', 25),
('STORE-001', 'KEYBOARD-001', 8),
('STORE-001', 'MONITOR-001', 5),
('STORE-001', 'HEADSET-001', 15)
ON CONFLICT (store_id, sku) DO NOTHING;

-- ====================================================================
-- TABELA DE EVENTOS FALHADOS (Dead Letter Queue)
-- ====================================================================

-- Tabela de Eventos Falhados para o Store Service
CREATE TABLE IF NOT EXISTS store_service.failed_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255),
    event_payload TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 10,
    last_error TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_retry_at TIMESTAMP,
    next_retry_at TIMESTAMP
);

-- Índices para Dead Letter Queue
CREATE INDEX IF NOT EXISTS idx_failed_events_event_id ON store_service.failed_events(event_id);
CREATE INDEX IF NOT EXISTS idx_failed_events_status ON store_service.failed_events(status);
CREATE INDEX IF NOT EXISTS idx_failed_events_next_retry ON store_service.failed_events(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_failed_events_created_at ON store_service.failed_events(created_at);
CREATE INDEX IF NOT EXISTS idx_failed_events_event_type ON store_service.failed_events(event_type);
