# 🚀 Guia de Execução - Sistema de Gerenciamento de Inventário

Este documento fornece instruções **passo a passo** para executar o sistema completo do gerenciamento de inventário distribuído.

## 📋 Pré-requisitos

### **Requisitos Mínimos**
```bash
# Sistema Operacional
✅ macOS 10.15+ / Ubuntu 20.04+ / Windows 10+

# Software Base
✅ Docker Desktop 24.0+ 
✅ Docker Compose 2.0+
✅ Git 2.30+

# Recursos de Hardware
✅ RAM: 8GB mínimo (16GB recomendado)
✅ CPU: 4 cores mínimo
✅ Disco: 10GB espaço livre
✅ Rede: Conexão estável para download das imagens
```

### **Verificar Pré-requisitos**
```bash
# Verificar versões instaladas
docker --version
# Docker version 24.0.0+

docker-compose --version  
# Docker Compose version 2.0.0+

git --version
# git version 2.30.0+

# Verificar recursos disponíveis
docker system info | grep -E 'CPUs|Total Memory'
```

## 🛠️ Passo 1: Preparação do Ambiente

### **1.1 Clonar o Repositório**
```bash
# Clonar o projeto
git clone <repository-url>
cd management-system

# Verificar estrutura
ls -la
# Deve mostrar: docker-compose.yml, store-service/, central-inventory-service/, etc.
```

### **1.2 Verificar Portas Disponíveis**
```bash
# Verificar se as portas necessárias estão livres
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis  
lsof -i :9092  # Kafka
lsof -i :2181  # Zookeeper
lsof -i :8081  # Store Service
lsof -i :8082  # Central Service
lsof -i :9090  # Prometheus
lsof -i :3000  # Grafana

# Se alguma porta estiver ocupada:
kill -9 <PID>  # Parar processo conflitante
```

### **1.3 Configurar Recursos do Docker**
```bash
# Aumentar recursos do Docker Desktop (se necessário)
# macOS: Docker Desktop > Settings > Resources
# - Memory: 8GB+
# - CPUs: 4+
# - Disk: 10GB+

# Linux: Verificar limite de memória
cat /sys/fs/cgroup/memory/memory.limit_in_bytes
```

## ⚡ Passo 2: Execução Rápida (Quick Start)

### **2.1 Executar Sistema Completo**
```bash
# Subir toda a infraestrutura
docker-compose up --build -d

# Verificar status dos containers
docker-compose ps

# Acompanhar logs de inicialização
docker-compose logs -f
```

### **2.2 Aguardar Inicialização**
```bash
# Aguardar todos os serviços ficarem "healthy" (~2-3 minutos)
watch -n 5 'docker-compose ps'

# Quando todos mostrarem "healthy" ou "running", prosseguir
```

### **2.3 Verificação Rápida**
```bash
# Testar conectividade básica
./scripts/test-basic.sh

# Se tudo estiver funcionando, você verá:
# ✅ Store Service: OK
# ✅ Central Service: OK  
# ✅ PostgreSQL: OK
# ✅ Redis: OK
# ✅ Kafka: OK
# ✅ Prometheus: OK
# ✅ Grafana: OK
```

## 🔍 Passo 3: Verificação Detalhada

### **3.1 Health Checks**
```bash
# Store Service
curl -s http://localhost:8081/store-service/actuator/health | jq
# Deve retornar: {"status":"UP"}

# Central Service  
curl -s http://localhost:8082/central-inventory-service/actuator/health | jq
# Deve retornar: {"status":"UP"}

# Kafka (verificar topics)
docker-compose exec kafka kafka-topics --bootstrap-server localhost:29092 --list
# Deve mostrar: inventory-update, inventory-reserve, inventory-commit
```

### **3.2 Verificar Dados de Exemplo**
```bash
# Listar produtos disponíveis
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products" | jq

# Deve retornar algo como:
# [
#   {
#     "sku": "NOTEBOOK-001",
#     "name": "Notebook Dell XPS",
#     "quantity": 15,
#     "reservedQuantity": 0
#   }
# ]
```

