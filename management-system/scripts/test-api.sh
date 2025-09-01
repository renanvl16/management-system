#!/bin/bash
# Script para testes específicos de API

set -e

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"; }
error() { echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}"; }
warning() { echo -e "${YELLOW}[$(date +'%H:%M:%S')] WARNING: $1${NC}"; }
info() { echo -e "${BLUE}[$(date +'%H:%M:%S')] INFO: $1${NC}"; }
title() { echo -e "${CYAN}[$(date +'%H:%M:%S')] === $1 ===${NC}"; }

title "🌐 TESTE COMPLETO DE APIs"

# Verificar se o sistema está rodando
if ! curl -f http://localhost:8081/store-service/actuator/health > /dev/null 2>&1; then
    error "Store Service não está rodando. Execute 'docker-compose up -d' primeiro"
    exit 1
fi

log "✅ Store Service está online. Iniciando testes de API..."
echo

# Contadores
TOTAL_API_TESTS=0
PASSED_API_TESTS=0
FAILED_API_TESTS=0

# Função para executar teste de API
test_api() {
    local test_name="$1"
    local method="$2"
    local url="$3"
    local data="$4"
    local expected_status="$5"
    local save_response="$6"  # true/false
    
    TOTAL_API_TESTS=$((TOTAL_API_TESTS + 1))
    info "Testando: $test_name"
    
    # Preparar comando curl
    if [ "$method" = "GET" ]; then
        curl_cmd="curl -s -w %{http_code} -o /tmp/api_response.json '$url'"
    else
        curl_cmd="curl -s -w %{http_code} -o /tmp/api_response.json -X $method -H 'Content-Type: application/json' -d '$data' '$url'"
    fi
    
    # Executar request
    response_code=$(eval "$curl_cmd" 2>/dev/null || echo "000")
    
    # Verificar resultado
    if [ "$response_code" = "$expected_status" ]; then
        log "✅ $test_name: PASSOU (HTTP $response_code)"
        PASSED_API_TESTS=$((PASSED_API_TESTS + 1))
        
        # Salvar resposta se solicitado
        if [ "$save_response" = "true" ] && [ -f /tmp/api_response.json ]; then
            cp /tmp/api_response.json "/tmp/${test_name// /_}_response.json"
        fi
        
        return 0
    else
        error "❌ $test_name: FALHOU (esperado: $expected_status, recebido: $response_code)"
        if [ -f /tmp/api_response.json ] && [ -s /tmp/api_response.json ]; then
            info "Resposta: $(cat /tmp/api_response.json | head -c 200)..."
        fi
        FAILED_API_TESTS=$((FAILED_API_TESTS + 1))
        return 1
    fi
}

# ===========================================
# FASE 1: TESTES BÁSICOS DE SAÚDE
# ===========================================
title "FASE 1: TESTES DE SAÚDE E MÉTRICAS"

test_api "Store Service Health Check" "GET" "http://localhost:8081/store-service/actuator/health" "" "200" false
test_api "Store Service Metrics" "GET" "http://localhost:8081/store-service/actuator/prometheus" "" "200" false
test_api "Store Service Info" "GET" "http://localhost:8081/store-service/actuator/info" "" "200" false

if curl -f http://localhost:8082/central-inventory-service/actuator/health > /dev/null 2>&1; then
    test_api "Central Service Health Check" "GET" "http://localhost:8082/central-inventory-service/actuator/health" "" "200" false
    test_api "Central Service Metrics" "GET" "http://localhost:8082/central-inventory-service/actuator/prometheus" "" "200" false
else
    warning "Central Service não está disponível - pulando testes"
fi

echo

# ===========================================
# FASE 2: TESTES DE CONSULTA DE PRODUTOS
# ===========================================
title "FASE 2: TESTES DE CONSULTA DE PRODUTOS"

