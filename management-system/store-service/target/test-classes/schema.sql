-- Schema para testes unitários com H2 Database
-- Este script é executado automaticamente pelo Spring Boot Test

-- Criar schema se não existir (H2 suporta schemas)
CREATE SCHEMA IF NOT EXISTS store_service;

-- Criar tabela products no schema correto
CREATE TABLE IF NOT EXISTS store_service.products (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    store_id VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uk_products_sku_store UNIQUE (sku, store_id)
);

-- Criar índices para performance (compatível com H2)
CREATE INDEX IF NOT EXISTS idx_store_sku ON store_service.products(store_id, sku);
CREATE INDEX IF NOT EXISTS idx_store_name ON store_service.products(store_id, name);
CREATE INDEX IF NOT EXISTS idx_active ON store_service.products(active);