### **3.3 Testar Operação Básica**
```bash
# Reservar um produto
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 1,
    "customerId": "customer-123",
    "reservationDuration": "PT30M"
  }' | jq

# Verificar inventário global  
curl -s "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" | jq
```

## 🌐 Passo 4: Acessar Interfaces Web

### **4.1 APIs e Documentação**
```bash
# Abrir Swagger UI - Store Service
open http://localhost:8081/store-service/swagger-ui.html

# Abrir Swagger UI - Central Service
open http://localhost:8082/central-inventory-service/swagger-ui.html
```

### **4.2 Ferramentas de Monitoramento**
```bash
# Grafana (Dashboards)
open http://localhost:3000
# Login: admin / grafana123

# Prometheus (Métricas)
open http://localhost:9090

# Verificar métricas customizadas
curl -s http://localhost:8081/store-service/actuator/prometheus | grep "dlq_events"
```

### **4.3 Importar Postman Collection**
```bash
# 1. Abrir Postman
# 2. Import > Upload File
# 3. Selecionar: postman-collection.json
# 4. Import > Upload File  
# 5. Selecionar: postman-environment.json
# 6. Executar requests na sequência sugerida
```

## 🧪 Passo 5: Execução de Testes

### **5.1 Testes Básicos**
```bash
# Teste básico de funcionamento
./scripts/test-basic.sh

# Saída esperada:
# 🏪 Testing Store Service...
# ✅ Health check passed
# ✅ Product listing passed
# ✅ Product detail passed
```

### **5.2 Suite Completa de Testes**
```bash
# Testes completos (APIs + Integração)
./scripts/test-complete.sh

# Saída esperada:
# 🧪 Running Complete Test Suite...
# ✅ Store Service API Tests: 25/25 passed
# ✅ Central Service API Tests: 18/18 passed  
# ✅ Integration Tests: 12/12 passed
# ✅ Performance Tests: 8/8 passed
```

### **5.3 Testes de Resiliência**
```bash
# Teste do sistema DLQ (Dead Letter Queue)
./scripts/test-resilience.sh

# Este teste:
# 1. Para o Kafka temporariamente
# 2. Executa operações (que vão para DLQ)
# 3. Reinicia o Kafka
# 4. Verifica se eventos são processados automaticamente
```

### **5.4 Testes Unitários (Opcional)**
```bash
# Executar testes unitários - Store Service
cd store-service
mvn clean test

# Executar testes unitários - Central Service
cd ../central-inventory-service  
mvn clean test

# Ver relatório de cobertura
open target/site/jacoco/index.html
```

## 📊 Passo 6: Explorar Funcionalidades

### **6.1 Operações de Inventário**
```bash
# Cenário: Cliente fazendo uma compra

# 1. Consultar produtos disponíveis
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products" | jq

# 2. Reservar produto para carrinho
RESERVATION=$(curl -s -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 1,
    "customerId": "customer-456", 
    "reservationDuration": "PT30M"
  }')

# 3. Confirmar compra
echo $RESERVATION | jq -r '.reservationId' | xargs -I {} \
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "{}",
    "customerId": "customer-456"
  }'
```

### **6.2 Monitoramento e Métricas**
```bash
# Ver métricas de negócio no Grafana:
# http://localhost:3000 → Dashboards → Application Metrics

# Consultar métricas específicas no Prometheus:
# http://localhost:9090 → Graph

# Queries úteis:
rate(http_requests_total[1m])                    # Request rate
histogram_quantile(0.95, http_request_duration_seconds)  # P95 latência
dlq_events_total{status="pending"}              # Eventos pendentes DLQ
```

### **6.3 Administração do Sistema**
```bash
# APIs administrativas

# 1. Estatísticas do DLQ
curl -s "http://localhost:8081/api/v1/admin/dlq/stats" | jq

# 2. Forçar processamento de eventos pendentes
curl -X POST "http://localhost:8081/api/v1/admin/dlq/process-queue"

# 3. Listar eventos na fila
curl -s "http://localhost:8081/api/v1/admin/dlq/events?status=PENDING" | jq

# 4. Reconciliação manual no central service
curl -X POST "http://localhost:8082/api/v1/inventory/reconcile" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "STORE-001",
    "sku": "NOTEBOOK-001"
  }'
```

