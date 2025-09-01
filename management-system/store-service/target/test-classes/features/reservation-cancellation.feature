# language: pt
@cancelamento-reservas
Funcionalidade: Cancelamento de Reservas
  Como um sistema de checkout
  Eu quero cancelar reservas de produtos
  Para liberar estoque quando necessário

  Cenário de Fundo:
    Dado que existe uma loja com ID "STORE-001"
    E existe um produto com SKU "PROD-001" e nome "Produto Teste" na loja
    E o produto tem 80 unidades em estoque
    E o produto tem 20 unidades reservadas
    E o produto está ativo

  @cancelamento-sucesso
  Cenário: Cancelar reserva com sucesso
    Quando eu cancelo 10 unidades da reserva do produto "PROD-001" na loja "STORE-001"
    Então o cancelamento deve ser bem-sucedido
    E o produto deve ter 10 unidades reservadas
    E o produto deve ter 90 unidades disponíveis
    E a mensagem deve ser "Reserva cancelada com sucesso"

  @cancelamento-reserva-insuficiente
  Cenário: Tentar cancelar mais do que está reservado
    Quando eu cancelo 30 unidades da reserva do produto "PROD-001" na loja "STORE-001"
    Então o cancelamento deve falhar
    E a mensagem deve conter "Quantidade reservada insuficiente"

  @cancelamento-produto-inexistente
  Cenário: Tentar cancelar reserva de produto que não existe
    Quando eu cancelo 5 unidades da reserva do produto "PROD-999" na loja "STORE-001"
    Então o cancelamento deve falhar
    E a mensagem deve conter "Produto não encontrado"

  @cancelamento-quantidade-invalida
  Esquema do Cenário: Tentar cancelar reserva com quantidade inválida
    Quando eu cancelo <quantidade> unidades da reserva do produto "PROD-001" na loja "STORE-001"
    Então o cancelamento deve falhar
    E a mensagem deve conter "Quantidade deve ser maior que zero"

    Exemplos:
      | quantidade |
      | 0          |
      | -5         |

  @cancelamento-loja-inexistente
  Cenário: Tentar cancelar reserva em loja que não existe
    Quando eu cancelo 5 unidades da reserva do produto "PROD-001" na loja "STORE-999"
    Então o cancelamento deve falhar
    E a mensagem deve conter "Produto não encontrado"

  @cancelamento-reserva-total
  Cenário: Cancelar toda a reserva do produto
    Quando eu cancelo 20 unidades da reserva do produto "PROD-001" na loja "STORE-001"
    Então o cancelamento deve ser bem-sucedido
    E o produto deve ter 0 unidades reservadas
    E o produto deve ter 100 unidades disponíveis
