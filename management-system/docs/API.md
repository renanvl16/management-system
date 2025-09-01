# 🌐 APIs e Endpoints

## 📋 Índice

- [🏪 Store Service APIs](#-store-service-apis)
- [🏢 Central Inventory Service APIs](#-central-inventory-service-apis)
- [🛡️ Admin APIs (DLQ)](#️-admin-apis-dlq)
- [📮 Coleção Postman](#-coleção-postman)
- [📚 Documentação Interativa](#-documentação-interativa)

## 🏪 Store Service APIs

**Base URL**: `http://localhost:8081`

### 📋 Gestão de Produtos

#### 🔍 Listar Produtos da Loja
```http
GET /api/v1/store/{storeId}/inventory/products
```

**Parâmetros:**
- `storeId` (path): ID da loja (ex: "STORE-001")
- `page` (query, opcional): Número da página (padrão: 0)
- `size` (query, opcional): Itens por página (padrão: 20)
- `search` (query, opcional): Filtro de busca

**Exemplo de Resposta (200):**
```json
{
  "storeId": "STORE-001",
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "sku": "NOTEBOOK-001",
      "name": "Notebook Dell Inspiron 15",
      "description": "Notebook Dell com 8GB RAM e SSD 256GB",
      "price": 2499.99,
      "quantity": 10,
      "reservedQuantity": 2,
      "availableQuantity": 8,
      "active": true,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T14:20:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

#### 📦 Obter Produto Específico
```http
GET /api/v1/store/{storeId}/inventory/products/{sku}
```

**cURL Example:**
```bash
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001" | jq
```

**Resposta de Sucesso (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "sku": "NOTEBOOK-001",
  "name": "Notebook Dell Inspiron 15",
  "description": "Notebook Dell com 8GB RAM e SSD 256GB", 
  "price": 2499.99,
  "quantity": 10,
  "reservedQuantity": 2,
  "availableQuantity": 8,
  "active": true,
  "storeId": "STORE-001"
}
```

### 🛒 Gestão de Reservas

#### 🎫 Reservar Produto
```http
POST /api/v1/store/{storeId}/inventory/products/{sku}/reserve
Content-Type: application/json
```

**Body:**
```json
{
  "quantity": 2,
  "customerId": "customer-123",
  "reservationDuration": "PT30M",
  "metadata": {
    "source": "web-app",
    "sessionId": "session-456"
  }
}
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2,
    "customerId": "customer-123",
    "reservationDuration": "PT30M"
  }' | jq
```

**Resposta de Sucesso (201):**
```json
{
  "reservationId": "res-550e8400-e29b-41d4-a716-446655440002",
  "sku": "NOTEBOOK-001",
  "quantity": 2,
  "customerId": "customer-123",
  "expiresAt": "2024-01-15T15:00:00Z",
  "status": "RESERVED",
  "createdAt": "2024-01-15T14:30:00Z"
}
```

#### ✅ Confirmar Reserva (Efetuar Venda)
```http
POST /api/v1/store/{storeId}/inventory/products/{sku}/commit
Content-Type: application/json
```

**Body:**
```json
{
  "reservationId": "res-550e8400-e29b-41d4-a716-446655440002",
  "customerId": "customer-123",
  "saleDetails": {
    "orderId": "order-789",
    "paymentId": "payment-101112"
  }
}
```

**Resposta de Sucesso (200):**
```json
{
  "transactionId": "tx-550e8400-e29b-41d4-a716-446655440003",
  "reservationId": "res-550e8400-e29b-41d4-a716-446655440002",
  "status": "COMMITTED",
  "quantity": 2,
  "finalPrice": 4999.98,
  "committedAt": "2024-01-15T14:35:00Z"
}
```

#### ❌ Cancelar Reserva
```http
POST /api/v1/store/{storeId}/inventory/products/{sku}/cancel
Content-Type: application/json
```

**Body:**
```json
{
  "reservationId": "res-550e8400-e29b-41d4-a716-446655440002",
  "customerId": "customer-123",
  "reason": "Customer changed mind"
}
```

### 📊 Atualização de Quantidade
```http
PUT /api/v1/store/{storeId}/inventory/products/{sku}/quantity
Content-Type: application/json
```

**Body:**
```json
{
  "newQuantity": 50,
  "reason": "Stock replenishment",
  "adjustmentType": "RESTOCK"
}
```

## 🏢 Central Inventory Service APIs

**Base URL**: `http://localhost:8082`

### 🌍 Visão Global do Inventário

#### 📊 Inventário Global por SKU
```http
GET /api/v1/inventory/global/{sku}
```

**cURL Example:**
```bash
curl -s "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" | jq
```

**Resposta de Sucesso (200):**
```json
{
  "sku": "NOTEBOOK-001",
  "totalQuantity": 45,
  "totalReserved": 8,
  "totalAvailable": 37,
  "storesCount": 3,
  "stores": [
    {
      "storeId": "STORE-001",
      "quantity": 10,
      "reserved": 2,
      "available": 8,
      "lastSync": "2024-01-15T14:30:00Z"
    },
    {
      "storeId": "STORE-002", 
      "quantity": 20,
      "reserved": 3,
      "available": 17,
      "lastSync": "2024-01-15T14:25:00Z"
    }
  ],
  "lastUpdated": "2024-01-15T14:30:00Z"
}
```

#### 🏪 Inventário por Loja
```http
GET /api/v1/inventory/stores/{storeId}/products
```

#### 🔄 Reconciliação Manual
```http
POST /api/v1/inventory/reconcile
Content-Type: application/json
```

**Body:**
```json
{
  "storeId": "STORE-001",
  "sku": "NOTEBOOK-001",
  "forceSync": true
}
```

### ⚙️ Endpoints de Controle de Concorrência

#### 🔒 Status de Concorrência Global
```http
GET /api/v1/inventory/concurrency/status
```

**Resposta (200):**
```json
{
  "activeConnections": 15,
  "optimisticLockFailures": {
    "last24Hours": 23,
    "currentHour": 2
  },
  "retryStatistics": {
    "totalRetries": 45,
    "averageRetryCount": 2.1,
    "maxRetriesReached": 3
  },
  "conflictResolution": {
    "conflictsResolved": 20,
    "averageResolutionTime": "150ms"
  }
}
```

#### 📊 Métricas de Versioning
```http
GET /api/v1/inventory/metrics/versioning
```

#### 🔄 Forçar Sincronização
```http
POST /api/v1/inventory/sync/force/{sku}
```

## 🛡️ Admin APIs (DLQ)

### 📊 Estatísticas do DLQ
```http
GET /api/v1/admin/dlq/stats
```

**Resposta:**
```json
{
  "totalEvents": 15,
  "pendingEvents": 3,
  "processingEvents": 1,
  "succeededEvents": 10,
  "failedEvents": 1,
  "averageRetryCount": 2.3,
  "oldestPendingEvent": "2024-01-15T10:30:00Z"
}
```

### 📋 Listar Eventos DLQ
```http
GET /api/v1/admin/dlq/events?page=0&size=10&status=PENDING
```

### 🔄 Processar Fila DLQ
```http
POST /api/v1/admin/dlq/process-queue
```

### 🧹 Limpeza de Eventos Antigos
```http
DELETE /api/v1/admin/dlq/cleanup?olderThanDays=7
```

## 🏥 Health & Monitoring

### ❤️ Health Checks
```http
# Store Service
GET /store-service/actuator/health

# Central Service  
GET /central-inventory-service/actuator/health
```

### 📊 Métricas Prometheus
```http
# Store Service
GET /store-service/actuator/prometheus

# Central Service
GET /central-inventory-service/actuator/prometheus
```

## 📮 Coleção Postman

### 🌍 Variáveis de Ambiente
```json
{
  "store_service_url": "http://localhost:8081",
  "central_service_url": "http://localhost:8082", 
  "store_id": "STORE-001",
  "customer_id": "customer-123"
}
```

### 🧪 Fluxo de Testes Recomendado

1. **🏥 Health Checks**
   ```bash
   # Store Service Health
   GET {{store_service_url}}/store-service/actuator/health
   
   # Central Service Health
   GET {{central_service_url}}/central-inventory-service/actuator/health
   ```

2. **📋 Consultar Dados**
   ```bash
   # Listar produtos
   GET {{store_service_url}}/api/v1/store/{{store_id}}/inventory/products
   
   # Inventário global
   GET {{central_service_url}}/api/v1/inventory/global/NOTEBOOK-001
   ```

3. **🛒 Fluxo de Compra Completo**
   ```bash
   # 1. Reservar produto
   POST {{store_service_url}}/api/v1/store/{{store_id}}/inventory/products/NOTEBOOK-001/reserve
   # Salvar reservation_id da resposta
   
   # 2. Confirmar reserva
   POST {{store_service_url}}/api/v1/store/{{store_id}}/inventory/products/NOTEBOOK-001/commit
   # Usar reservation_id salvo
   
   # 3. Verificar atualização
   GET {{central_service_url}}/api/v1/inventory/global/NOTEBOOK-001
   ```

### 📥 Importar no Postman

```bash
# Importar coleção e ambiente
# 1. Postman → Import
# 2. Selecionar arquivos:
#    - postman-collection.json
#    - postman-environment.json
# 3. Executar requests na ordem sugerida
```

## 📚 Documentação Interativa

### Swagger UI (OpenAPI 3)
- **Store Service**: http://localhost:8081/store-service/swagger-ui.html
- **Central Service**: http://localhost:8082/central-inventory-service/swagger-ui.html

### OpenAPI JSON
- **Store Service**: http://localhost:8081/store-service/v3/api-docs
- **Central Service**: http://localhost:8082/central-inventory-service/v3/api-docs

## 🔧 Códigos de Status HTTP

| Código | Significado | Exemplo de Uso |
|--------|-------------|----------------|
| **200** | OK | Operação bem-sucedida |
| **201** | Created | Reserva criada |
| **400** | Bad Request | Dados inválidos |
| **404** | Not Found | Produto não encontrado |
| **409** | Conflict | Produto já reservado |
| **422** | Unprocessable Entity | Estoque insuficiente |
| **500** | Internal Server Error | Erro interno |
| **503** | Service Unavailable | Sistema em manutenção |

## 🔍 Exemplos de Testes Avançados

### Teste de Concorrência
```bash
#!/bin/bash
# Testar concorrência com múltiplas requisições

echo "🔄 Testando concorrência..."

# Executar 10 reservas simultâneas do mesmo produto
for i in {1..10}; do
  curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
    -H "Content-Type: application/json" \
    -d "{\"quantity\": 1, \"customerId\": \"customer-${i}\"}" \
    -w "Request ${i}: %{http_code}\n" &
done

wait
echo "✅ Teste de concorrência concluído"
```

### Teste de Resiliência
```bash
#!/bin/bash
# Testar sistema de DLQ

echo "🛡️ Testando resiliência..."

# 1. Parar Kafka
docker-compose stop kafka

# 2. Fazer operação (deve ir para DLQ)
curl -X PUT "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/quantity" \
  -H "Content-Type: application/json" \
  -d '{"newQuantity": 100}'

# 3. Verificar DLQ
curl -s "http://localhost:8081/api/v1/admin/dlq/stats" | jq

# 4. Religar Kafka e processar
docker-compose start kafka
sleep 10
curl -X POST "http://localhost:8081/api/v1/admin/dlq/process-queue"

echo "✅ Teste de resiliência concluído"
```

---

**💡 Dicas:**
- Use `jq` para formatar JSON: `curl ... | jq`
- Salve variáveis entre requests: `RESERVATION_ID=$(curl ... | jq -r '.reservationId')`
- Execute testes em paralelo com `&` para simular concorrência
- Monitore métricas em tempo real no Grafana durante os testes
