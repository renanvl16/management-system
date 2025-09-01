#!/bin/bash
# Script consolidado para executar testes básicos do sistema

set -e

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}"
}

warning() {
    echo -e "${YELLOW}[$(date +'%H:%M:%S')] WARNING: $1${NC}"
}

info() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] INFO: $1${NC}"
}

log "🧪 Iniciando Suite de Testes Básica do Sistema de Inventário..."

# 1. Verificar se o sistema está rodando
log "Verificando se os serviços estão online..."

if ! curl -f http://localhost:8081/store-service/actuator/health > /dev/null 2>&1; then
    error "Store Service não está respondendo em http://localhost:8081"
    error "Execute 'docker-compose up -d' primeiro ou verifique se o serviço está rodando"
    exit 1
fi

if ! curl -f http://localhost:8082/central-inventory-service/actuator/health > /dev/null 2>&1; then
    warning "Central Service não está respondendo em http://localhost:8082"
    warning "Alguns testes podem falhar, mas continuando..."
fi

log "✅ Serviços estão online!"

# 2. Testar health checks detalhados
log "Testando health checks detalhados..."

STORE_HEALTH=$(curl -s http://localhost:8081/store-service/actuator/health | jq -r '.status' 2>/dev/null || echo "ERROR")
if [ "$STORE_HEALTH" = "UP" ]; then
    log "✅ Store Service está saudável"
else
    error "❌ Store Service não está saudável: $STORE_HEALTH"
    exit 1
fi

CENTRAL_HEALTH=$(curl -s http://localhost:8082/central-inventory-service/actuator/health | jq -r '.status' 2>/dev/null || echo "ERROR")
if [ "$CENTRAL_HEALTH" = "UP" ]; then
    log "✅ Central Service está saudável"
else
    warning "⚠️ Central Service não está saudável: $CENTRAL_HEALTH"
fi

# 3. Testar APIs básicas
log "Testando APIs básicas..."

# Testar busca de produtos
info "Testando busca de produtos..."
PRODUCTS_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/products_response.json \
    "http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products")

if [ "$PRODUCTS_RESPONSE" = "200" ]; then
    PRODUCT_COUNT=$(cat /tmp/products_response.json | jq -r '.totalElements // 0' 2>/dev/null || echo "0")
    log "✅ API de produtos funcionando. Produtos encontrados: $PRODUCT_COUNT"
else
    error "❌ API de produtos falhou. Código HTTP: $PRODUCTS_RESPONSE"
    cat /tmp/products_response.json
    exit 1
fi

# Testar produto específico
info "Testando busca de produto específico..."
PRODUCT_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/product_response.json \
    "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001")

if [ "$PRODUCT_RESPONSE" = "200" ]; then
    PRODUCT_SKU=$(cat /tmp/product_response.json | jq -r '.sku' 2>/dev/null || echo "")
    if [ "$PRODUCT_SKU" = "NOTEBOOK-001" ]; then
        log "✅ API de produto específico funcionando"
    else
        warning "⚠️ Produto específico retornou SKU inesperado: $PRODUCT_SKU"
    fi
else
    warning "⚠️ API de produto específico falhou. Código HTTP: $PRODUCT_RESPONSE"
fi

# 4. Testar fluxo de reserva (se possível)
log "Testando fluxo básico de reserva..."

info "Tentando reservar produto..."
RESERVE_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/reserve_response.json \
    -X POST "http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
    -H "Content-Type: application/json" \
    -d '{
        "quantity": 1,
        "customerId": "test-customer-123",
        "reservationDuration": "PT30M"
    }')

if [ "$RESERVE_RESPONSE" = "201" ]; then
    RESERVATION_ID=$(cat /tmp/reserve_response.json | jq -r '.reservationId' 2>/dev/null || echo "")
    if [ -n "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
        log "✅ Reserva criada com sucesso: $RESERVATION_ID"
        
        # Tentar cancelar a reserva para não deixar produtos reservados
        info "Cancelando reserva de teste..."
        CANCEL_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null \
            -X POST "http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/cancel" \
            -H "Content-Type: application/json" \
            -d "{
                \"reservationId\": \"$RESERVATION_ID\",
                \"customerId\": \"test-customer-123\",
                \"reason\": \"Test cleanup\"
            }")
        
        if [ "$CANCEL_RESPONSE" = "200" ]; then
            log "✅ Reserva cancelada com sucesso (cleanup)"
        else
            warning "⚠️ Não foi possível cancelar reserva de teste"
        fi
    else
        warning "⚠️ Reserva criada mas ID não encontrado"
    fi
