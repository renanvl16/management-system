# 🧪 Guia Completo de Testes

## 🎯 Estratégia de Testes

O sistema implementa uma **pirâmide de testes** completa:

```
                    🔺 E2E Tests
                   /            \
                  /   🧪 API      \
                 /    Tests       \
                /________________\
               /                  \
              /   🔧 Integration   \
             /      Tests          \
            /______________________\
           /                        \
          /     🏗️ Unit Tests        \
         /        (70%)              \
        /__________________________\
```

## 1️⃣ Testes Unitários

### Executar Testes Unitários
```bash
# Store Service
cd store-service
mvn test

# Central Service
cd central-inventory-service
mvn test

# Executar com cobertura
mvn clean test jacoco:report

# Visualizar relatório
open target/site/jacoco/index.html
```

### Exemplo de Teste de Domínio
```java
@ExtendWith(MockitoExtension.class)
class InventoryDomainServiceTest {
    
    @Test
    void shouldReserveProductWhenAvailable() {
        // Given
        var product = Product.builder()
            .sku("NOTEBOOK-001")
            .quantity(10)
            .reservedQuantity(2)
            .build();
            
        // When
        var result = inventoryService.reserveProduct(product, 3);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(product.getAvailableQuantity()).isEqualTo(5);
    }
}
```

## 2️⃣ Testes de Integração

### Executar com TestContainers
```bash
# Testes de integração (requer Docker)
cd store-service
mvn verify -Pintegration-tests

# Profile específico para integração
mvn test -Pintegration-tests
```

### Exemplo com TestContainers
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class StoreServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @Test
    void shouldSearchProductsSuccessfully() {
        // Given
        var storeId = "STORE-001";
        
        // When
        var response = restTemplate.getForEntity(
            "/api/v1/store/{storeId}/inventory/products", 
            ProductListResponse.class, 
            storeId
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getProducts()).isNotEmpty();
    }
}
```

## 3️⃣ Testes de API

### Scripts Automatizados
```bash
# Executar teste básico
./scripts/test-basic.sh

# Teste completo de funcionalidades
./scripts/test-complete.sh

# Teste de resiliência e DLQ
./scripts/test-resilience.sh

# Teste de concorrência
./scripts/test-concurrency.sh
```

### Teste Manual com cURL
```bash
# 1. Health Check
curl -s http://localhost:8081/store-service/actuator/health | jq

# 2. Listar produtos
curl -s "http://localhost:8081/api/v1/store/STORE-001/inventory/products" | jq

# 3. Reservar produto
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/reserve" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2,
    "customerId": "customer-123",
    "reservationDuration": "PT30M"
  }' | jq '.reservationId'

# 4. Confirmar reserva (usar reservationId do passo anterior)
curl -X POST "http://localhost:8081/api/v1/store/STORE-001/inventory/products/NOTEBOOK-001/commit" \
  -H "Content-Type: application/json" \
  -d '{
    "reservationId": "RESERVATION_ID_AQUI",
    "customerId": "customer-123"
  }' | jq

# 5. Verificar inventário global
curl -s "http://localhost:8082/api/v1/inventory/global/NOTEBOOK-001" | jq
```

## 4️⃣ Testes de Resiliência (DLQ)

### Script de Teste de Resiliência
```bash
#!/bin/bash
# Testar sistema de DLQ e resiliência

echo "🛡️ Testando Sistema de Resiliência..."

# 1. Parar Kafka temporariamente
echo "⏸️  Parando Kafka..."
docker-compose stop kafka

# 2. Fazer operações que devem ir para DLQ
echo "📤 Enviando eventos para DLQ..."
curl -X PUT "http://localhost:8081/api/v1/store/STORE-001/inventory/products/KEYBOARD-001/quantity" \
  -H "Content-Type: application/json" \
  -d '{"newQuantity": 50}'

# 3. Verificar DLQ
echo "📊 Verificando estatísticas do DLQ..."
curl -s "http://localhost:8081/api/v1/admin/dlq/stats" | jq

# 4. Religar Kafka
echo "▶️  Religando Kafka..."
docker-compose start kafka
sleep 30

# 5. Processar fila DLQ
echo "🔄 Processando fila DLQ..."
curl -X POST "http://localhost:8081/api/v1/admin/dlq/process-queue"

# 6. Verificar se eventos foram processados
echo "✅ Verificando processamento..."
curl -s "http://localhost:8081/api/v1/admin/dlq/stats" | jq
```

## 5️⃣ Testes de Concorrência

### Teste de Concorrência Distribuída
```bash
#!/bin/bash
# Teste de concorrência com múltiplas lojas

echo "⚙️ Testando Controle de Concorrência..."

# Função para simular atualização concorrente
test_concurrent_update() {
    local store_id=$1
    local product_sku=$2
    local new_quantity=$3
    
    curl -X PUT "http://localhost:8081/api/v1/store/${store_id}/inventory/products/${product_sku}/quantity" \
      -H "Content-Type: application/json" \
      -d "{\"newQuantity\": ${new_quantity}}" \
      -w "%{http_code}\n" -o /dev/null -s
}

# Executar 20 requisições concorrentes para 5 lojas diferentes
echo "🚀 Executando 100 requisições concorrentes..."
for i in {1..5}; do
    for j in {1..20}; do
        test_concurrent_update "STORE-00${i}" "LAPTOP-001" $((50 + j)) &
    done
