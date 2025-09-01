-- Central Inventory Service Database Schema
-- Este script cria as tabelas necessárias para o inventário central

-- =====================================
-- Tabela de Inventário Central
-- =====================================
CREATE TABLE IF NOT EXISTS central_inventory (
    product_sku VARCHAR(100) PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(100),
    unit_price DECIMAL(10,2),
    total_quantity INTEGER NOT NULL DEFAULT 0,
    total_reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_central_inventory_category ON central_inventory(category);
CREATE INDEX IF NOT EXISTS idx_central_inventory_active ON central_inventory(active);
CREATE INDEX IF NOT EXISTS idx_central_inventory_available ON central_inventory(available_quantity);
CREATE INDEX IF NOT EXISTS idx_central_inventory_name ON central_inventory(product_name);

-- =====================================
-- Tabela de Inventário por Loja
-- =====================================
CREATE TABLE IF NOT EXISTS store_inventory (
    product_sku VARCHAR(100) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    store_name VARCHAR(200),
    store_location VARCHAR(200),
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_sync_time TIMESTAMP,
    synchronized BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (product_sku, store_id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_store_inventory_store ON store_inventory(store_id);
CREATE INDEX IF NOT EXISTS idx_store_inventory_product ON store_inventory(product_sku);
CREATE INDEX IF NOT EXISTS idx_store_inventory_available ON store_inventory(available_quantity);
CREATE INDEX IF NOT EXISTS idx_store_inventory_sync ON store_inventory(synchronized);
CREATE INDEX IF NOT EXISTS idx_store_inventory_updated ON store_inventory(last_updated);

-- =====================================
-- Tabela de Eventos de Inventário
-- =====================================
CREATE TABLE IF NOT EXISTS inventory_events (
    event_id UUID PRIMARY KEY,
    product_sku VARCHAR(100) NOT NULL,
    store_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    previous_quantity INTEGER,
    new_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    timestamp TIMESTAMP NOT NULL,
    details VARCHAR(500),
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    error_message VARCHAR(1000)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_inventory_events_product ON inventory_events(product_sku);
CREATE INDEX IF NOT EXISTS idx_inventory_events_store ON inventory_events(store_id);
CREATE INDEX IF NOT EXISTS idx_inventory_events_type ON inventory_events(event_type);
CREATE INDEX IF NOT EXISTS idx_inventory_events_status ON inventory_events(processing_status);
CREATE INDEX IF NOT EXISTS idx_inventory_events_timestamp ON inventory_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_inventory_events_processed ON inventory_events(processed_at);

-- =====================================
-- Triggers e Functions (PostgreSQL)
-- =====================================

-- Trigger para atualizar available_quantity automaticamente
CREATE OR REPLACE FUNCTION update_central_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = NEW.total_quantity - NEW.total_reserved_quantity;
    NEW.last_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_central_available_quantity
    BEFORE INSERT OR UPDATE ON central_inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_central_available_quantity();

-- Trigger para store_inventory
CREATE OR REPLACE FUNCTION update_store_available_quantity()
RETURNS TRIGGER AS $$
BEGIN
    NEW.available_quantity = NEW.quantity - NEW.reserved_quantity;
    NEW.last_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_store_available_quantity
    BEFORE INSERT OR UPDATE ON store_inventory
    FOR EACH ROW
    EXECUTE FUNCTION update_store_available_quantity();

-- =====================================
-- Views para relatórios
-- =====================================

-- View consolidada de inventário
CREATE OR REPLACE VIEW v_inventory_summary AS
SELECT 
    ci.product_sku,
    ci.product_name,
    ci.category,
    ci.total_quantity,
    ci.total_reserved_quantity,
    ci.available_quantity,
    COUNT(si.store_id) as stores_with_product,
    COUNT(CASE WHEN si.quantity > 0 THEN 1 END) as stores_with_stock,
    ci.last_updated
FROM central_inventory ci
LEFT JOIN store_inventory si ON ci.product_sku = si.product_sku
WHERE ci.active = true
GROUP BY ci.product_sku, ci.product_name, ci.category, 
         ci.total_quantity, ci.total_reserved_quantity, 
         ci.available_quantity, ci.last_updated
ORDER BY ci.product_name;

-- View de eventos recentes
CREATE OR REPLACE VIEW v_recent_events AS
SELECT 
    e.event_id,
    e.product_sku,
    e.store_id,
    e.event_type,
    e.new_quantity,
    e.reserved_quantity,
    e.timestamp,
    e.processing_status,
    ci.product_name
FROM inventory_events e
LEFT JOIN central_inventory ci ON e.product_sku = ci.product_sku
WHERE e.timestamp >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
ORDER BY e.timestamp DESC;

-- =====================================
-- Dados iniciais de exemplo
-- =====================================

-- Inserir alguns produtos de exemplo
INSERT INTO central_inventory (product_sku, product_name, description, category, unit_price) VALUES
('PROD-001', 'Smartphone XYZ', 'Smartphone com 128GB de armazenamento', 'Electronics', 899.99),
('PROD-002', 'Notebook ABC', 'Notebook para uso profissional', 'Electronics', 1299.99),
('PROD-003', 'Fone Bluetooth', 'Fone de ouvido sem fio', 'Accessories', 199.99),
('PROD-004', 'Mouse Wireless', 'Mouse sem fio ergonômico', 'Accessories', 79.99),
('PROD-005', 'Teclado Mecânico', 'Teclado mecânico para games', 'Accessories', 299.99)
ON CONFLICT (product_sku) DO NOTHING;

-- Commit das alterações
COMMIT;