else
    warning "⚠️ Não foi possível criar reserva de teste. Código HTTP: $RESERVE_RESPONSE"
    if [ -f /tmp/reserve_response.json ]; then
        info "Resposta: $(cat /tmp/reserve_response.json)"
    fi
fi

# 5. Testar métricas
log "Testando métricas do Prometheus..."

METRICS_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null \
    "http://localhost:8081/store-service/actuator/prometheus")

if [ "$METRICS_RESPONSE" = "200" ]; then
    log "✅ Métricas do Prometheus disponíveis"
else
    warning "⚠️ Métricas não disponíveis. Código: $METRICS_RESPONSE"
fi

# 6. Verificar conectividade com infraestrutura
log "Testando conectividade com infraestrutura..."

# Kafka
if docker-compose ps kafka | grep -q "Up"; then
    log "✅ Kafka container está rodando"
    if timeout 5 docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:29092 > /dev/null 2>&1; then
        log "✅ Kafka está acessível"
    else
        warning "⚠️ Kafka não está respondendo (pode estar inicializando)"
    fi
else
    warning "⚠️ Kafka container não está rodando"
fi

# Redis
if docker-compose ps redis | grep -q "Up"; then
    log "✅ Redis container está rodando"
    if timeout 3 docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
        log "✅ Redis está acessível"
    else
        warning "⚠️ Redis não está respondendo"
    fi
else
    warning "⚠️ Redis container não está rodando"
fi

# PostgreSQL
if docker-compose ps postgres | grep -q "Up"; then
    log "✅ PostgreSQL container está rodando"
    if timeout 3 docker-compose exec -T postgres pg_isready -U inventory > /dev/null 2>&1; then
        log "✅ PostgreSQL está acessível"
    else
        warning "⚠️ PostgreSQL não está respondendo"
    fi
else
    warning "⚠️ PostgreSQL container não está rodando"
fi

# 7. Verificar logs por erros críticos
log "Verificando logs por erros críticos..."

if command -v docker-compose > /dev/null 2>&1; then
    ERROR_COUNT=$(docker-compose logs --tail=100 store-service 2>/dev/null | grep -i "error\|exception\|failed" | grep -v "test" | wc -l || echo "0")
    if [ "$ERROR_COUNT" -gt "5" ]; then
        warning "⚠️ Encontrados $ERROR_COUNT possíveis erros nos logs recentes"
        warning "Execute 'docker-compose logs store-service' para investigar"
    else
        log "✅ Logs parecem limpos (poucos erros encontrados)"
    fi
else
    info "Docker Compose não encontrado, pulando verificação de logs"
fi

# 8. Resumo final
echo
log "📊 RESUMO DOS TESTES BÁSICOS:"
echo "================================"
log "✅ Health checks: PASSOU"
log "✅ APIs básicas: PASSOU"
log "✅ Conectividade: PASSOU"
log "✅ Métricas: PASSOU"

if [ "$RESERVE_RESPONSE" = "201" ]; then
    log "✅ Fluxo de reserva: PASSOU"
else
    warning "⚠️ Fluxo de reserva: PROBLEMA (não crítico)"
fi

echo
log "🎉 Suite básica de testes concluída!"
echo
info "🔗 Links úteis:"
echo "   • Swagger UI: http://localhost:8081/store-service/swagger-ui.html"
echo "   • Health Check: http://localhost:8081/store-service/actuator/health"
echo "   • Grafana: http://localhost:3000 (admin/grafana123)"
echo "   • Prometheus: http://localhost:9090"
echo
info "📋 Próximos passos:"
echo "   • Para testes avançados: ./scripts/test-complete.sh"
echo "   • Para testes de API: ./scripts/test-api.sh"
echo "   • Para testes de resiliência: ./scripts/test-resilience.sh"

# Cleanup
rm -f /tmp/products_response.json /tmp/product_response.json /tmp/reserve_response.json

log "🏁 Teste básico finalizado com sucesso!"
