#!/bin/bash
# Script consolidado para testes completos do sistema

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

title "🧪 SUITE COMPLETA DE TESTES - SISTEMA DE INVENTÁRIO"

# Contadores para estatísticas
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

run_test() {
    local test_name="$1"
    local test_command="$2"
    local critical="$3"  # true/false
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    info "Executando: $test_name"
    
    if eval "$test_command" > /dev/null 2>&1; then
        log "✅ $test_name: PASSOU"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        if [ "$critical" = "true" ]; then
            error "❌ $test_name: FALHOU (CRÍTICO)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
            return 1
        else
            warning "⚠️ $test_name: FALHOU (NÃO CRÍTICO)"
            FAILED_TESTS=$((FAILED_TESTS + 1))
            return 0
        fi
    fi
}

# ======================================
# 1. TESTES DE INFRAESTRUTURA
# ======================================
title "1. TESTANDO INFRAESTRUTURA"

# Verificar se containers estão rodando
run_test "PostgreSQL Container" "docker-compose ps postgres | grep -q 'Up'" true
run_test "Redis Container" "docker-compose ps redis | grep -q 'Up'" true
run_test "Kafka Container" "docker-compose ps kafka | grep -q 'Up'" true

# Testar conectividade
run_test "PostgreSQL Conectividade" "timeout 3 docker-compose exec -T postgres pg_isready -U inventory" false
run_test "Redis Conectividade" "timeout 3 docker-compose exec -T redis redis-cli ping | grep -q PONG" false

# ======================================
# 2. TESTES DE APLICAÇÃO
# ======================================
title "2. TESTANDO APLICAÇÕES"

# Health checks
run_test "Store Service Health" "curl -f http://localhost:8081/store-service/actuator/health" true
run_test "Central Service Health" "curl -f http://localhost:8082/central-inventory-service/actuator/health" false

# Métricas
run_test "Store Service Metrics" "curl -f http://localhost:8081/store-service/actuator/prometheus" false
run_test "Central Service Metrics" "curl -f http://localhost:8082/central-inventory-service/actuator/prometheus" false

# ======================================
# 3. TESTES DE API - STORE SERVICE
# ======================================
title "3. TESTANDO APIs - STORE SERVICE"

# API de produtos
run_test "Listar Produtos" "curl -f http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products" true
run_test "Produto Específico" "curl -f http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001" true

# Teste de reserva completo
log "Testando fluxo completo de reserva..."

# Criar reserva
RESERVATION_RESPONSE=$(mktemp)
if curl -s -o "$RESERVATION_RESPONSE" -w "%{http_code}" \
    -X POST "http://localhost:8081/store-service/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
    -H "Content-Type: application/json" \
    -d '{
        "quantity": 1,
        "customerId": "test-customer-complete",
        "reservationDuration": "PT30M"
    }' | grep -q "201"; then
    
    RESERVATION_ID=$(jq -r '.reservationId' < "$RESERVATION_RESPONSE" 2>/dev/null || echo "")
    
    if [ -n "$RESERVATION_ID" ] && [ "$RESERVATION_ID" != "null" ]; then
        log "✅ Reserva criada: $RESERVATION_ID"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Testar commit da reserva
        if curl -s -w "%{http_code}" -o /dev/null \
            -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" \
            -H "Content-Type: application/json" \
            -d "{
                \"reservationId\": \"$RESERVATION_ID\",
                \"customerId\": \"test-customer-complete\"
            }" | grep -q "200"; then
            log "✅ Commit de reserva: PASSOU"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            warning "⚠️ Commit de reserva: FALHOU"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        warning "⚠️ Reserva criada mas ID inválido"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
else
    error "❌ Falha ao criar reserva"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 2))
rm -f "$RESERVATION_RESPONSE"

# ======================================
# 4. TESTES DE API - CENTRAL SERVICE
# ======================================
title "4. TESTANDO APIs - CENTRAL SERVICE"

run_test "Inventário Global" "curl -f http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" false

# ======================================
# 5. TESTES DE CONCORRÊNCIA
# ======================================
title "5. TESTANDO CONTROLE DE CONCORRÊNCIA"

log "Executando 20 requisições concorrentes..."

# Criar arquivo temporário para resultados
CONCURRENT_RESULTS=$(mktemp)

# Executar requisições concorrentes
for i in {1..20}; do
    {
        curl -s -w "%{http_code}\n" -o /dev/null \
            -X PUT "http://localhost:8081/api/v1/store/STORE-001/inventory/products/LAPTOP-001/quantity" \
            -H "Content-Type: application/json" \
            -d "{\"newQuantity\": $((50 + i))}" >> "$CONCURRENT_RESULTS"
    } &
done

# Aguardar todas as requisições
wait

# Analisar resultados
SUCCESS_COUNT=$(grep -c "200" "$CONCURRENT_RESULTS" 2>/dev/null || echo "0")
TOTAL_CONCURRENT=20

if [ "$SUCCESS_COUNT" -gt "$((TOTAL_CONCURRENT * 80 / 100))" ]; then
    log "✅ Concorrência: $SUCCESS_COUNT/$TOTAL_CONCURRENT requisições bem-sucedidas"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    warning "⚠️ Concorrência: apenas $SUCCESS_COUNT/$TOTAL_CONCURRENT bem-sucedidas"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))
rm -f "$CONCURRENT_RESULTS"

# ======================================
# 6. TESTE DE RESILIÊNCIA (DLQ)
# ======================================
title "6. TESTANDO SISTEMA DE RESILIÊNCIA (DLQ)"

info "Verificando se DLQ está funcionando..."

