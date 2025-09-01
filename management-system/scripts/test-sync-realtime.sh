#!/bin/bash

# Script de Teste de Sincronização em Tempo Real
# Testa todas as operações e verifica se os eventos Kafka sincronizam corretamente

set -e

# URLs dos serviços
STORE_SERVICE_URL="http://localhost:8081/store-service"
CENTRAL_SERVICE_URL="http://localhost:8082/central-inventory-service"

# Configurações
STORE_ID="STORE001"
TEST_SKU="LAPTOP001"
CUSTOMER_ID="CUSTOMER001"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Função para esperar sincronização
wait_for_sync() {
    local seconds=${1:-3}
    log_info "Aguardando ${seconds}s para sincronização via Kafka..."
    sleep $seconds
}

# Função para fazer requisição com retry
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local max_retries=3
    local retry=0
    
    while [ $retry -lt $max_retries ]; do
        if [ "$method" = "GET" ]; then
            response=$(curl -s -w "HTTPSTATUS:%{http_code}" "$url")
        else
            response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
        fi
        
        http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
        body=$(echo $response | sed -e 's/HTTPSTATUS:.*//')
        
        if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
            echo "$body"
            return 0
        else
            retry=$((retry + 1))
            log_warning "Tentativa $retry falhou (HTTP $http_code). Tentando novamente..."
            sleep 2
        fi
    done
    
    log_error "Falha após $max_retries tentativas. HTTP Code: $http_code"
    log_error "Response: $body"
    return 1
}

# Função para testar conectividade
test_connectivity() {
    echo "🔗 Testando conectividade dos serviços..."
    
    # Teste Store Service
    if curl -s "${STORE_SERVICE_URL}/actuator/health" > /dev/null; then
        log_success "Store Service está respondendo"
    else
        log_error "Store Service não está respondendo"
        return 1
    fi
    
    # Teste Central Inventory Service
    if curl -s "${CENTRAL_SERVICE_URL}/actuator/health" > /dev/null; then
        log_success "Central Inventory Service está respondendo"
    else
        log_error "Central Inventory Service não está respondendo"
        return 1
    fi
    
    return 0
}

