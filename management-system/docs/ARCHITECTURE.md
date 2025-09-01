# Arquitetura do Sistema - Sistema de Gerenciamento de Inventário

## 🏗️ Visão Geral da Arquitetura

Este documento detalha a arquitetura completa do sistema de gerenciamento de inventário distribuído, incluindo padrões de resiliência e controle de concorrência.

## 📊 Diagrama de Arquitetura com Sistema de Resiliência

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Store App     │    │   Admin Portal  │    │  External APIs  │
│   (Frontend)    │    │   (Dashboard)   │    │   (Partners)    │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                     API Gateway / Load Balancer                │
└─────────┬─────────────────────┬─────────────────────┬───────────┘
          ▼                     ▼                     ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  Store Service  │   │Central Inventory│   │  Other Services │
│    (Port 8081)  │◄──┤   Service       │   │   (Future)      │
│ ┌─────────────┐ │   │   (Port 8082)   │   │                 │
│ │🛡️ DLQ System │ │   │                 │   │                 │
│ │Failed Events │ │   │                 │   │                 │
│ │Auto Retry    │ │   │                 │   │                 │
│ └─────────────┘ │   │                 │   │                 │
└─────────┬───────┘   └─────────┬───────┘   └─────────────────┘
          │                     │
          ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    🔄 Event Bus (Kafka)                        │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │inventory-update │ │inventory-reserve│ │inventory-commit │   │
│  │     Topic       │ │     Topic       │ │     Topic       │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
│                                                                 │
│  🚫 Offline Mode: Events → DLQ → Local Storage → Auto Retry    │
└─────────────────────────────────────────────────────────────────┘
          │                     │                    │
          ▼                     ▼                    ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│   PostgreSQL    │   │      Redis      │   │   Prometheus    │
│  (Port 5432)    │   │   (Port 6379)   │   │   (Port 9090)   │
│                 │   │                 │   │                 │
│ ┌─────────────┐ │   │ ┌─────────────┐ │   │ ┌─────────────┐ │
│ │failed_events│ │   │ │  Cache +    │ │   │ │ DLQ Metrics │ │
│ │   Table     │ │   │ │ Session     │ │   │ │ & Alerts    │ │
│ │(DLQ Storage)│ │   │ │  Storage    │ │   │ │             │ │
│ └─────────────┘ │   │ └─────────────┘ │   │ └─────────────┘ │
└─────────────────┘   └─────────────────┘   └─────────┬───────┘
                                                      ▼
                                            ┌─────────────────┐
                                            │     Grafana     │
                                            │   (Port 3000)   │
                                            │ ┌─────────────┐ │
                                            │ │DLQ Dashboard│ │
                                            │ │Resilience   │ │
                                            │ │  Metrics    │ │
                                            │ └─────────────┘ │
                                            └─────────────────┘
```

## ⚙️ Controle de Concorrência Distribuída

### Problema de Concorrência Resolvido
O sistema trata cenários onde **múltiplas lojas** (store-service-1, store-service-2, store-service-3...) atualizam simultaneamente:
- 🏪 **Base local** de cada loja (inventário intra-loja)  
- 🌍 **Base central** (inventário global inter-lojas)

### Solução Implementada

#### Store Service - Concorrência INTRA-LOJA
```java
@Entity
public class ProductEntity {
    @Version
    @Column(name = "version", nullable = false)
    private Long version; // Optimistic Locking
}

@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2.0))
public Product updateProductQuantity(String sku, Integer newQuantity) {
    // Retry automático em caso de conflito
}
```

#### Central Inventory Service - Concorrência INTER-LOJAS
```java
@Entity
@Table(name = "global_inventory")
public class GlobalInventoryEntity {
    @Version
    private Long version; // Controle de versão global
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "store_quantities", columnDefinition = "jsonb")
    private String storeQuantitiesJson; // Dados de todas as lojas
}
```

### Estrutura JSONB para Múltiplas Lojas
```json
{
  "store-001": {
    "quantity": 15,
    "reservedQuantity": 2, 
    "timestamp": "2025-08-30T09:00:00"
  },
  "store-002": {
    "quantity": 10,
    "reservedQuantity": 1,
    "timestamp": "2025-08-30T09:15:00"
  },
  "store-003": {
    "quantity": 8,
    "reservedQuantity": 0,
    "timestamp": "2025-08-30T09:30:00"
  }
}
```

## 🛡️ Sistema de Resiliência e Dead Letter Queue (DLQ)

### Problema Resolvido
O sistema garante que **nenhum evento seja perdido** mesmo quando o Kafka está indisponível, implementando um padrão de Dead Letter Queue robusto com retry automático.

### Arquitetura de Resiliência
```
┌─────────────────────────────────────────────────────────────────┐
│                    🎯 Business Operation                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   Reserve   │  │   Commit    │  │   Update    │             │
│  │   Product   │  │   Sale      │  │  Quantity   │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
└─────────────┬───────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   🔄 Event Publishing Flow                      │
│                                                                 │
│  1️⃣ Try Kafka Direct                                           │
│      ┌─────────────┐    ✅ Success     ┌─────────────┐          │
│      │   Kafka     │ ◄────────────────│ Event       │          │
│      │   Topic     │                  │ Publisher   │          │
│      └─────────────┘                  └─────────────┘          │
│                                               │                 │
│  2️⃣ On Failure → DLQ                        ❌ Kafka Offline   │
│      ┌─────────────┐    🛡️ Fallback    ┌─────────────┐          │
│      │ PostgreSQL  │ ◄────────────────│ DLQ         │          │
│      │failed_events│                  │ Handler     │          │
│      └─────────────┘                  └─────────────┘          │
└─────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   🤖 Auto Recovery System                       │
│                                                                 │
│  ⏰ Every 5 minutes:                                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Load      │───▶│   Retry     │───▶│   Mark      │         │
│  │  Pending    │    │   Failed    │    │ Succeeded   │         │
│  │  Events     │    │   Events    │    │ or Failed   │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│                                                                 │
│  📊 Exponential Backoff: 1s → 2s → 4s → 8s → 16s             │
│  🔄 Max Retries: 10 attempts                                  │
│  🧹 Auto Cleanup: Remove old succeeded events                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🏪 Arquitetura dos Microserviços

