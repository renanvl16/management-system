#!/bin/bash
# Diagnóstico rápido dos containers e logs do sistema de inventário

echo "\n==== STATUS DOS CONTAINERS ===="
docker-compose ps

echo "\n==== LOGS DOS SERVIÇOS (últimos 100) ===="
docker-compose logs --tail=100 store-service central-inventory-service postgres redis kafka

echo "\n==== TESTE DE CONECTIVIDADE ===="
docker-compose exec -T postgres pg_isready -U inventory || echo "Postgres indisponível"
docker-compose exec -T redis redis-cli ping || echo "Redis indisponível"

echo "\n==== TESTE DE LATÊNCIA STORE-SERVICE ===="
curl -w "Tempo total: %{time_total}s\n" -s -o /dev/null http://localhost:8081/store-service/actuator/health

echo "\n==== TESTE DE LATÊNCIA CENTRAL-INVENTORY-SERVICE ===="
curl -w "Tempo total: %{time_total}s\n" -s -o /dev/null http://localhost:8082/central-inventory-service/actuator/health

echo "\n==== FIM DO DIAGNÓSTICO ===="
