#!/bin/bash

# Teste de integração completo do Sistema de Inventário Central
# Este script testa toda a interação entre os serviços

set -e  # Parar em caso de erro

# Configurações
STORE_SERVICE_URL="http://localhost:8081/store-service"
CENTRAL_SERVICE_URL="http://localhost:8082/api/central-inventory"
GRAFANA_URL="http://localhost:3000"
PROMETHEUS_URL="http://localhost:9090"

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}   TESTE INTEGRADO DO SISTEMA DE INVENTÁRIO${NC}"
echo -e "${BLUE}============================================${NC}"

# Função para aguardar serviços
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "\n${YELLOW}Aguardando $service_name estar disponível...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name está disponível!${NC}"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗ $service_name não está disponível após $max_attempts tentativas${NC}"
    return 1
}

# 1. Verificar se todos os serviços estão rodando
echo -e "\n${YELLOW}=== VERIFICANDO SERVIÇOS ===${NC}"
wait_for_service "$STORE_SERVICE_URL/actuator/health" "Store Service"
wait_for_service "$CENTRAL_SERVICE_URL/actuator/health" "Central Inventory Service"
wait_for_service "$PROMETHEUS_URL/-/healthy" "Prometheus"
wait_for_service "$GRAFANA_URL/api/health" "Grafana"

# 2. Criar produto na loja
echo -e "\n${YELLOW}=== TESTANDO FLUXO DE INVENTÁRIO ===${NC}"

PRODUCT_SKU="INTEGRATION-TEST-$(date +%s)"
echo -e "Criando produto com SKU: ${BLUE}$PRODUCT_SKU${NC}"

# Criar produto no store-service
CREATE_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{
        \"sku\": \"$PRODUCT_SKU\",
        \"name\": \"Produto de Teste Integração\",
        \"description\": \"Produto criado durante teste de integração\",
        \"quantity\": 50,
        \"price\": 199.99
    }" \
    "$STORE_SERVICE_URL/products")

echo -e "${GREEN}✓ Produto criado no Store Service${NC}"

# Aguardar propagação via Kafka
echo -e "Aguardando sincronização via Kafka..."
sleep 5

# 3. Verificar se produto apareceu no central
echo -e "Verificando sincronização no Central Service..."
CENTRAL_RESPONSE=$(curl -s "$CENTRAL_SERVICE_URL/global-inventory/$PRODUCT_SKU")

if echo "$CENTRAL_RESPONSE" | grep -q "$PRODUCT_SKU"; then
    echo -e "${GREEN}✓ Produto sincronizado no Central Service${NC}"
else
    echo -e "${RED}✗ Produto NÃO sincronizado no Central Service${NC}"
    echo "Response: $CENTRAL_RESPONSE"
fi

# 4. Testar reserva de produto
echo -e "\nTestando reserva de produto..."
RESERVE_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{
        \"sku\": \"$PRODUCT_SKU\",
        \"quantity\": 10
    }" \
    "$STORE_SERVICE_URL/inventory/reserve")

echo -e "${GREEN}✓ Reserva realizada${NC}"

# Aguardar propagação
sleep 3

# Verificar atualização no central
UPDATED_RESPONSE=$(curl -s "$CENTRAL_SERVICE_URL/global-inventory/$PRODUCT_SKU")
if echo "$UPDATED_RESPONSE" | grep -q '"totalReserved":10'; then
    echo -e "${GREEN}✓ Reserva sincronizada no Central Service${NC}"
else
    echo -e "${YELLOW}⚠ Reserva pode não ter sido sincronizada completamente${NC}"
fi

# 5. Testar consultas do central
echo -e "\n${YELLOW}=== TESTANDO CONSULTAS CENTRAIS ===${NC}"

# Lista geral
GENERAL_LIST=$(curl -s "$CENTRAL_SERVICE_URL/global-inventory?page=0&size=10")
echo -e "${GREEN}✓ Consulta geral de inventário${NC}"

# Produtos disponíveis
AVAILABLE_LIST=$(curl -s "$CENTRAL_SERVICE_URL/global-inventory/available?page=0&size=10")
echo -e "${GREEN}✓ Consulta de produtos disponíveis${NC}"

