package com.inventory.management.store.infrastructure.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para KafkaConfig.
 * Valida todas as configurações do Apache Kafka.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConfig Tests")
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.producer.client-id=store-service-producer",
    "spring.kafka.producer.acks=all",
    "spring.kafka.producer.retries=3",
    "spring.kafka.producer.batch-size=16384",
    "spring.kafka.producer.linger-ms=1",
    "spring.kafka.producer.buffer-memory=33554432"
})
class KafkaConfigTest {

    // Field name constants
    private static final String BOOTSTRAP_SERVERS_FIELD = "bootstrapServers";
    private static final String CLIENT_ID_FIELD = "clientId";
    private static final String ACKS_FIELD = "acks";
    private static final String RETRIES_FIELD = "retries";
    private static final String BATCH_SIZE_FIELD = "batchSize";
    private static final String LINGER_MS_FIELD = "lingerMs";
    private static final String BUFFER_MEMORY_FIELD = "bufferMemory";

    // Default value constants
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String DEFAULT_CLIENT_ID = "store-service-producer";
    private static final String DEFAULT_ACKS = "all";
    private static final Integer DEFAULT_RETRIES = 3;
    private static final Integer DEFAULT_BATCH_SIZE = 16384;
    private static final Integer DEFAULT_LINGER_MS = 1;
    private static final Long DEFAULT_BUFFER_MEMORY = 33554432L;

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        
        // Set default values using reflection
        ReflectionTestUtils.setField(kafkaConfig, BOOTSTRAP_SERVERS_FIELD, DEFAULT_BOOTSTRAP_SERVERS);
        ReflectionTestUtils.setField(kafkaConfig, CLIENT_ID_FIELD, DEFAULT_CLIENT_ID);
        ReflectionTestUtils.setField(kafkaConfig, ACKS_FIELD, DEFAULT_ACKS);
        ReflectionTestUtils.setField(kafkaConfig, RETRIES_FIELD, DEFAULT_RETRIES);
        ReflectionTestUtils.setField(kafkaConfig, BATCH_SIZE_FIELD, DEFAULT_BATCH_SIZE);
        ReflectionTestUtils.setField(kafkaConfig, LINGER_MS_FIELD, DEFAULT_LINGER_MS);
        ReflectionTestUtils.setField(kafkaConfig, BUFFER_MEMORY_FIELD, DEFAULT_BUFFER_MEMORY);
    }

    @Nested
    @DisplayName("Producer Configuration Tests")
    class ProducerConfigurationTests {

        @Test
        @DisplayName("Should create producer configs with correct properties")
        void shouldCreateProducerConfigsWithCorrectProperties() {
            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertNotNull(configs);
            assertEquals(DEFAULT_BOOTSTRAP_SERVERS, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertEquals(DEFAULT_CLIENT_ID, configs.get(ProducerConfig.CLIENT_ID_CONFIG));
            assertEquals(StringSerializer.class, configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
            assertEquals(StringSerializer.class, configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
            assertEquals("all", configs.get(ProducerConfig.ACKS_CONFIG));
            assertEquals(3, configs.get(ProducerConfig.RETRIES_CONFIG));
            assertEquals(16384, configs.get(ProducerConfig.BATCH_SIZE_CONFIG));
            assertEquals(1, configs.get(ProducerConfig.LINGER_MS_CONFIG));
            assertEquals(33554432L, configs.get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        }

        @Test
        @DisplayName("Should configure idempotence settings")
        void shouldConfigureIdempotenceSettings() {
            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(true, configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
            assertEquals(5, configs.get(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION));
        }

        @Test
        @DisplayName("Should configure compression settings")
        void shouldConfigureCompressionSettings() {
            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals("snappy", configs.get(ProducerConfig.COMPRESSION_TYPE_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom bootstrap servers")
        void shouldHandleCustomBootstrapServers() {
            // Given
            String customBootstrapServers = "kafka1:9092,kafka2:9092";
            ReflectionTestUtils.setField(kafkaConfig, BOOTSTRAP_SERVERS_FIELD, customBootstrapServers);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customBootstrapServers, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom client ID")
        void shouldHandleCustomClientId() {
            // Given
            String customClientId = "custom-producer-id";
            ReflectionTestUtils.setField(kafkaConfig, CLIENT_ID_FIELD, customClientId);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customClientId, configs.get(ProducerConfig.CLIENT_ID_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom acks configuration")
        void shouldHandleCustomAcksConfiguration() {
            // Given
            String customAcks = "1";
            ReflectionTestUtils.setField(kafkaConfig, ACKS_FIELD, customAcks);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customAcks, configs.get(ProducerConfig.ACKS_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom retry configuration")
        void shouldHandleCustomRetryConfiguration() {
            // Given
            Integer customRetries = 5;
            ReflectionTestUtils.setField(kafkaConfig, RETRIES_FIELD, customRetries);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customRetries, configs.get(ProducerConfig.RETRIES_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom batch size")
        void shouldHandleCustomBatchSize() {
            // Given
            Integer customBatchSize = 32768;
            ReflectionTestUtils.setField(kafkaConfig, BATCH_SIZE_FIELD, customBatchSize);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customBatchSize, configs.get(ProducerConfig.BATCH_SIZE_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom linger ms")
        void shouldHandleCustomLingerMs() {
            // Given
            Integer customLingerMs = 5;
            ReflectionTestUtils.setField(kafkaConfig, LINGER_MS_FIELD, customLingerMs);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customLingerMs, configs.get(ProducerConfig.LINGER_MS_CONFIG));
        }

        @Test
        @DisplayName("Should handle custom buffer memory")
        void shouldHandleCustomBufferMemory() {
            // Given
            Long customBufferMemory = 67108864L;
            ReflectionTestUtils.setField(kafkaConfig, BUFFER_MEMORY_FIELD, customBufferMemory);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(customBufferMemory, configs.get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        }
    }

    @Nested
    @DisplayName("Producer Factory Tests")
    class ProducerFactoryTests {

        @Test
        @DisplayName("Should create producer factory")
        void shouldCreateProducerFactory() {
            // When
            ProducerFactory<String, String> producerFactory = kafkaConfig.producerFactory();

            // Then
            assertNotNull(producerFactory);
            assertInstanceOf(DefaultKafkaProducerFactory.class, producerFactory);
        }

        @Test
        @DisplayName("Should create producer factory with correct configurations")
        void shouldCreateProducerFactoryWithCorrectConfigurations() {
            // When
            ProducerFactory<String, String> producerFactory = kafkaConfig.producerFactory();

            // Then
            assertNotNull(producerFactory);
            
            // Verify that the producer factory uses the correct configuration
            Map<String, Object> configs = producerFactory.getConfigurationProperties();
            assertEquals(DEFAULT_BOOTSTRAP_SERVERS, configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
            assertEquals(DEFAULT_CLIENT_ID, configs.get(ProducerConfig.CLIENT_ID_CONFIG));
        }
    }

    @Nested
    @DisplayName("Kafka Template Tests")
    class KafkaTemplateTests {

        @Test
        @DisplayName("Should create kafka template")
        void shouldCreateKafkaTemplate() {
            // When
            KafkaTemplate<String, String> kafkaTemplate = kafkaConfig.kafkaTemplate();

            // Then
            assertNotNull(kafkaTemplate);
            assertInstanceOf(KafkaTemplate.class, kafkaTemplate);
        }

        @Test
        @DisplayName("Should create kafka template with correct producer factory")
        void shouldCreateKafkaTemplateWithCorrectProducerFactory() {
            // When
            KafkaTemplate<String, String> kafkaTemplate = kafkaConfig.kafkaTemplate();
            ProducerFactory<String, String> producerFactory = kafkaTemplate.getProducerFactory();

            // Then
            assertNotNull(producerFactory);
            assertInstanceOf(DefaultKafkaProducerFactory.class, producerFactory);
        }
    }

    @Nested
    @DisplayName("Configuration Annotations Tests")
    class ConfigurationAnnotationsTests {

        @Test
        @DisplayName("Should have @Configuration annotation")
        void shouldHaveConfigurationAnnotation() {
            // When
            boolean hasConfigurationAnnotation = KafkaConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class);

            // Then
            assertTrue(hasConfigurationAnnotation);
        }
    }

    @Nested
    @DisplayName("Bean Definition Tests")
    class BeanDefinitionTests {

        @Test
        @DisplayName("Should have producerConfigs bean method")
        void shouldHaveProducerConfigsBeanMethod() throws NoSuchMethodException {
            // When
            var method = KafkaConfig.class.getDeclaredMethod("producerConfigs");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        }

        @Test
        @DisplayName("Should have producerFactory bean method")
        void shouldHaveProducerFactoryBeanMethod() throws NoSuchMethodException {
            // When
            var method = KafkaConfig.class.getDeclaredMethod("producerFactory");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        }

        @Test
        @DisplayName("Should have kafkaTemplate bean method")
        void shouldHaveKafkaTemplateBeanMethod() throws NoSuchMethodException {
            // When
            var method = KafkaConfig.class.getDeclaredMethod("kafkaTemplate");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            ReflectionTestUtils.setField(kafkaConfig, BOOTSTRAP_SERVERS_FIELD, null);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertNotNull(configs);
            assertNull(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        }

        @Test
        @DisplayName("Should handle zero retries")
        void shouldHandleZeroRetries() {
            // Given
            Integer zeroRetries = 0;
            ReflectionTestUtils.setField(kafkaConfig, RETRIES_FIELD, zeroRetries);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(zeroRetries, configs.get(ProducerConfig.RETRIES_CONFIG));
        }

        @Test
        @DisplayName("Should handle minimum buffer memory")
        void shouldHandleMinimumBufferMemory() {
            // Given
            Long minimumBufferMemory = 1024L;
            ReflectionTestUtils.setField(kafkaConfig, BUFFER_MEMORY_FIELD, minimumBufferMemory);

            // When
            Map<String, Object> configs = kafkaConfig.producerConfigs();

            // Then
            assertEquals(minimumBufferMemory, configs.get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        }
    }
}
