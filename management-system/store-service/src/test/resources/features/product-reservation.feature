# language: pt
@reservas
Funcionalidade: Reserva de Produtos
  Como um sistema de checkout
  Eu quero reservar produtos no inventário
  Para garantir disponibilidade durante o processo de compra

  Cenário de Fundo:
    Dado que existe uma loja com ID "STORE-001"
    E existe um produto com SKU "PROD-001" e nome "Produto Teste" na loja
    E o produto tem 50 unidades em estoque
    E o produto está ativo

  @reserva-sucesso
  Cenário: Reservar produto com sucesso
    Quando eu reservo 10 unidades do produto "PROD-001" na loja "STORE-001"
    Então a reserva deve ser bem-sucedida
    E o produto deve ter 10 unidades reservadas
    E o produto deve ter 40 unidades disponíveis
    E a mensagem deve ser "Produto reservado com sucesso"

  @reserva-quantidade-insuficiente
  Cenário: Tentar reservar mais produtos do que disponível
  Quando eu reservo 60 unidades do produto "PROD-001" na loja "STORE-001"
  Então a reserva deve falhar
  E a mensagem deve conter "Estoque insuficiente"

@reservas @reserva-produto-inexistente
  Cenário: Tentar reservar produto que não existe
    Quando eu reservo 5 unidades do produto "PROD-999" na loja "STORE-001"
    Então a reserva deve falhar
    E a mensagem deve conter "Produto não encontrado"

  @reserva-quantidade-invalida
  Esquema do Cenário: Tentar reservar com quantidade inválida
    Quando eu reservo <quantidade> unidades do produto "PROD-001" na loja "STORE-001"
    Então a reserva deve falhar
    E a mensagem deve conter "Quantidade deve ser maior que zero"

    Exemplos:
      | quantidade |
      | 0          |
      | -1         |
      | -10        |

  @reserva-produto-inativo
  Cenário: Tentar reservar produto inativo
    Dado que o produto "PROD-001" está inativo
    Quando eu reservo 5 unidades do produto "PROD-001" na loja "STORE-001"
    Então a reserva deve falhar
    E a mensagem deve conter "Produto não está ativo"