done

# Aguardar todas as requisições terminarem
wait

# Verificar resultados
echo "📊 Verificando resultados de concorrência..."
curl -s "http://localhost:8082/api/v1/inventory/metrics/versioning" | jq
```

## 6️⃣ Testes BDD (Cucumber)

### Executar Testes BDD
```bash
# Testes comportamentais
cd store-service
mvn test -Dcucumber.options="--tags @inventory"

# Relatório HTML
mvn test -Dcucumber.options="--plugin html:target/cucumber-reports"
```

### Exemplo de Feature
```gherkin
# src/test/resources/features/inventory.feature
Feature: Gestão de Inventário da Loja

  Background:
    Given uma loja "STORE-001" com os seguintes produtos:
      | sku         | name              | quantity | price  |
      | NOTEBOOK-001| Notebook Dell     | 10       | 2499.99|
      | MOUSE-001   | Mouse Logitech    | 25       | 349.99 |

  @inventory @search
  Scenario: Buscar produtos disponíveis
    When eu busco produtos da loja "STORE-001"
    Then eu devo receber uma lista com 2 produtos
    And o produto "NOTEBOOK-001" deve estar disponível

  @inventory @reserve
  Scenario: Reservar produto com estoque disponível
    When eu reservo 3 unidades do produto "NOTEBOOK-001" da loja "STORE-001"
    Then a reserva deve ser bem-sucedida
    And o estoque disponível deve ser 7 unidades
    And um evento de reserva deve ser publicado no Kafka
```

## 7️⃣ Testes de Performance

### JMeter Load Testing
```bash
# Instalar JMeter
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.2.tgz
tar -xzf apache-jmeter-5.6.2.tgz

# Executar teste de carga
./apache-jmeter-5.6.2/bin/jmeter -n -t tests/load-test.jmx -l results.jtl

# Gerar relatório
./apache-jmeter-5.6.2/bin/jmeter -g results.jtl -o report/
```

### Métricas de Performance Esperadas
- **Latência P95**: < 100ms para operações locais
- **Throughput**: > 1000 RPS por instância
- **Error Rate**: < 0.1%
- **Memory Usage**: < 512MB heap

## 8️⃣ Cobertura de Testes

### Verificar Cobertura
```bash
# Gerar relatório de cobertura
mvn clean test jacoco:report

# Ver relatório consolidado
mvn jacoco:report-aggregate
```

### Metas de Cobertura
- **Domain Layer**: 95%+
- **Application Layer**: 90%+
- **Infrastructure Layer**: 80%+
- **Overall**: 85%+

## 9️⃣ Testes com Postman

### Importar Coleção
1. Abrir Postman
2. Importar `postman-collection.json`
3. Importar `postman-environment.json`
4. Executar testes na ordem:
   - Health Checks
   - Listar Produtos
   - Reservar Produto
   - Confirmar Reserva
   - Verificar Inventário

### Executar Coleção via CLI
```bash
# Instalar Newman (CLI do Postman)
npm install -g newman

# Executar coleção completa
newman run postman-collection.json -e postman-environment.json

# Executar com relatório HTML
newman run postman-collection.json -e postman-environment.json -r htmlextra
```

## 🔧 Configuração de Debugging

### Logs Detalhados
```bash
# Habilitar logs de debug
export LOGGING_LEVEL_COM_INVENTORY=DEBUG

# Debug específico para SQL
export LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG

# Debug para retry e concorrência
export LOGGING_LEVEL_RETRY=DEBUG
```

### Remote Debugging
```bash
# Adicionar JVM args no Docker
JAVA_OPTS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005"

# Conectar IDE na porta 5005
```

## 📊 Métricas de Teste

### Prometheus Queries para Testes
```promql
# Taxa de sucesso dos testes
rate(test_executions_total{result="success"}[5m])

# Latência de testes de API
histogram_quantile(0.95, api_test_duration_seconds)

# Cobertura de código
code_coverage_percentage{module="store-service"}
```

## ✅ Checklist de Validação

Antes de considerar os testes completos, verifique:

- [ ] ✅ Todos os testes unitários passam
- [ ] ✅ Testes de integração com containers funcionam
- [ ] ✅ APIs respondem corretamente
- [ ] ✅ Sistema de DLQ funciona (teste de resiliência)
- [ ] ✅ Controle de concorrência resolve conflitos
- [ ] ✅ Métricas estão sendo coletadas
- [ ] ✅ Logs estruturados estão funcionando
- [ ] ✅ Performance está dentro dos SLAs
- [ ] ✅ Cobertura de código > 85%
- [ ] ✅ Documentação está atualizada

## 🚀 CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
      - name: Run tests
        run: |
          mvn clean test
          ./scripts/test-basic.sh
          ./scripts/test-resilience.sh
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 📞 Suporte

Para problemas com testes:

1. 🔍 Verifique os logs: `docker-compose logs`
2. 🧪 Execute teste isolado: `mvn test -Dtest=TestClass`
3. 🐛 Use modo debug: `LOGGING_LEVEL_ROOT=DEBUG`
4. 📋 Consulte a documentação específica de cada componente

---

**💡 Dica**: Execute sempre `./scripts/test-basic.sh` antes de fazer commits para garantir que tudo está funcionando!
