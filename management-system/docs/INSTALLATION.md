# 🚀 Guia de Instalação e Execução

## 📋 Pré-requisitos

### Obrigatórios
- 🐳 **Docker** 24.0+ ([Instalar Docker](https://docs.docker.com/get-docker/))
- 🐙 **Docker Compose** 2.0+ (incluído no Docker Desktop)
- ☕ **Java 21** ([SDKMAN recomendado](https://sdkman.io/))
- 🔨 **Maven 3.9+** (para desenvolvimento local)

### Opcionais (para desenvolvimento)
- 💻 **IntelliJ IDEA** ou **VS Code** com extensões Java
- 📮 **Postman** para testes de API
- 🖥️ **Terminal/PowerShell** para comandos

## 🔧 Verificação do Ambiente

```bash
# Verificar versões
docker --version          # Docker version 24.0.7+
docker compose version    # Docker Compose version 2.21.0+
java --version            # Java 21.0.2+  
mvn --version             # Maven 3.9.5+

# Verificar portas disponíveis (não devem estar em uso)
lsof -i :5432   # PostgreSQL
lsof -i :6379   # Redis  
lsof -i :9092   # Kafka
lsof -i :8081   # Store Service
lsof -i :8082   # Central Service
lsof -i :9090   # Prometheus
lsof -i :3000   # Grafana
```

## 🐳 Opção 1: Execução Completa com Docker Compose (Recomendado)

### 1️⃣ Clone e Acesse o Projeto
```bash
git clone <repository-url>
cd management-system
```

### 2️⃣ Execute o Sistema Completo
```bash
# Construir e iniciar todos os serviços
docker-compose up --build -d

# Verificar status dos containers
docker-compose ps

# Acompanhar logs em tempo real
docker-compose logs -f
```

### 3️⃣ Aguardar Inicialização (⏱️ ~2-3 minutos)
```bash
# Verificar saúde dos serviços
curl http://localhost:8081/store-service/actuator/health
curl http://localhost:8082/central-inventory-service/actuator/health

# Status esperado: {"status":"UP"}
```

## 💻 Opção 2: Desenvolvimento Local

### 1️⃣ Infraestrutura Separada
```bash
# Subir apenas a infraestrutura (DB, Cache, Messaging)
docker-compose up -d postgres redis kafka zookeeper prometheus grafana

# Verificar se está rodando
docker-compose ps
```

### 2️⃣ Compilar Aplicações
```bash
# Store Service
cd store-service
mvn clean package -DskipTests
cd ..

# Central Inventory Service  
cd central-inventory-service
mvn clean package -DskipTests
cd ..
```

### 3️⃣ Executar Localmente
```bash
# Terminal 1 - Store Service
cd store-service
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run

# Terminal 2 - Central Service  
cd central-inventory-service
SPRING_PROFILES_ACTIVE=local mvn spring-boot:run
```

## 📊 Verificação de Funcionamento

### ✅ Health Checks
```bash
# Store Service
curl -s http://localhost:8081/store-service/actuator/health | jq

# Central Service  
curl -s http://localhost:8082/central-inventory-service/actuator/health | jq

# PostgreSQL
docker-compose exec postgres pg_isready -U inventory

# Redis
docker-compose exec redis redis-cli ping

# Kafka
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### 🎯 Teste Básico da API
```bash
# Buscar produtos da loja
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products" | jq

# Obter produto específico
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001" | jq

# Consulta global
curl -s "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" | jq
```

## 🔗 URLs de Acesso

| Serviço | URL | Credenciais | Descrição |
|---------|-----|-------------|-----------|
| 🏪 **Store Service** | http://localhost:8081 | N/A | API da loja |
| 🏢 **Central Service** | http://localhost:8082 | N/A | API central |
| 📖 **Swagger Store** | http://localhost:8081/store-service/swagger-ui.html | N/A | Documentação API |
| 📖 **Swagger Central** | http://localhost:8082/central-inventory-service/swagger-ui.html | N/A | Documentação API |
| 📈 **Grafana** | http://localhost:3000 | `admin/grafana123` | Dashboards e visualizações |
| 🔍 **Prometheus** | http://localhost:9090 | N/A | Métricas e alertas |

## 🧪 Testes de Validação

### Script de Teste Rápido
```bash
# Executar teste básico automatizado
./scripts/test-basic.sh

# Executar suite completa de testes
./scripts/test-complete.sh

# Testes de resiliência (DLQ)
./scripts/test-resilience.sh
```

### Teste Manual Completo
```bash
# 1. Health checks
curl http://localhost:8081/store-service/actuator/health
curl http://localhost:8082/central-inventory-service/actuator/health

# 2. Listar produtos
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products" | jq

# 3. Reservar produto
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2,
    "customerId": "customer-123",
    "reservationDuration": "PT30M"
  }' | jq

# 4. Verificar inventário global
curl -s "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" | jq
```

## 🛑 Parar o Sistema

```bash
# Parar todos os serviços
docker-compose down

# Parar e remover volumes (⚠️ apaga dados)
docker-compose down -v

# Parar e remover imagens
docker-compose down --rmi all
```

## 🐛 Troubleshooting Comum

### ❌ Porta já em uso
```bash
# Encontrar processo usando a porta
lsof -i :8081

# Parar processo específico
kill -9 <PID>
```

### ❌ Falta de memória
```bash
# Verificar uso de memória
docker stats

# Aumentar limite do Docker (Docker Desktop)
# Settings → Resources → Memory: 8GB+
```

### ❌ Containers não inicializam
```bash
# Ver logs detalhados
docker-compose logs <service-name>

# Forçar recriação
docker-compose up --build --force-recreate
```

### ❌ Erro de conectividade entre serviços
```bash
# Verificar rede Docker
docker network ls
docker network inspect management-system_inventory-network

# Testar conectividade
docker-compose exec store-service ping postgres
```

### 🔍 Informações para Suporte
```bash
# Coletar informações do ambiente
docker --version
docker-compose --version
java --version

# Status dos containers
docker-compose ps

# Logs recentes
docker-compose logs --tail=100 --timestamps

# Uso de recursos
docker stats --no-stream
```

## ⚡ Quick Start (Para Impacientes)

```bash
# Executar tudo de uma vez
docker-compose up --build -d && \
sleep 30 && \
curl -s http://localhost:8081/store-service/actuator/health | jq && \
echo "🎉 Sistema funcionando! Acesse: http://localhost:8081/store-service/swagger-ui.html"
```

## 🚀 Próximos Passos

Após a instalação bem-sucedida:

1. 📖 **Explore a API**: http://localhost:8081/store-service/swagger-ui.html
2. 📮 **Importe no Postman**: Use os arquivos `postman-collection.json` e `postman-environment.json`
3. 🧪 **Execute os testes**: Consulte [TESTING.md](./TESTING.md)
4. 📊 **Monitore o sistema**: http://localhost:3000 (Grafana)
5. 🌐 **Consulte as APIs**: Documentação completa no [README.md](./README.md)