## 🐛 Solução de Problemas

### **Problema 1: Containers Não Sobem**
```bash
# Verificar logs detalhados
docker-compose logs <nome-do-servico>

# Exemplos:
docker-compose logs postgres
docker-compose logs kafka
docker-compose logs store-service

# Forçar recriação
docker-compose down
docker-compose up --build --force-recreate -d
```

### **Problema 2: Serviços Não Respondem**
```bash
# Verificar conectividade entre containers
docker-compose exec store-service ping postgres
docker-compose exec store-service ping kafka
docker-compose exec store-service ping redis

# Verificar configurações de rede
docker network inspect management-system_inventory-network
```

### **Problema 3: Erro de Portas Ocupadas**
```bash
# Identificar processo usando a porta
lsof -i :8081
lsof -i :5432

# Parar processos conflitantes
kill -9 <PID>

# Ou usar portas alternativas (modificar docker-compose.yml)
```

### **Problema 4: Falta de Recursos**
```bash
# Verificar uso de recursos
docker stats --no-stream

# Liberar espaço em disco
docker system prune -a

# Verificar logs de memória
dmesg | grep -i memory
```

### **Problema 5: Kafka Não Conecta**
```bash
# Aguardar Kafka inicializar completamente
docker-compose logs -f kafka

# Verificar tópicos foram criados
docker-compose exec kafka kafka-topics --bootstrap-server localhost:29092 --list

# Recriar tópicos se necessário
docker-compose exec kafka kafka-topics --bootstrap-server localhost:29092 --create --topic inventory-update --partitions 3 --replication-factor 1
```

## 🔄 Operações de Manutenção

### **Parar Sistema**
```bash
# Parar serviços (preservar dados)
docker-compose down

# Parar e remover volumes (⚠️ perde dados)
docker-compose down -v

# Limpeza completa  
docker-compose down --rmi all -v
docker system prune -a
```

### **Backup de Dados**
```bash
# Backup do PostgreSQL
docker-compose exec postgres pg_dump -U inventory_user inventory_db > backup.sql

# Backup dos volumes
docker run --rm -v management-system_postgres_data:/source -v $(pwd):/backup alpine \
  tar czf /backup/postgres_backup.tar.gz -C /source .
```

### **Logs e Debugging**
```bash
# Seguir logs em tempo real
docker-compose logs -f --tail=100

# Logs de serviço específico
docker-compose logs -f store-service

# Entrar no container para debug
docker-compose exec store-service /bin/bash
```

### **Métricas de Performance**
```bash
# Estatísticas de performance
curl -s "http://localhost:8081/store-service/actuator/metrics" | jq

# JVM metrics específicas
curl -s "http://localhost:8081/store-service/actuator/metrics/jvm.memory.used" | jq
curl -s "http://localhost:8081/store-service/actuator/metrics/hikaricp.connections.active" | jq
```

## ✅ Checklist de Validação

Após seguir todos os passos, verifique:

- [ ] ✅ Todos os containers estão "healthy"
- [ ] ✅ APIs respondem com status 200
- [ ] ✅ Swagger UI carrega corretamente  
- [ ] ✅ Grafana mostra dados nos dashboards
- [ ] ✅ Prometheus coleta métricas
- [ ] ✅ Testes básicos passam (./scripts/test-basic.sh)
- [ ] ✅ Operação de reserva/commit funciona
- [ ] ✅ DLQ processa eventos corretamente
- [ ] ✅ Logs não mostram erros críticos

## 🆘 Obter Suporte

Se encontrar problemas:

1. **Verificar logs**: `docker-compose logs -f`
2. **Consultar troubleshooting**: [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)  
3. **Executar diagnóstico**: `./scripts/diagnostic.sh`
4. **Criar issue**: Com logs e informações do sistema

---

**🎉 Parabéns! Seu sistema está rodando!** 

Acesse: http://localhost:8081/store-service/swagger-ui.html e comece a explorar as APIs.