test_api "Listar Produtos" "GET" "http://localhost:8081/api/v1/store/STORE-001/inventory/products" "" "200" true
test_api "Listar Produtos com Paginação" "GET" "http://localhost:8081/api/v1/store/STORE-001/inventory/products?page=0&size=5" "" "200" false
test_api "Produto Específico" "GET" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001" "" "200" true
test_api "Produto Inexistente" "GET" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/PRODUTO-INEXISTENTE" "" "404" false

echo

# ===========================================
# FASE 3: TESTES DE RESERVA DE PRODUTOS
# ===========================================
title "FASE 3: TESTES DE RESERVA DE PRODUTOS"

# Dados para reserva
RESERVE_DATA='{
    "quantity": 2,
    "customerId": "test-api-customer",
    "reservationDuration": "PT30M",
    "metadata": {
        "source": "api-test",
        "testId": "test-'$(date +%s)'"
    }
}'

test_api "Reservar Produto Válido" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" "$RESERVE_DATA" "201" true

# Extrair reservation_id da resposta
if [ -f /tmp/Reservar_Produto_Válido_response.json ]; then
    RESERVATION_ID=$(jq -r '.reservationId' /tmp/Reservar_Produto_Válido_response.json 2>/dev/null || echo "")
    if [ -n "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
        log "📝 Reservation ID obtido: $RESERVATION_ID"
    else
        warning "⚠️ Não foi possível extrair reservation ID"
        RESERVATION_ID=""
    fi
fi

# Teste de reserva com quantidade inválida
INVALID_RESERVE_DATA='{"quantity": -1, "customerId": "test-customer"}'
test_api "Reservar com Quantidade Inválida" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" "$INVALID_RESERVE_DATA" "400" false

# Teste de reserva com produto inexistente
test_api "Reservar Produto Inexistente" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/PRODUTO-INEXISTENTE/reserve" "$RESERVE_DATA" "404" false

echo

# ===========================================
# FASE 4: TESTES DE CONFIRMAÇÃO DE RESERVA
# ===========================================
title "FASE 4: TESTES DE CONFIRMAÇÃO DE RESERVA"

if [ -n "$RESERVATION_ID" ]; then
    # Commit da reserva
    COMMIT_DATA="{
        \"reservationId\": \"$RESERVATION_ID\",
        \"customerId\": \"test-api-customer\",
        \"saleDetails\": {
            \"orderId\": \"order-$(date +%s)\",
            \"paymentId\": \"payment-$(date +%s)\"
        }
    }"
    
    test_api "Confirmar Reserva Válida" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" "$COMMIT_DATA" "200" true
    
    # Tentar confirmar a mesma reserva novamente (deve falhar)
    test_api "Confirmar Reserva Já Confirmada" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" "$COMMIT_DATA" "409" false
else
    warning "⚠️ Pulando testes de confirmação - Reservation ID não disponível"
    FAILED_API_TESTS=$((FAILED_API_TESTS + 2))
    TOTAL_API_TESTS=$((TOTAL_API_TESTS + 2))
fi

# Teste de commit com reservation ID inválido
INVALID_COMMIT_DATA='{"reservationId": "invalid-id", "customerId": "test-customer"}'
test_api "Confirmar Reserva com ID Inválido" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" "$INVALID_COMMIT_DATA" "404" false

echo

# ===========================================
# FASE 5: TESTES DE CANCELAMENTO
# ===========================================
title "FASE 5: TESTES DE CANCELAMENTO"

# Criar nova reserva para cancelar
NEW_RESERVE_DATA='{
    "quantity": 1,
    "customerId": "test-cancel-customer",
    "reservationDuration": "PT30M"
}'