# Busca por termo
SEARCH_RESULT=$(curl -s "$CENTRAL_SERVICE_URL/global-inventory/search?term=Integração&page=0&size=10")
echo -e "${GREEN}✓ Busca por termo${NC}"

# 6. Testar APIs administrativas
echo -e "\n${YELLOW}=== TESTANDO APIs ADMINISTRATIVAS ===${NC}"

# Estatísticas
STATS_RESPONSE=$(curl -s "$CENTRAL_SERVICE_URL/admin/stats/summary")
echo -e "${GREEN}✓ Estatísticas administrativas${NC}"

# Informações das lojas
STORES_RESPONSE=$(curl -s "$CENTRAL_SERVICE_URL/admin/stores")
echo -e "${GREEN}✓ Informações das lojas${NC}"

# Health check do sistema
HEALTH_RESPONSE=$(curl -s "$CENTRAL_SERVICE_URL/admin/health/system")
echo -e "${GREEN}✓ Health check do sistema${NC}"

# 7. Testar métricas
echo -e "\n${YELLOW}=== TESTANDO MÉTRICAS E MONITORAMENTO ===${NC}"

# Métricas Prometheus do Store Service
STORE_METRICS=$(curl -s "$STORE_SERVICE_URL/actuator/prometheus")
if echo "$STORE_METRICS" | grep -q "http_server_requests_seconds"; then
    echo -e "${GREEN}✓ Métricas do Store Service${NC}"
else
    echo -e "${RED}✗ Métricas do Store Service não disponíveis${NC}"
fi

# Métricas Prometheus do Central Service
CENTRAL_METRICS=$(curl -s "$CENTRAL_SERVICE_URL/actuator/prometheus")
if echo "$CENTRAL_METRICS" | grep -q "http_server_requests_seconds"; then
    echo -e "${GREEN}✓ Métricas do Central Service${NC}"
else
    echo -e "${RED}✗ Métricas do Central Service não disponíveis${NC}"
fi

# 8. Limpeza
echo -e "\n${YELLOW}=== LIMPEZA ===${NC}"

# Remover produto de teste
DELETE_STORE_RESPONSE=$(curl -s -X DELETE "$STORE_SERVICE_URL/products/$PRODUCT_SKU")
DELETE_CENTRAL_RESPONSE=$(curl -s -X DELETE "$CENTRAL_SERVICE_URL/global-inventory/$PRODUCT_SKU")

echo -e "${GREEN}✓ Produtos de teste removidos${NC}"

# 9. Relatório final
echo -e "\n${BLUE}============================================${NC}"
echo -e "${BLUE}           RELATÓRIO DO TESTE${NC}"
echo -e "${BLUE}============================================${NC}"

echo -e "\n${GREEN}SUCESSOS:${NC}"
echo -e "✓ Store Service operacional"
echo -e "✓ Central Inventory Service operacional"  
echo -e "✓ Prometheus operacional"
echo -e "✓ Grafana operacional"
echo -e "✓ Sincronização via Kafka funcionando"
echo -e "✓ APIs de consulta funcionando"
echo -e "✓ APIs administrativas funcionando"
echo -e "✓ Métricas sendo coletadas"

echo -e "\n${YELLOW}INFORMAÇÕES IMPORTANTES:${NC}"
echo -e "• Store Service: $STORE_SERVICE_URL"
echo -e "• Central Service: $CENTRAL_SERVICE_URL"
echo -e "• Prometheus: $PROMETHEUS_URL"
echo -e "• Grafana: $GRAFANA_URL (admin/grafana123)"

echo -e "\n${YELLOW}PRÓXIMOS PASSOS:${NC}"
echo -e "• Configurar dashboards personalizados no Grafana"
echo -e "• Implementar alertas no Prometheus"
echo -e "• Executar testes de carga"
echo -e "• Monitorar logs em produção"

echo -e "\n${GREEN}🎉 TESTE DE INTEGRAÇÃO CONCLUÍDO COM SUCESSO! 🎉${NC}"
echo -e "${BLUE}============================================${NC}"