### Store Service (Loja Local)
- **🎯 Propósito**: Operações de inventário local com baixa latência
- **📍 Localização**: Executado em cada loja ou próximo ao PDV
- **⚡ Responsabilidades**:
  - Consulta de produtos disponíveis
  - Reserva de itens para carrinho
  - Confirmação/cancelamento de vendas
  - Publicação de eventos de atualização
  - Cache local para operação offline

### Central Inventory Service (Controle Central)
- **🎯 Propósito**: Visão global e reconciliação de inventário
- **📍 Localização**: Datacenter central ou cloud
- **🔄 Responsabilidades**:
  - Agregação de dados de todas as lojas
  - Consultas para sistemas legados
  - Reconciliação de conflitos
  - Relatórios gerenciais
  - Rebalanceamento entre lojas

## 🔧 Configurações de Resiliência

```yaml
app:
  resilience:
    kafka:
      retry:
        max-attempts: 5              # Tentativas síncronas
        initial-delay: 1000          # Delay inicial: 1s
        max-delay: 30000            # Delay máximo: 30s
        multiplier: 2.0             # Multiplicador exponencial
      dlq:
        enabled: true               # Habilitar DLQ
        max-retries: 10            # Máximo de tentativas no DLQ
        cleanup-after-days: 7      # Limpeza automática após 7 dias
      circuit-breaker:
        enabled: true               # Circuit breaker
        failure-threshold: 5        # Falhas para abrir circuito
        timeout: 60000             # Timeout: 60s
```

## 🛠️ Tecnologias Utilizadas

### Backend Framework
- **Java 21** - LTS com performance melhorada
- **Spring Boot 3.2.0** - Framework principal
- **Spring Data JPA** - Acesso a dados
- **Spring Kafka** - Integração com Apache Kafka
- **Spring Data Redis** - Cache distribuído

### Controle de Concorrência
- **JPA @Version** - Versionamento automático de entidades
- **Spring Retry** - Retry template com backoff exponencial  
- **JSONB (PostgreSQL)** - Armazenamento eficiente de dados por loja
- **Pessimistic Locking** - Locks explícitos para operações críticas
- **Flyway** - Migrações de banco versionadas e seguras

### Infraestrutura
- **Docker** - Containerização de aplicações
- **PostgreSQL 15** - Banco de dados principal
- **Redis 7.2** - Cache e sessões
- **Apache Kafka 7.4** - Message streaming platform
- **Prometheus** - Coleta de métricas
- **Grafana** - Visualização e dashboards

## 📊 Métricas de Concorrência

**Prometheus Metrics:**
```
# Eventos no DLQ por status
dlq_events_total{status="pending|processing|succeeded|failed"}

# Tempo de retry
dlq_retry_duration_seconds

# Taxa de recuperação
dlq_recovery_rate{success="true|false"}

# Circuit breaker state
circuit_breaker_state{name="kafka-publisher",state="open|closed|half_open"}

# Conflitos de versão
optimistic_lock_failures_total{table="global_inventory"}

# Tentativas de retry
retry_attempts_total{operation="updateInventory"}
```

## ✅ Benefícios Implementados

1. **🔄 Zero Data Loss**: Eventos nunca são perdidos
2. **🚀 Auto Recovery**: Sistema se recupera automaticamente
3. **📊 Full Observability**: Métricas e dashboards completos
4. **🎛️ Admin Control**: APIs para gestão manual
5. **⚡ Performance**: Operações locais não são impactadas
6. **🛡️ Resilient Design**: Funciona offline e online
7. **🧹 Self Healing**: Limpeza automática de dados antigos
8. **⚙️ Concurrency Control**: Controle de versão distribuído
9. **🔄 Smart Retry**: Backoff exponencial com jitter