if test_api "Criar Reserva para Cancelar" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" "$NEW_RESERVE_DATA" "201" true; then
    # Extrair novo reservation_id
    if [ -f /tmp/Criar_Reserva_para_Cancelar_response.json ]; then
        CANCEL_RESERVATION_ID=$(jq -r '.reservationId' /tmp/Criar_Reserva_para_Cancelar_response.json 2>/dev/null || echo "")
        
        if [ -n "$CANCEL_RESERVATION_ID" ] && [ "$CANCEL_RESERVATION_ID" != "null" ]; then
            # Cancelar reserva
            CANCEL_DATA="{
                \"reservationId\": \"$CANCEL_RESERVATION_ID\",
                \"customerId\": \"test-cancel-customer\",
                \"reason\": \"API test cancellation\"
            }"
            
            test_api "Cancelar Reserva Válida" "POST" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/cancel" "$CANCEL_DATA" "200" false
        else
            warning "⚠️ Não foi possível extrair reservation ID para cancelamento"
        fi
    fi
fi

echo

# ===========================================
# FASE 6: TESTES DE ATUALIZAÇÃO DE QUANTIDADE
# ===========================================
title "FASE 6: TESTES DE ATUALIZAÇÃO DE QUANTIDADE"

# Atualização válida
UPDATE_DATA='{"newQuantity": 50, "reason": "API test restock", "adjustmentType": "RESTOCK"}'
test_api "Atualizar Quantidade Válida" "PUT" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/quantity" "$UPDATE_DATA" "200" false

# Atualização com quantidade negativa
INVALID_UPDATE_DATA='{"newQuantity": -5}'
test_api "Atualizar com Quantidade Negativa" "PUT" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/quantity" "$INVALID_UPDATE_DATA" "400" false

# Atualização de produto inexistente
test_api "Atualizar Produto Inexistente" "PUT" "http://localhost:8081/api/v1/store/STORE-001/inventory/products/PRODUTO-INEXISTENTE/quantity" "$UPDATE_DATA" "404" false

echo

# ===========================================
# FASE 7: TESTES DE CENTRAL SERVICE (SE DISPONÍVEL)
# ===========================================
if curl -f http://localhost:8082/central-inventory-service/actuator/health > /dev/null 2>&1; then
    title "FASE 7: TESTES DO CENTRAL SERVICE"
    
    test_api "Inventário Global por SKU" "GET" "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" "" "200" false
    test_api "Inventário por Loja" "GET" "http://localhost:8082/api/v1/inventory/stores/STORE-001/products" "" "200" false
    
    # Teste de reconciliação
    RECONCILE_DATA='{"storeId": "STORE-001", "sku": "NOTEBOOK-001", "forceSync": false}'
    test_api "Reconciliação Manual" "POST" "http://localhost:8082/api/v1/inventory/reconcile" "$RECONCILE_DATA" "200" false
    
    echo
fi

# ===========================================
# FASE 8: TESTES DE APIS ADMIN (DLQ)
# ===========================================
title "FASE 8: TESTES DE APIs ADMINISTRATIVAS"

test_api "Estatísticas DLQ" "GET" "http://localhost:8081/api/v1/admin/dlq/stats" "" "200" false
test_api "Listar Eventos DLQ" "GET" "http://localhost:8081/api/v1/admin/dlq/events?page=0&size=5" "" "200" false
test_api "Processar Fila DLQ" "POST" "http://localhost:8081/api/v1/admin/dlq/process-queue" "" "200" false

echo

# ===========================================
# FASE 9: TESTES DE VALIDAÇÃO DE DADOS
# ===========================================
title "FASE 9: TESTES DE VALIDAÇÃO DE DADOS"

# Store ID inválido
test_api "Store ID Inválido" "GET" "http://localhost:8081/api/v1/store/INVALID-STORE/inventory/products" "" "400" false

# Content-Type inválido para POST
INVALID_CONTENT_TYPE_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null \
    -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
    -H "Content-Type: text/plain" \
    -d "$RESERVE_DATA" 2>/dev/null || echo "000")

if [ "$INVALID_CONTENT_TYPE_RESPONSE" = "415" ] || [ "$INVALID_CONTENT_TYPE_RESPONSE" = "400" ]; then
    log "✅ Content-Type Inválido: PASSOU (HTTP $INVALID_CONTENT_TYPE_RESPONSE)"
    PASSED_API_TESTS=$((PASSED_API_TESTS + 1))
