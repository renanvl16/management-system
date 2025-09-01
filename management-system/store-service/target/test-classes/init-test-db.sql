-- Script de inicialização para banco de dados de testes
-- Este script cria as tabelas e dados iniciais necessários para os testes integrados

-- Garantir que o schema está limpo
DROP TABLE IF EXISTS product CASCADE;
DROP SEQUENCE IF EXISTS product_seq;

-- Criar sequence para IDs
CREATE SEQUENCE IF NOT EXISTS product_seq START WITH 1 INCREMENT BY 50;

-- Criar tabela de produtos
CREATE TABLE product (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    price DECIMAL(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_product_sku_store UNIQUE (sku, store_id)
);

-- Criar índices para performance
CREATE INDEX idx_product_store_id ON product(store_id);
CREATE INDEX idx_product_sku ON product(sku);
CREATE INDEX idx_product_active ON product(active);
CREATE INDEX idx_product_last_updated ON product(last_updated);

-- Inserir dados de teste
INSERT INTO product (id, sku, name, store_id, quantity, reserved_quantity, price, active, last_updated, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'IPHONE-15-001', 'iPhone 15', 'STORE_TEST_001', 10, 0, 999.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440002', 'SAMSUNG-S24-001', 'Samsung Galaxy S24', 'STORE_TEST_001', 5, 0, 899.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440003', 'MACBOOK-PRO-001', 'MacBook Pro', 'STORE_TEST_001', 8, 0, 2499.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440004', 'NINTENDO-SW-001', 'Nintendo Switch', 'STORE_TEST_001', 3, 0, 299.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440005', 'MOUSE-001', 'Logitech MX Master 3', 'STORE_TEST_001', 15, 0, 89.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440006', 'KEYBOARD-001', 'Mechanical Keyboard RGB', 'STORE_TEST_001', 12, 0, 129.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440007', 'MONITOR-001', '4K Monitor 27inch', 'STORE_TEST_001', 7, 0, 399.99, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Verificar inserção
SELECT COUNT(*) as total_products FROM product;
SELECT sku, name, quantity, reserved_quantity FROM product ORDER BY name;