# Verificar estatísticas do DLQ
if curl -f -s "http://localhost:8081/api/v1/admin/dlq/stats" > /dev/null 2>&1; then
    log "✅ API do DLQ está acessível"
    PASSED_TESTS=$((PASSED_TESTS + 1))
    
    # Tentar processar fila DLQ
    if curl -f -s -X POST "http://localhost:8081/api/v1/admin/dlq/process-queue" > /dev/null 2>&1; then
        log "✅ Processamento do DLQ: PASSOU"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        warning "⚠️ Processamento do DLQ: FALHOU"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
else
    warning "⚠️ API do DLQ não está acessível"
    FAILED_TESTS=$((FAILED_TESTS + 2))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 2))

# ======================================
# 7. TESTES DE INTEGRAÇÃO COM KAFKA
# ======================================
title "7. TESTANDO INTEGRAÇÃO COM KAFKA"

if docker-compose ps kafka | grep -q "Up"; then
    # Verificar se tópicos existem
    if timeout 10 docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:29092 --list > /dev/null 2>&1; then
        log "✅ Kafka está respondendo"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        
        # Listar tópicos
        TOPICS=$(timeout 10 docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:29092 --list 2>/dev/null || echo "")
        if echo "$TOPICS" | grep -q "inventory"; then
            log "✅ Tópicos de inventário encontrados"
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            warning "⚠️ Tópicos de inventário não encontrados"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        warning "⚠️ Kafka não está respondendo"
        FAILED_TESTS=$((FAILED_TESTS + 2))
    fi
else
    warning "⚠️ Kafka container não está rodando"
    FAILED_TESTS=$((FAILED_TESTS + 2))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 2))

# ======================================
# 8. TESTES DE PERFORMANCE BÁSICOS
# ======================================
title "8. TESTANDO PERFORMANCE BÁSICA"

log "Medindo latência de APIs..."

# Testar latência da API principal
RESPONSE_TIME=$(curl -w "%{time_total}" -s -o /dev/null "http://localhost:8081/api/v1/store/STORE-001/inventory/products" 2>/dev/null || echo "999")

# Converter para milissegundos
RESPONSE_TIME_MS=$(echo "$RESPONSE_TIME * 1000" | bc 2>/dev/null || echo "999")
RESPONSE_TIME_INT=${RESPONSE_TIME_MS%.*}

if [ "$RESPONSE_TIME_INT" -lt 1000 ]; then
    log "✅ Latência da API: ${RESPONSE_TIME_INT}ms (< 1000ms)"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    warning "⚠️ Latência da API: ${RESPONSE_TIME_INT}ms (> 1000ms)"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))

# ======================================
# 9. VERIFICAÇÃO DE LOGS
# ======================================
title "9. VERIFICANDO LOGS"

# Verificar erros críticos nos logs
ERROR_COUNT=$(docker-compose logs --tail=200 store-service 2>/dev/null | grep -i "error\|exception\|failed" | grep -v -i "test\|debug" | wc -l || echo "0")

if [ "$ERROR_COUNT" -lt 10 ]; then
    log "✅ Logs limpos: $ERROR_COUNT erros encontrados (< 10)"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    warning "⚠️ Muitos erros nos logs: $ERROR_COUNT erros encontrados"
    warning "Execute 'docker-compose logs store-service' para investigar"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

TOTAL_TESTS=$((TOTAL_TESTS + 1))

# ======================================
# RELATÓRIO FINAL
# ======================================
echo
title "📊 RELATÓRIO FINAL - SUITE COMPLETA DE TESTES"
echo
log "Total de Testes Executados: $TOTAL_TESTS"
log "Testes Aprovados: $PASSED_TESTS"

if [ "$FAILED_TESTS" -gt 0 ]; then
    warning "Testes com Falha: $FAILED_TESTS"
else
    log "Testes com Falha: $FAILED_TESTS"
fi

# Calcular percentual de sucesso
SUCCESS_RATE=$(echo "scale=1; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc 2>/dev/null || echo "0")
echo
log "Taxa de Sucesso: ${SUCCESS_RATE}%"

if [ "$FAILED_TESTS" -eq 0 ]; then
    echo
    log "🎉 TODOS OS TESTES PASSARAM! Sistema funcionando perfeitamente!"
    echo
    info "🌟 O sistema está pronto para uso!"
    info "🔗 Acesse: http://localhost:8081/store-service/swagger-ui.html"
    echo
elif [ "$SUCCESS_RATE" -ge "80" ]; then
    echo
    log "✅ Sistema está funcionando bem (${SUCCESS_RATE}% de sucesso)"
    warning "Algumas funcionalidades podem ter problemas menores"
    echo
    info "💡 Sistema pode ser usado, mas verifique os warnings acima"
    echo
else
    echo
    error "❌ Sistema tem problemas significativos (${SUCCESS_RATE}% de sucesso)"
    error "Recomendamos investigar as falhas antes de usar em produção"
    echo
    info "🔧 Para investigar problemas:"
    info "   • Verifique logs: docker-compose logs"
    info "   • Teste serviços individuais: ./scripts/test-basic.sh"
    info "   • Verifique infraestrutura: docker-compose ps"
    echo
fi

# Links úteis sempre no final
info "🔗 Links úteis:"
echo "   • Store API: http://localhost:8081/store-service/swagger-ui.html"
echo "   • Central API: http://localhost:8082/central-inventory-service/swagger-ui.html"
echo "   • Grafana: http://localhost:3000 (admin/grafana123)"
echo "   • Prometheus: http://localhost:9090"
echo

log "🏁 Suite completa de testes finalizada!"

# Exit com código baseado no resultado
if [ "$FAILED_TESTS" -eq 0 ]; then
    exit 0
elif [ "$SUCCESS_RATE" -ge "80" ]; then
    exit 0  # Sucesso com warnings
else
    exit 1  # Falha significativa
fi
