# Dockerfile para Store Service
FROM nexus.elocloud.ninja/alpine:3.19

# Metadados da imagem
LABEL maintainer="inventory-team@company.com"
LABEL version="1.0.0"
LABEL description="Store Service - Microserviço de inventário local da loja"

# Instalar OpenJDK 21 e outras dependências, criar usuário
RUN apk add --no-cache openjdk21-jre curl && \
    addgroup -g 1001 appuser && \
    adduser -D -s /bin/sh -u 1001 -G appuser appuser

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da aplicação diretamente do target local
COPY target/store-service-*.jar app.jar

# Criar diretório para logs e alterar propriedades dos arquivos
RUN mkdir -p /app/logs && \
    chown -R appuser:appuser /app

# Mudar para usuário não-root
USER appuser

# Configurações de JVM para otimização
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Expor porta da aplicação
EXPOSE 8081

# Health check - removido para ser controlado pelo docker-compose
# HEALTHCHECK removido

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
