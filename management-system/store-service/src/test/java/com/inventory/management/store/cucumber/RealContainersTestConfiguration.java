package com.inventory.management.store.cucumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuração de testes integrados usando TestContainers com as mesmas imagens do docker-compose
 * 
 * Esta configuração substitui SimpleIntegratedTestConfiguration para usar containers reais:
 * - PostgreSQL: postgres:15-alpine
 * - Redis: redis:7.2-alpine  
 * - Kafka: confluentinc/cp-kafka:7.4.0
 *
 * NOTA: A anotação @CucumberContextConfiguration foi removida para evitar conflitos.
 * A configuração principal do Cucumber está em CucumberSpringConfiguration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@ContextConfiguration(initializers = RealContainersTestConfiguration.Initializer.class)
public class RealContainersTestConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RealContainersTestConfiguration.class);
    private static final String CONTAINER_ID_LOG = "    Container ID: {}";
    
    private RealContainersTestConfiguration() {
        // Construtor privado para classe de configuração
    }

    // Criar uma rede para os containers se comunicarem
    static Network network = Network.newNetwork();

    // PostgreSQL Container - usando a mesma imagem do docker-compose com registry
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("nexus.elocloud.ninja/postgres:15-alpine")
                .asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("inventorydb")
            .withUsername("inventory")
            .withPassword("inventory123")
            .withInitScript("init-test-db.sql") // Script de inicialização se necessário
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2));

    // Redis Container - usando a mesma imagem do docker-compose com registry
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
            DockerImageName.parse("nexus.elocloud.ninja/redis:7.2-alpine")
                .asCompatibleSubstituteFor("redis"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "inventorypass123", "--appendonly", "yes")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));

    // Kafka Container - usando a mesma imagem do docker-compose com registry
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("nexus.elocloud.ninja/confluentinc/cp-kafka:7.4.0")
                .asCompatibleSubstituteFor("confluentinc/cp-kafka"))
            .withNetwork(network)
            .withNetworkAliases("kafka")
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_LOG_RETENTION_HOURS", "168")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .waitingFor(Wait.forLogMessage(".*started \\(kafka.server.KafkaServer\\).*", 1));

    /**
     * Inicializador do contexto Spring que configura as propriedades
     * dos containers TestContainers
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            // Aguardar todos os containers iniciarem
            startContainersIfNeeded();

            // Configurar propriedades do Spring com os containers
            TestPropertyValues.of(
                    // PostgreSQL Configuration
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                    "spring.datasource.driver-class-name=org.postgresql.Driver",
                    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.jpa.show-sql=false",
                    
                    // Redis Configuration
                    "spring.data.redis.host=" + redisContainer.getHost(),
                    "spring.data.redis.port=" + redisContainer.getMappedPort(6379),
                    "spring.data.redis.password=inventorypass123",
                    "spring.data.redis.timeout=60000",
                    "spring.cache.type=redis",
                    
                    // Kafka Configuration
                    "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                    "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                    "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
                    "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
                    "spring.kafka.consumer.group-id=inventory-test-group",
                    "spring.kafka.consumer.auto-offset-reset=earliest",
                    "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
                    
                    // Test specific configurations
                    "logging.level.org.testcontainers=INFO",
                    "logging.level.com.github.dockerjava=WARN",
                    "logging.level.org.apache.kafka=WARN",
                    "logging.level.org.springframework.kafka=INFO",
                    
                    // Application specific
                    "app.store-id=STORE_TEST_001",
                    "app.kafka.topics.inventory-updates=test-inventory-updates",
                    "app.kafka.topics.reservation-events=test-reservation-events"
                    
            ).applyTo(configurableApplicationContext.getEnvironment());

            logContainerInformation();
        }

        /**
         * Inicia os containers se ainda não estiverem rodando
         */
        private void startContainersIfNeeded() {
            logger.info("=== INICIANDO CONTAINERS PARA TESTES INTEGRADOS ===");
            
            if (!postgreSQLContainer.isRunning()) {
                logger.info("🐘 Iniciando container PostgreSQL (nexus.elocloud.ninja/postgres:15-alpine)...");
                postgreSQLContainer.start();
                logger.info("✅ PostgreSQL iniciado: {}", postgreSQLContainer.getJdbcUrl());
            }

            if (!redisContainer.isRunning()) {
                logger.info("🔴 Iniciando container Redis (nexus.elocloud.ninja/redis:7.2-alpine)...");
                redisContainer.start();
                logger.info("✅ Redis iniciado: {}:{}", redisContainer.getHost(), redisContainer.getMappedPort(6379));
            }

            if (!kafkaContainer.isRunning()) {
                logger.info("📨 Iniciando container Kafka (nexus.elocloud.ninja/confluentinc/cp-kafka:7.4.0)...");
                kafkaContainer.start();
                logger.info("✅ Kafka iniciado: {}", kafkaContainer.getBootstrapServers());
            }
            
            // Aguardar estabilização
            try {
                Thread.sleep(3000);
                logger.info("⏳ Containers estabilizados após 3 segundos");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrompido durante estabilização", e);
            }
        }

        /**
         * Log das informações dos containers para debug
         */
        private void logContainerInformation() {
            logger.info("=== CONTAINERS CONFIGURADOS PARA TESTES ===");
            logger.info("🐘 PostgreSQL:");
            logger.info("    URL: {}", postgreSQLContainer.getJdbcUrl());
            logger.info("    Usuario: {}", postgreSQLContainer.getUsername());
            logger.info(CONTAINER_ID_LOG, postgreSQLContainer.getContainerId());
            
            logger.info("🔴 Redis:");
            logger.info("    Host: {}", redisContainer.getHost());
            logger.info("    Port: {}", redisContainer.getMappedPort(6379));
            logger.info(CONTAINER_ID_LOG, redisContainer.getContainerId());
            
            logger.info("📨 Kafka:");
            logger.info("    Bootstrap Servers: {}", kafkaContainer.getBootstrapServers());
            logger.info(CONTAINER_ID_LOG, kafkaContainer.getContainerId());
            
            logger.info("🌐 Network: {}", network.getId());
            logger.info("===============================================");
        }
    }
    
    /**
     * Hook para limpeza após todos os testes
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🧹 Executando limpeza dos containers...");
            
            try {
                if (kafkaContainer.isRunning()) {
                    kafkaContainer.stop();
                    logger.info("✅ Kafka container parado");
                }
                
                if (redisContainer.isRunning()) {
                    redisContainer.stop();
                    logger.info("✅ Redis container parado");
                }
                
                if (postgreSQLContainer.isRunning()) {
                    postgreSQLContainer.stop();
                    logger.info("✅ PostgreSQL container parado");
                }
                
                if (network != null) {
                    network.close();
                    logger.info("✅ Network removida");
                }
                
            } catch (Exception e) {
                logger.warn("⚠️ Erro durante limpeza dos containers: {}", e.getMessage());
            }
        }));
    }
}
