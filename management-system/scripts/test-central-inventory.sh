# Health check script for central-inventory-service
#!/bin/bash

# Set color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "Central Inventory Service Health Check"
echo "========================================"

# Service base URL
BASE_URL="http://localhost:8082/api/central-inventory"

# Function to check HTTP status
check_endpoint() {
    local endpoint=$1
    local description=$2
    
    echo -n "Checking $description... "
    
    response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$endpoint")
    
    if [ "$response" == "200" ]; then
        echo -e "${GREEN}✓ OK (HTTP $response)${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED (HTTP $response)${NC}"
        return 1
    fi
}

# Check basic endpoints
echo -e "\n${YELLOW}=== Basic Endpoints ===${NC}"
check_endpoint "/actuator/health" "Health endpoint"
check_endpoint "/actuator/info" "Info endpoint"

echo -e "\n${YELLOW}=== API Endpoints ===${NC}"
check_endpoint "/global-inventory" "Global inventory list"
check_endpoint "/global-inventory/available" "Available products"

echo -e "\n${YELLOW}=== Admin Endpoints ===${NC}"
check_endpoint "/admin/stats/summary" "Statistics summary"
check_endpoint "/admin/health/system" "System health"

# Check if service is properly connected to dependencies
echo -e "\n${YELLOW}=== Dependency Checks ===${NC}"

# Database connectivity check
echo -n "Checking database connectivity... "
db_check=$(curl -s "$BASE_URL/actuator/health" | grep -o '"db":{"status":"UP"')
if [ -n "$db_check" ]; then
    echo -e "${GREEN}✓ Database UP${NC}"
else
    echo -e "${RED}✗ Database DOWN${NC}"
fi

# Redis connectivity check
echo -n "Checking Redis connectivity... "
redis_check=$(curl -s "$BASE_URL/actuator/health" | grep -o '"redis":{"status":"UP"')
if [ -n "$redis_check" ]; then
    echo -e "${GREEN}✓ Redis UP${NC}"
else
    echo -e "${RED}✗ Redis DOWN${NC}"
fi

# Kafka connectivity check (via application metrics if available)
echo -n "Checking Kafka connectivity... "
kafka_check=$(curl -s "$BASE_URL/actuator/health" | grep -o '"kafka"')
if [ -n "$kafka_check" ]; then
    echo -e "${GREEN}✓ Kafka detected${NC}"
else
    echo -e "${YELLOW}? Kafka status unknown${NC}"
fi

echo -e "\n${YELLOW}=== Sample Data Test ===${NC}"

# Try to create a test inventory entry
echo -n "Testing inventory creation... "
create_response=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Content-Type: application/json" \
    -d '{
        "sku": "TEST-HEALTH-001",
        "productName": "Health Check Test Product",
        "description": "Product created during health check",
        "storeInventories": [
            {
                "storeId": "STORE-HEALTH",
                "storeName": "Health Check Store",
                "quantity": 10,
                "reserved": 2
            }
        ]
    }' \
    "$BASE_URL/global-inventory")

if [ "$create_response" == "201" ] || [ "$create_response" == "200" ]; then
    echo -e "${GREEN}✓ Inventory creation OK${NC}"
    
    # Test reading the created inventory
    echo -n "Testing inventory retrieval... "
    read_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/global-inventory/TEST-HEALTH-001")
    
    if [ "$read_response" == "200" ]; then
        echo -e "${GREEN}✓ Inventory retrieval OK${NC}"
        
        # Clean up test data
        echo -n "Cleaning up test data... "
        delete_response=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/global-inventory/TEST-HEALTH-001")
        if [ "$delete_response" == "200" ] || [ "$delete_response" == "204" ]; then
            echo -e "${GREEN}✓ Cleanup OK${NC}"
        else
            echo -e "${YELLOW}? Cleanup response: HTTP $delete_response${NC}"
        fi
    else
        echo -e "${RED}✗ Inventory retrieval failed (HTTP $read_response)${NC}"
    fi
else
    echo -e "${RED}✗ Inventory creation failed (HTTP $create_response)${NC}"
fi

echo -e "\n========================================"
echo -e "${GREEN}Central Inventory Service Health Check Complete${NC}"
echo "========================================"
