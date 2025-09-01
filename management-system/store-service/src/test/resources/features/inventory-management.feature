# language: pt
@inventory
Funcionalidade: Gerenciamento de Inventário
  Como um sistema de loja
  Eu quero gerenciar o inventário de produtos
  Para controlar estoque e reservas adequadamente

  Cenário de Fundo:
    Dado que existe uma loja com ID "STORE-001"
    E existe um produto com SKU "PROD-001" e nome "Produto Teste" na loja
    E o produto tem 100 unidades em estoque
    E o produto está ativo

  @busca-produtos
  Cenário: Buscar produtos disponíveis na loja
    Quando eu busco produtos disponíveis na loja "STORE-001"
    Então eu devo receber uma lista de produtos
    E a lista deve conter o produto "PROD-001"
    E o produto deve ter 100 unidades disponíveis

  @busca-por-nome
  Cenário: Buscar produtos por nome
    Quando eu busco produtos com nome "Produto Teste" na loja "STORE-001"
    Então eu devo receber uma lista de produtos
    E a lista deve conter o produto "PROD-001"
    E o produto deve ter o nome "Produto Teste"

  @busca-produto-especifico
  Cenário: Buscar produto específico por SKU
    Quando eu busco o produto com SKU "PROD-001" na loja "STORE-001"
    Então eu devo receber os detalhes do produto
    E o produto deve ter SKU "PROD-001"
    E o produto deve ter nome "Produto Teste"
    E o produto deve ter 100 unidades em estoque

  @produto-nao-encontrado
  Cenário: Buscar produto que não existe
    Quando eu busco o produto com SKU "PROD-999" na loja "STORE-001"
    Então eu devo receber um erro
    E a mensagem deve ser "Produto não encontrado"
