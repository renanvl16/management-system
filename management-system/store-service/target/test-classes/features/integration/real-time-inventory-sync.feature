# language: pt
@integration
Funcionalidade: Sincronização de Estoque em Tempo Real - Testes Integrados
  Como um sistema de gestão de inventário
  Eu quero garantir que a sincronização de estoque funcione corretamente
  Para manter a consistência de dados em tempo real entre todos os componentes

  Cenário de Fundo:
    Dado que o sistema está rodando com infraestrutura completa
    E o banco de dados PostgreSQL está disponível
    E o cache Redis está disponível  
    E o Kafka está disponível para mensageria
    E existem produtos no estoque central

  @integration @real-time-sync
  Cenário: Sincronização de reserva de produto com sucesso
    Dado que existe um produto "iPhone 15" com 10 unidades no estoque central
    E o produto está disponível no cache Redis
    Quando eu reservo 3 unidades do produto "iPhone 15"
    Então a reserva deve ser processada com sucesso
    E o estoque deve ser atualizado para 7 unidades no banco PostgreSQL
    E o cache Redis deve refletir o novo estoque de 7 unidades
    E uma mensagem de atualização deve ser enviada via Kafka
    E a mensagem deve ser consumida pelo sistema central

  @integration @real-time-sync
  Cenário: Sincronização de cancelamento de reserva
    Dado que existe um produto "Samsung Galaxy S24" com 5 unidades no estoque
    E existe uma reserva ativa de 2 unidades para este produto
    E o estoque disponível é de 3 unidades
    Quando eu cancelo a reserva de 2 unidades
    Então a reserva deve ser cancelada com sucesso
    E o estoque deve voltar para 5 unidades no banco PostgreSQL
    E o cache Redis deve ser atualizado para 5 unidades disponíveis
    E uma mensagem de cancelamento deve ser enviada via Kafka

  @integration @real-time-sync @error-handling
  Cenário: Recuperação automática após falha temporária do Redis
    Dado que existe um produto "MacBook Pro" com 8 unidades no estoque
    E o cache Redis está temporariamente indisponível
    Quando eu tento reservar 2 unidades do produto "MacBook Pro"
    Então o sistema deve consultar diretamente o banco PostgreSQL
    E a reserva deve ser processada mesmo sem cache
    E o estoque deve ser atualizado no banco para 6 unidades
    E quando o Redis voltar a estar disponível
    Então o cache deve ser sincronizado automaticamente

  @integration @real-time-sync @concurrency
  Cenário: Gerenciamento de concorrência em reservas simultâneas
    Dado que o sistema está rodando com infraestrutura completa
    E o banco de dados PostgreSQL está disponível
    E o cache Redis está disponível
    E o Kafka está disponível para mensageria
    E existem produtos no estoque central
    E que existe um produto "Nintendo Switch" com 3 unidades no estoque central
    Quando 5 usuários tentam reservar 1 unidade simultaneamente
    Então apenas 3 reservas devem ser aprovadas
    E 2 reservas devem ser rejeitadas por falta de estoque
    E o estoque final deve ser 0 unidades no banco PostgreSQL
    E o cache Redis deve refletir 0 unidades disponíveis
    E as mensagens Kafka devem registrar todas as transações

  @integration @real-time-sync @batch-processing
  Cenário: Processamento em lote de atualizações de estoque
    Dado que existem múltiplos produtos no estoque:
      | produto      | quantidade |
      | iPad Air     | 15         |
      | AirPods Pro  | 25         |
      | Apple Watch  | 20         |
    Quando eu processo um lote de reservas:
      | produto      | quantidade_reservada |
      | iPad Air     | 5                    |
      | AirPods Pro  | 10                   |
      | Apple Watch  | 8                    |
    Então todas as reservas devem ser processadas em lote
    E os estoques devem ser atualizados atomicamente no PostgreSQL:
      | produto      | quantidade_final |
      | iPad Air     | 10               |
      | AirPods Pro  | 15               |
      | Apple Watch  | 12               |
    E o cache Redis deve refletir todas as alterações
    E uma mensagem Kafka deve ser enviada para cada atualização

  @integration @real-time-sync @monitoring
  Cenário: Monitoramento de métricas em tempo real
    Dado que o sistema está coletando métricas via Prometheus
    Quando eu realizo várias operações de reserva e cancelamento
    Então as métricas devem ser atualizadas em tempo real
    E os endpoints de health check devem reportar status OK
    E as métricas de latência do Kafka devem estar dentro dos limites