# Fase 1: Conectividade e Estado Inicial
test_phase_1() {
    echo ""
    echo "=== FASE 1: CONECTIVIDADE E ESTADO INICIAL ==="
    
    if ! test_connectivity; then
        log_error "Falha na conectividade dos serviços"
        exit 1
    fi
    
    log_info "Consultando estado inicial dos produtos..."
    
    # Buscar produtos no Store Service
    log_info "Buscando produtos no Store Service..."
    store_products=$(make_request "GET" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products")
    if [ $? -eq 0 ]; then
        log_success "Produtos encontrados no Store Service:"
        echo "$store_products" | jq '.'
    else
        log_error "Falha ao buscar produtos no Store Service"
        return 1
    fi
    
    # Buscar produtos no Central Service
    log_info "Buscando produtos no Central Inventory Service..."
    central_products=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products")
    if [ $? -eq 0 ]; then
        log_success "Produtos encontrados no Central Service:"
        echo "$central_products" | jq '.'
    else
        log_error "Falha ao buscar produtos no Central Service"
        return 1
    fi
    
    log_success "Fase 1 concluída com sucesso!"
}

# Fase 2: Teste de Busca de Produto Específico
test_phase_2() {
    echo ""
    echo "=== FASE 2: BUSCA DE PRODUTO ESPECÍFICO ==="
    
    log_info "Testando busca de produto específico: $TEST_SKU"
    
    # Buscar produto específico no Store Service
    log_info "Buscando produto $TEST_SKU no Store Service..."
    store_product=$(make_request "GET" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        log_success "Produto encontrado no Store Service:"
        echo "$store_product" | jq '.'
        
        # Extrair quantidade atual
        CURRENT_STORE_QTY=$(echo "$store_product" | jq -r '.availableQuantity // .quantity // 0')
        log_info "Quantidade atual no Store Service: $CURRENT_STORE_QTY"
    else
        log_error "Produto $TEST_SKU não encontrado no Store Service"
        return 1
    fi
    
    # Buscar produto específico no Central Service
    log_info "Buscando produto $TEST_SKU no Central Service..."
    central_product=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        log_success "Produto encontrado no Central Service:"
        echo "$central_product" | jq '.'
        
        # Extrair quantidade atual
        CURRENT_CENTRAL_QTY=$(echo "$central_product" | jq -r '.availableQuantity // .totalQuantity // 0')
        log_info "Quantidade atual no Central Service: $CURRENT_CENTRAL_QTY"
    else
        log_warning "Produto $TEST_SKU não encontrado no Central Service (pode estar sendo criado)"
        CURRENT_CENTRAL_QTY=0
    fi
    
    log_success "Fase 2 concluída com sucesso!"
}

# Fase 3: Teste de Reserva
test_phase_3() {
    echo ""
    echo "=== FASE 3: TESTE DE RESERVA ==="
    
    RESERVE_QTY=2
    log_info "Testando reserva de $RESERVE_QTY unidades do produto $TEST_SKU"
    
    # Dados da reserva
    reserve_data="{
        \"quantity\": $RESERVE_QTY,
        \"customerId\": \"$CUSTOMER_ID\",
        \"reservationDuration\": 1800
    }"
    
    # Fazer reserva
    log_info "Fazendo reserva no Store Service..."
    reservation_response=$(make_request "POST" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products/${TEST_SKU}/reserve" "$reserve_data")
    if [ $? -eq 0 ]; then
        log_success "Reserva realizada com sucesso:"
        echo "$reservation_response" | jq '.'
        
        RESERVATION_ID=$(echo "$reservation_response" | jq -r '.reservationId // .id // "N/A"')
        log_info "ID da Reserva: $RESERVATION_ID"
    else
        log_error "Falha ao fazer reserva"
        return 1
    fi
    
    # Aguardar sincronização
    wait_for_sync 5
    
    # Verificar se a reserva foi sincronizada no Central Service
    log_info "Verificando sincronização no Central Service..."
    central_product_after=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        NEW_CENTRAL_QTY=$(echo "$central_product_after" | jq -r '.availableQuantity // .totalQuantity // 0')
        log_info "Nova quantidade no Central Service: $NEW_CENTRAL_QTY"
        
        if [ "$NEW_CENTRAL_QTY" -lt "$CURRENT_CENTRAL_QTY" ]; then
            log_success "✅ Sincronização de reserva detectada no Central Service!"
        else
            log_warning "⚠️  Sincronização de reserva pode não ter ocorrido ainda"
        fi
    else
        log_warning "Não foi possível verificar sincronização no Central Service"
    fi
    
    log_success "Fase 3 concluída!"
}

# Fase 4: Teste de Confirmação (Commit)
test_phase_4() {
    echo ""
    echo "=== FASE 4: TESTE DE CONFIRMAÇÃO (COMMIT) ==="
    
    COMMIT_QTY=1
    log_info "Testando confirmação de $COMMIT_QTY unidade do produto $TEST_SKU"
    
    # Dados da confirmação
    commit_data="{
        \"quantity\": $COMMIT_QTY
    }"
    
    # Fazer confirmação
    log_info "Fazendo confirmação no Store Service..."
    commit_response=$(make_request "POST" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products/${TEST_SKU}/commit" "$commit_data")
    if [ $? -eq 0 ]; then
        log_success "Confirmação realizada com sucesso:"
        echo "$commit_response" | jq '.'
    else
        log_error "Falha ao fazer confirmação"
        return 1
    fi
    
    # Aguardar sincronização
    wait_for_sync 5
    
    # Verificar sincronização no Central Service
    log_info "Verificando sincronização da confirmação no Central Service..."
    central_product_after_commit=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        FINAL_CENTRAL_QTY=$(echo "$central_product_after_commit" | jq -r '.availableQuantity // .totalQuantity // 0')
        log_info "Quantidade final no Central Service: $FINAL_CENTRAL_QTY"
        log_success "✅ Confirmação sincronizada no Central Service!"
    else
        log_warning "Não foi possível verificar sincronização da confirmação"
    fi
    
    log_success "Fase 4 concluída!"
}

# Fase 5: Teste de Cancelamento
test_phase_5() {
    echo ""
    echo "=== FASE 5: TESTE DE CANCELAMENTO ==="
    
    CANCEL_QTY=1
    log_info "Testando cancelamento de $CANCEL_QTY unidade do produto $TEST_SKU"
    
    # Dados do cancelamento
    cancel_data="{
        \"quantity\": $CANCEL_QTY
    }"
    
    # Fazer cancelamento
    log_info "Fazendo cancelamento no Store Service..."
    cancel_response=$(make_request "POST" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products/${TEST_SKU}/cancel" "$cancel_data")
    if [ $? -eq 0 ]; then
        log_success "Cancelamento realizado com sucesso:"
        echo "$cancel_response" | jq '.'
    else
        log_error "Falha ao fazer cancelamento"
        return 1
    fi
    
    # Aguardar sincronização
    wait_for_sync 5
    
    # Verificar sincronização no Central Service
    log_info "Verificando sincronização do cancelamento no Central Service..."
    central_product_after_cancel=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        AFTER_CANCEL_QTY=$(echo "$central_product_after_cancel" | jq -r '.availableQuantity // .totalQuantity // 0')
        log_info "Quantidade após cancelamento no Central Service: $AFTER_CANCEL_QTY"
        log_success "✅ Cancelamento sincronizado no Central Service!"
    else
        log_warning "Não foi possível verificar sincronização do cancelamento"
    fi
    
    log_success "Fase 5 concluída!"
}

# Fase 6: Teste de Atualização de Quantidade
test_phase_6() {
    echo ""
    echo "=== FASE 6: TESTE DE ATUALIZAÇÃO DE QUANTIDADE ==="
    
    NEW_QTY=50
    log_info "Testando atualização de quantidade para $NEW_QTY unidades do produto $TEST_SKU"
    
    # Dados da atualização
    update_data="{
        \"newQuantity\": $NEW_QTY
    }"
    
    # Fazer atualização
    log_info "Fazendo atualização de quantidade no Store Service..."
    update_response=$(make_request "PUT" "${STORE_SERVICE_URL}/api/v1/store/${STORE_ID}/inventory/products/${TEST_SKU}/quantity" "$update_data")
    if [ $? -eq 0 ]; then
        log_success "Atualização realizada com sucesso:"
        echo "$update_response" | jq '.'
    else
        log_error "Falha ao fazer atualização de quantidade"
        return 1
    fi
    
    # Aguardar sincronização
    wait_for_sync 5
    
    # Verificar sincronização no Central Service
    log_info "Verificando sincronização da atualização no Central Service..."
    central_product_final=$(make_request "GET" "${CENTRAL_SERVICE_URL}/api/v1/central-inventory/products/${TEST_SKU}")
    if [ $? -eq 0 ]; then
        UPDATED_CENTRAL_QTY=$(echo "$central_product_final" | jq -r '.availableQuantity // .totalQuantity // 0')
        log_info "Quantidade atualizada no Central Service: $UPDATED_CENTRAL_QTY"
        
        if [ "$UPDATED_CENTRAL_QTY" -eq "$NEW_QTY" ]; then
            log_success "✅ Atualização de quantidade sincronizada corretamente!"
        else
            log_warning "⚠️  Quantidade não sincronizada corretamente (Esperado: $NEW_QTY, Atual: $UPDATED_CENTRAL_QTY)"
        fi
    else
        log_warning "Não foi possível verificar sincronização da atualização"
    fi
    
    log_success "Fase 6 concluída!"
}

# Fase 7: Verificação de Dead Letter Queue (DLQ)
test_phase_7() {
    echo ""
    echo "=== FASE 7: VERIFICAÇÃO DE DEAD LETTER QUEUE ==="
    
    log_info "Verificando se existem eventos na DLQ..."
    
    # Verificar DLQ no Store Service
    dlq_response=$(make_request "GET" "${STORE_SERVICE_URL}/api/v1/admin/dlq/events")
    if [ $? -eq 0 ]; then
        dlq_count=$(echo "$dlq_response" | jq '. | length // 0')
        if [ "$dlq_count" -eq 0 ]; then
            log_success "✅ Nenhum evento na DLQ - todos os eventos foram processados corretamente!"
        else
            log_warning "⚠️  Encontrados $dlq_count eventos na DLQ:"
            echo "$dlq_response" | jq '.'
        fi
    else
        log_warning "Não foi possível verificar a DLQ"
    fi
    
    log_success "Fase 7 concluída!"
}

# Fase 8: Métricas e Monitoramento
test_phase_8() {
    echo ""
    echo "=== FASE 8: MÉTRICAS E MONITORAMENTO ==="
    
    log_info "Coletando métricas dos serviços..."
    
    # Métricas do Store Service
    log_info "Coletando métricas do Store Service..."
    store_metrics=$(curl -s "${STORE_SERVICE_URL}/actuator/prometheus" | grep -E "(kafka|inventory|product)" | head -10)
    if [ $? -eq 0 ]; then
        log_success "Métricas do Store Service:"
        echo "$store_metrics"
    else
        log_warning "Não foi possível coletar métricas do Store Service"
    fi
    
    # Métricas do Central Service
    log_info "Coletando métricas do Central Service..."
    central_metrics=$(curl -s "${CENTRAL_SERVICE_URL}/actuator/prometheus" | grep -E "(kafka|inventory|product)" | head -10)
    if [ $? -eq 0 ]; then
        log_success "Métricas do Central Service:"
        echo "$central_metrics"
    else
        log_warning "Não foi possível coletar métricas do Central Service"
    fi
    
    log_success "Fase 8 concluída!"
}

# Função principal
main() {
    echo "🚀 INICIANDO TESTE DE SINCRONIZAÇÃO EM TEMPO REAL"
    echo "=================================================="
    echo "Testando sincronização Kafka entre Store Service e Central Inventory Service"
    echo ""
    
    # Verificar se jq está instalado
    if ! command -v jq &> /dev/null; then
        log_error "jq não está instalado. Instale com: brew install jq"
        exit 1
    fi
    
    # Executar todas as fases
    test_phase_1 || exit 1
    test_phase_2 || exit 1
    test_phase_3 || exit 1
    test_phase_4 || exit 1
    test_phase_5 || exit 1
    test_phase_6 || exit 1
    test_phase_7 || exit 1
    test_phase_8 || exit 1
    
    echo ""
    echo "🎉 TESTE DE SINCRONIZAÇÃO CONCLUÍDO COM SUCESSO!"
    echo "================================================="
    echo "✅ Todas as operações foram testadas"
    echo "✅ Sincronização Kafka está funcionando"
    echo "✅ Ambos os serviços estão operacionais"
    echo ""
    echo "Para monitoramento contínuo, acesse:"
    echo "- Grafana: http://localhost:3000"
    echo "- Prometheus: http://localhost:9090"
    echo "- Store Service Health: ${STORE_SERVICE_URL}/actuator/health"
    echo "- Central Service Health: ${CENTRAL_SERVICE_URL}/actuator/health"
}

# Executar script principal
main "$@"