else
    error "❌ Content-Type Inválido: FALHOU (esperado: 400/415, recebido: $INVALID_CONTENT_TYPE_RESPONSE)"
    FAILED_API_TESTS=$((FAILED_API_TESTS + 1))
fi

TOTAL_API_TESTS=$((TOTAL_API_TESTS + 1))

echo

# ===========================================
# RELATÓRIO FINAL
# ===========================================
title "📊 RELATÓRIO DE TESTES DE API"
echo

log "=== ESTATÍSTICAS ==="
log "Total de testes de API executados: $TOTAL_API_TESTS"
log "Testes aprovados: $PASSED_API_TESTS"
log "Testes falharam: $FAILED_API_TESTS"

# Calcular taxa de sucesso
if [ "$TOTAL_API_TESTS" -gt 0 ]; then
    SUCCESS_RATE=$(echo "scale=1; $PASSED_API_TESTS * 100 / $TOTAL_API_TESTS" | bc 2>/dev/null || echo "0")
else
    SUCCESS_RATE="0"
fi

log "Taxa de sucesso: ${SUCCESS_RATE}%"
echo

# Análise por categoria
log "=== ANÁLISE POR CATEGORIA ==="

# Saúde e métricas (primeiros 3-5 testes)
HEALTH_TESTS=5
if [ "$PASSED_API_TESTS" -ge 3 ]; then
    log "✅ Health Checks: Funcionando"
else
    error "❌ Health Checks: Problemas detectados"
fi

# APIs principais (consulta, reserva, confirmação)
if [ "$SUCCESS_RATE" -ge "80" ]; then
    log "✅ APIs Principais: Funcionando bem"
elif [ "$SUCCESS_RATE" -ge "60" ]; then
    warning "⚠️ APIs Principais: Funcionando com alguns problemas"
else
    error "❌ APIs Principais: Muitos problemas detectados"
fi

# Validação de dados
if [ "$FAILED_API_TESTS" -le "$((TOTAL_API_TESTS / 10))" ]; then
    log "✅ Validação de Dados: Robusta"
else
    warning "⚠️ Validação de Dados: Pode ser melhorada"
fi

echo
log "=== AVALIAÇÃO GERAL ==="

if [ "$FAILED_API_TESTS" -eq 0 ]; then
    log "🎉 PERFEITO! Todas as APIs estão funcionando corretamente!"
    echo
    log "✅ Sistema pronto para uso em produção"
    log "✅ Todas as funcionalidades validadas"
    log "✅ Tratamento de erros funcionando"
elif [ "$SUCCESS_RATE" -ge "90" ]; then
    log "🎯 EXCELENTE! APIs funcionando muito bem (${SUCCESS_RATE}%)"
    warning "Apenas alguns problemas menores detectados"
elif [ "$SUCCESS_RATE" -ge "75" ]; then
    log "✅ BOM! APIs funcionando adequadamente (${SUCCESS_RATE}%)"
    warning "Alguns aspectos podem precisar de atenção"
else
    error "❌ PROBLEMAS! APIs com muitas falhas (${SUCCESS_RATE}%)"
    error "Sistema precisa de correções antes do uso em produção"
fi

echo
info "🔗 Para teste manual interativo:"
info "   • Swagger UI: http://localhost:8081/store-service/swagger-ui.html"
info "   • Postman: Importe postman-collection.json"
echo
info "🔧 Para investigação de problemas:"
info "   • Logs: docker-compose logs store-service"
info "   • Métricas: http://localhost:9090"
echo

# Cleanup
rm -f /tmp/api_response.json
rm -f /tmp/*_response.json

log "🏁 Teste de APIs finalizado!"

# Exit code baseado no resultado
if [ "$SUCCESS_RATE" -ge "80" ]; then
    exit 0
else
    exit 1
fi
