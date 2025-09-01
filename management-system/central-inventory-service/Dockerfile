# Imagem de produção direta (JAR pré-compilado)
FROM nexus.elocloud.ninja/eclipse-temurin:21-jre-alpine

# Metadados da imagem
LABEL maintainer="Sistema de Gerenciamento de Inventário"
LABEL description="Central Inventory Service - Serviço Centralizado de Inventário"
LABEL version="1.0.0"

# Instalar curl para health check
RUN apk add --no-cache curl bash && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Definir diretório de trabalho
WORKDIR /app

# Criar diretórios necessários
RUN mkdir -p logs && chown -R appuser:appgroup /app

# Copiar JAR pré-compilado
COPY target/central-inventory-service-*.jar app.jar

# Dar propriedade dos arquivos ao usuário não-root
RUN chown appuser:appgroup app.jar

# Mudar para usuário não-root
USER appuser

# Configurar variáveis de ambiente para JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Dfile.encoding=UTF-8 \
               -Duser.timezone=America/Sao_Paulo"

# Expor porta da aplicação
EXPOSE 8082

# Health check - removido para ser controlado pelo docker-compose

# Comando para iniciar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
