#!/bin/bash
# Script para testes específicos de resiliência e DLQ

set -e

# Cores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

log() { echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"; }
error() { echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}"; }
warning() { echo -e "${YELLOW}[$(date +'%H:%M:%S')] WARNING: $1${NC}"; }
info() { echo -e "${BLUE}[$(date +'%H:%M:%S')] INFO: $1${NC}"; }
title() { echo -e "${CYAN}[$(date +'%H:%M:%S')] === $1 ===${NC}"; }
step() { echo -e "${MAGENTA}[$(date +'%H:%M:%S')] STEP: $1${NC}"; }

title "🛡️ TESTE DE RESILIÊNCIA E SISTEMA DLQ"

# Verificar se o sistema está rodando
if ! curl -f http://localhost:8081/store-service/actuator/health > /dev/null 2>&1; then
    error "Store Service não está rodando. Execute 'docker-compose up -d' primeiro"
    exit 1
fi

log "✅ Store Service está online. Iniciando testes de resiliência..."
echo

# ===========================================
# FASE 1: VERIFICAR ESTADO INICIAL DO DLQ
# ===========================================
title "FASE 1: VERIFICANDO ESTADO INICIAL DO DLQ"

step "1.1 Obtendo estatísticas iniciais do DLQ"
DLQ_INITIAL_STATS=$(curl -s "http://localhost:8081/api/v1/admin/dlq/stats" 2>/dev/null || echo "{}")
INITIAL_PENDING=$(echo "$DLQ_INITIAL_STATS" | jq -r '.pendingEvents // 0' 2>/dev/null || echo "0")
INITIAL_TOTAL=$(echo "$DLQ_INITIAL_STATS" | jq -r '.totalEvents // 0' 2>/dev/null || echo "0")

log "Estado inicial do DLQ:"
log "  • Eventos pendentes: $INITIAL_PENDING"
log "  • Total de eventos: $INITIAL_TOTAL"
echo

# ===========================================
# FASE 2: SIMULAR FALHA DO KAFKA
# ===========================================
title "FASE 2: SIMULANDO FALHA DO KAFKA"

step "2.1 Parando Kafka temporariamente"
if docker-compose stop kafka > /dev/null 2>&1; then
    log "✅ Kafka parado com sucesso"
else
    warning "⚠️ Problema ao parar Kafka (pode já estar parado)"
fi

# Aguardar um pouco para garantir que o Kafka está realmente parado
sleep 5

step "2.2 Verificando se Kafka está realmente indisponível"
if timeout 3 docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:29092 --list > /dev/null 2>&1; then
    warning "⚠️ Kafka ainda parece estar respondendo"
else
    log "✅ Kafka está indisponível conforme esperado"
fi
echo

# ===========================================
# FASE 3: EXECUTAR OPERAÇÕES QUE DEVEM IR PARA DLQ
# ===========================================
title "FASE 3: EXECUTANDO OPERAÇÕES QUE DEVEM IR PARA DLQ"

step "3.1 Executando operações de atualização de quantidade (devem ir para DLQ)"

# Array de operações para testar
declare -a operations=(
    '{"newQuantity": 100, "reason": "DLQ Test 1"}'
    '{"newQuantity": 200, "reason": "DLQ Test 2"}'
    '{"newQuantity": 300, "reason": "DLQ Test 3"}'
)

OPERATIONS_SENT=0

for i in "${!operations[@]}"; do
    info "Enviando operação $((i + 1))/3..."
    
    RESPONSE_CODE=$(curl -s -w "%{http_code}" -o /dev/null \
        -X PUT "http://localhost:8081/api/v1/store/STORE-001/inventory/products/TEST-DLQ-00$((i + 1))/quantity" \
        -H "Content-Type: application/json" \
        -d "${operations[$i]}" 2>/dev/null || echo "000")
    
    if [ "$RESPONSE_CODE" = "200" ] || [ "$RESPONSE_CODE" = "202" ]; then
        log "✅ Operação $((i + 1)) enviada (código: $RESPONSE_CODE)"
        OPERATIONS_SENT=$((OPERATIONS_SENT + 1))
    else
        warning "⚠️ Operação $((i + 1)) falhou (código: $RESPONSE_CODE)"
    fi
    
    sleep 1
done

log "Operações enviadas: $OPERATIONS_SENT/3"
echo

step "3.2 Aguardando eventos serem processados pelo DLQ (10 segundos)"
sleep 10

# ===========================================
# FASE 4: VERIFICAR SE EVENTOS FORAM PARA DLQ
# ===========================================
title "FASE 4: VERIFICANDO EVENTOS NO DLQ"

step "4.1 Obtendo estatísticas atualizadas do DLQ"
DLQ_UPDATED_STATS=$(curl -s "http://localhost:8081/api/v1/admin/dlq/stats" 2>/dev/null || echo "{}")
CURRENT_PENDING=$(echo "$DLQ_UPDATED_STATS" | jq -r '.pendingEvents // 0' 2>/dev/null || echo "0")
CURRENT_TOTAL=$(echo "$DLQ_UPDATED_STATS" | jq -r '.totalEvents // 0' 2>/dev/null || echo "0")

log "Estado atual do DLQ:"
log "  • Eventos pendentes: $CURRENT_PENDING"
log "  • Total de eventos: $CURRENT_TOTAL"

# Calcular novos eventos
NEW_EVENTS=$((CURRENT_TOTAL - INITIAL_TOTAL))
NEW_PENDING=$((CURRENT_PENDING - INITIAL_PENDING))

if [ "$NEW_EVENTS" -gt 0 ]; then
    log "✅ $NEW_EVENTS novos eventos foram adicionados ao DLQ"
    if [ "$NEW_PENDING" -gt 0 ]; then
        log "✅ $NEW_PENDING eventos estão pendentes (aguardando retry)"
    fi
else
    warning "⚠️ Nenhum novo evento foi detectado no DLQ"
    warning "Isso pode indicar que o fallback DLQ não está funcionando"
fi
echo

step "4.2 Listando eventos pendentes no DLQ"
DLQ_EVENTS=$(curl -s "http://localhost:8081/api/v1/admin/dlq/events?status=PENDING&size=10" 2>/dev/null || echo "{}")
EVENT_COUNT=$(echo "$DLQ_EVENTS" | jq -r '.content | length' 2>/dev/null || echo "0")

if [ "$EVENT_COUNT" -gt 0 ]; then
    log "✅ $EVENT_COUNT eventos pendentes encontrados no DLQ"
    
    # Mostrar detalhes do primeiro evento
    FIRST_EVENT_TYPE=$(echo "$DLQ_EVENTS" | jq -r '.content[0].eventType // "N/A"' 2>/dev/null)
    FIRST_EVENT_RETRY=$(echo "$DLQ_EVENTS" | jq -r '.content[0].retryCount // 0' 2>/dev/null)
    
    info "Primeiro evento: Tipo=$FIRST_EVENT_TYPE, Tentativas=$FIRST_EVENT_RETRY"
else
    warning "⚠️ Nenhum evento pendente encontrado na listagem"
fi
echo

# ===========================================
# FASE 5: RELIGAR KAFKA
# ===========================================
title "FASE 5: RELIGANDO KAFKA"

step "5.1 Reiniciando Kafka"
if docker-compose start kafka > /dev/null 2>&1; then
    log "✅ Comando para reiniciar Kafka executado"
else
    error "❌ Falha ao executar comando para reiniciar Kafka"
    exit 1
fi

step "5.2 Aguardando Kafka inicializar (30 segundos)"
sleep 30

step "5.3 Verificando se Kafka está funcionando"
KAFKA_CHECK_ATTEMPTS=0
KAFKA_MAX_ATTEMPTS=6

while [ $KAFKA_CHECK_ATTEMPTS -lt $KAFKA_MAX_ATTEMPTS ]; do
    if timeout 10 docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:29092 --list > /dev/null 2>&1; then
        log "✅ Kafka está funcionando novamente"
        break
    else
        KAFKA_CHECK_ATTEMPTS=$((KAFKA_CHECK_ATTEMPTS + 1))
        if [ $KAFKA_CHECK_ATTEMPTS -lt $KAFKA_MAX_ATTEMPTS ]; then
            info "Tentativa $KAFKA_CHECK_ATTEMPTS/$KAFKA_MAX_ATTEMPTS - Kafka ainda inicializando..."
            sleep 10
        fi
    fi
done

if [ $KAFKA_CHECK_ATTEMPTS -eq $KAFKA_MAX_ATTEMPTS ]; then
    error "❌ Kafka não conseguiu reiniciar após múltiplas tentativas"
    error "Teste de recovery não pode continuar"
    exit 1
fi
echo

# ===========================================
# FASE 6: PROCESSAR FILA DLQ
# ===========================================
title "FASE 6: PROCESSANDO FILA DLQ"

step "6.1 Iniciando processamento manual da fila DLQ"
PROCESS_RESPONSE=$(curl -s -w "%{http_code}" -o /tmp/dlq_process_response.json \
    -X POST "http://localhost:8081/api/v1/admin/dlq/process-queue" 2>/dev/null || echo "000")

if [ "$PROCESS_RESPONSE" = "200" ] || [ "$PROCESS_RESPONSE" = "202" ]; then
    log "✅ Processamento da fila DLQ iniciado (código: $PROCESS_RESPONSE)"
else
    warning "⚠️ Falha ao iniciar processamento (código: $PROCESS_RESPONSE)"
    if [ -f /tmp/dlq_process_response.json ]; then
        warning "Resposta: $(cat /tmp/dlq_process_response.json)"
    fi
fi

step "6.2 Aguardando processamento (20 segundos)"
sleep 20

# ===========================================
# FASE 7: VERIFICAR RECOVERY
# ===========================================
title "FASE 7: VERIFICANDO RECOVERY"

step "7.1 Obtendo estatísticas finais do DLQ"
DLQ_FINAL_STATS=$(curl -s "http://localhost:8081/api/v1/admin/dlq/stats" 2>/dev/null || echo "{}")
FINAL_PENDING=$(echo "$DLQ_FINAL_STATS" | jq -r '.pendingEvents // 0' 2>/dev/null || echo "0")
FINAL_SUCCEEDED=$(echo "$DLQ_FINAL_STATS" | jq -r '.succeededEvents // 0' 2>/dev/null || echo "0")
FINAL_FAILED=$(echo "$DLQ_FINAL_STATS" | jq -r '.failedEvents // 0' 2>/dev/null || echo "0")

log "Estado final do DLQ:"
log "  • Eventos pendentes: $FINAL_PENDING"
log "  • Eventos processados com sucesso: $FINAL_SUCCEEDED"
log "  • Eventos com falha permanente: $FINAL_FAILED"

# Análise de recovery
RECOVERED_EVENTS=$((FINAL_SUCCEEDED - (INITIAL_TOTAL > 0 ? $(echo "$DLQ_INITIAL_STATS" | jq -r '.succeededEvents // 0' 2>/dev/null || echo "0") : 0)))

if [ "$RECOVERED_EVENTS" -gt 0 ]; then
    log "✅ $RECOVERED_EVENTS eventos foram recuperados com sucesso!"
elif [ "$FINAL_PENDING" -lt "$CURRENT_PENDING" ]; then
    log "✅ Redução de eventos pendentes detectada (recovery em progresso)"
else
    warning "⚠️ Nenhum evento foi recuperado ainda"
    warning "O processo de recovery pode estar em andamento ou com problemas"
fi
echo

# ===========================================
# RELATÓRIO FINAL
# ===========================================
title "📊 RELATÓRIO DE TESTE DE RESILIÊNCIA"
echo

log "=== RESUMO DA EXECUÇÃO ==="
log "Operações enviadas durante falha do Kafka: $OPERATIONS_SENT/3"
log "Novos eventos no DLQ: $NEW_EVENTS"
log "Eventos recuperados: $RECOVERED_EVENTS"
log "Status final - Pendentes: $FINAL_PENDING, Sucesso: $FINAL_SUCCEEDED, Falha: $FINAL_FAILED"
echo

# Avaliação geral
TOTAL_SCORE=0
MAX_SCORE=5

# 1. Operações foram enviadas mesmo com Kafka offline
if [ "$OPERATIONS_SENT" -gt 0 ]; then
    log "✅ 1. Sistema manteve disponibilidade durante falha do Kafka"
    TOTAL_SCORE=$((TOTAL_SCORE + 1))
else
    error "❌ 1. Sistema não manteve disponibilidade durante falha"
fi

# 2. Eventos foram para DLQ
if [ "$NEW_EVENTS" -gt 0 ]; then
    log "✅ 2. Sistema DLQ capturou eventos durante falha"
    TOTAL_SCORE=$((TOTAL_SCORE + 1))
else
    error "❌ 2. Sistema DLQ não funcionou adequadamente"
fi

# 3. Kafka foi religado com sucesso
if [ $KAFKA_CHECK_ATTEMPTS -lt $KAFKA_MAX_ATTEMPTS ]; then
    log "✅ 3. Kafka foi religado com sucesso"
    TOTAL_SCORE=$((TOTAL_SCORE + 1))
else
    error "❌ 3. Problemas ao religar Kafka"
fi

# 4. API de processamento funcionou
if [ "$PROCESS_RESPONSE" = "200" ] || [ "$PROCESS_RESPONSE" = "202" ]; then
    log "✅ 4. API de processamento DLQ funcionou"
    TOTAL_SCORE=$((TOTAL_SCORE + 1))
else
    error "❌ 4. API de processamento DLQ com problemas"
fi

# 5. Recovery foi bem-sucedido
if [ "$RECOVERED_EVENTS" -gt 0 ] || [ "$FINAL_PENDING" -lt "$CURRENT_PENDING" ]; then
    log "✅ 5. Sistema de recovery funcionou"
    TOTAL_SCORE=$((TOTAL_SCORE + 1))
else
    warning "⚠️ 5. Sistema de recovery precisa de mais tempo ou tem problemas"
fi

echo
log "=== AVALIAÇÃO GERAL ==="
log "Score: $TOTAL_SCORE/$MAX_SCORE"

if [ "$TOTAL_SCORE" -eq "$MAX_SCORE" ]; then
    log "🎉 EXCELENTE! Sistema de resiliência funcionando perfeitamente!"
    echo
    log "✅ Todos os componentes de resiliência estão operacionais:"
    log "   • Fallback para DLQ durante falhas"
    log "   • Manutenção de disponibilidade"
    log "   • Recovery automático"
    log "   • APIs administrativas funcionais"
elif [ "$TOTAL_SCORE" -ge 3 ]; then
    log "✅ BOM! Sistema de resiliência está funcionando bem"
    warning "Alguns aspectos podem precisar de atenção (veja detalhes acima)"
else
    error "❌ PROBLEMA! Sistema de resiliência precisa de correções"
    error "Vários componentes não estão funcionando adequadamente"
fi

echo
info "🔧 Para investigação adicional:"
info "   • Logs do Store Service: docker-compose logs store-service"
info "   • APIs DLQ: http://localhost:8081/store-service/swagger-ui.html"
info "   • Métricas: http://localhost:9090 (Prometheus)"
echo

# Cleanup
rm -f /tmp/dlq_process_response.json

log "🏁 Teste de resiliência finalizado!"

# Exit code baseado no score
if [ "$TOTAL_SCORE" -ge 3 ]; then
    exit 0
else
    exit 1
fi
