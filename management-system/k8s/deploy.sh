#!/bin/bash

# Kubernetes Deployment Script for Inventory Management System
set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%H:%M:%S')] ERROR: $1${NC}"
}

warning() {
    echo -e "${YELLOW}[$(date +'%H:%M:%S')] WARNING: $1${NC}"
}

info() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] INFO: $1${NC}"
}

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    error "kubectl not found. Please install kubectl first."
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

log "🚀 Starting Kubernetes deployment for Inventory Management System..."

# Create namespace
log "📦 Creating namespace..."
kubectl apply -f k8s/namespace.yaml

# Apply infrastructure components
log "🗄️ Deploying PostgreSQL..."
kubectl apply -f k8s/postgres.yaml

log "📋 Deploying Redis..."
kubectl apply -f k8s/redis.yaml

# Wait for infrastructure to be ready
log "⏳ Waiting for infrastructure to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n inventory-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n inventory-system --timeout=300s

# Deploy application services
log "🏪 Deploying Store Service..."
kubectl apply -f k8s/store-service.yaml

log "🏢 Deploying Central Inventory Service..."
kubectl apply -f k8s/central-inventory-service.yaml

# Wait for applications to be ready
log "⏳ Waiting for applications to be ready..."
kubectl wait --for=condition=ready pod -l app=store-service -n inventory-system --timeout=300s
kubectl wait --for=condition=ready pod -l app=central-inventory-service -n inventory-system --timeout=300s

log "✅ Deployment completed successfully!"

# Show deployment status
log "📊 Deployment status:"
kubectl get pods -n inventory-system
kubectl get services -n inventory-system
kubectl get ingress -n inventory-system

info "🌐 Access URLs (after setting up port-forwarding or ingress):"
info "Store Service: http://inventory.local/store-service/swagger-ui.html"
info "Central Service: http://inventory.local/central-inventory-service/swagger-ui.html"

info "🔧 Port-forward commands for local access:"
info "Store Service: kubectl port-forward -n inventory-system svc/store-service 8081:8081"
info "Central Service: kubectl port-forward -n inventory-system svc/central-inventory-service 8082:8082"

log "🎉 Inventory Management System is now running on Kubernetes!"
