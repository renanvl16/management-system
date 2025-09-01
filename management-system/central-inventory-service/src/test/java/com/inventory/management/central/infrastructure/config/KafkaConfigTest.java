package com.inventory.management.central.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConfig - Testes Unitários")
class KafkaConfigTest {

    @InjectMocks
    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        // Configurar valores via reflection para simular @Value
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "central-inventory-group");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(kafkaConfig, "enableAutoCommit", false);
        ReflectionTestUtils.setField(kafkaConfig, "sessionTimeoutMs", 30000);
        ReflectionTestUtils.setField(kafkaConfig, "heartbeatIntervalMs", 3000);
        ReflectionTestUtils.setField(kafkaConfig, "maxPollRecords", 10);
        ReflectionTestUtils.setField(kafkaConfig, "maxPollIntervalMs", 300000);
    }

    @Test
    @DisplayName("Deve criar consumer factory com configurações corretas")
    void shouldCreateConsumerFactoryWithCorrectConfigurations() {
        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Then
        assertThat(consumerFactory).isNotNull();

        // Verificar configurações específicas
        var configProps = consumerFactory.getConfigurationProperties();

        assertThat(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(configProps.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("central-inventory-group");
        assertThat(configProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
        assertThat(configProps.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
        assertThat(configProps.get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)).isEqualTo(30000);
        assertThat(configProps.get(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG)).isEqualTo(3000);
        assertThat(configProps.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG)).isEqualTo(10);
        assertThat(configProps.get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG)).isEqualTo(300000);
    }

    @Test
    @DisplayName("Deve configurar deserializers corretos")
    void shouldConfigureCorrectDeserializers() {
        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Then
        var configProps = consumerFactory.getConfigurationProperties();

        assertThat(configProps.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(StringDeserializer.class);
        assertThat(configProps.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(StringDeserializer.class);
    }

    @Test
    @DisplayName("Deve configurar parâmetros de fetch")
    void shouldConfigureFetchParameters() {
        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Then
        var configProps = consumerFactory.getConfigurationProperties();

        assertThat(configProps.get(ConsumerConfig.FETCH_MIN_BYTES_CONFIG)).isEqualTo(1);
        assertThat(configProps.get(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG)).isEqualTo(5000);
    }

    @Test
    @DisplayName("Deve criar kafka listener container factory")
    void shouldCreateKafkaListenerContainerFactory() {
        // When
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        // Then
        assertThat(factory).isNotNull();
        assertThat(factory.getConsumerFactory()).isNotNull();
    }

    @Test
    @DisplayName("Deve funcionar com diferentes valores de configuração")
    void shouldWorkWithDifferentConfigurationValues() {
        // Given - configurar valores diferentes
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "kafka1:9092,kafka2:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");
        ReflectionTestUtils.setField(kafkaConfig, "autoOffsetReset", "latest");
        ReflectionTestUtils.setField(kafkaConfig, "enableAutoCommit", true);
        ReflectionTestUtils.setField(kafkaConfig, "maxPollRecords", 50);

        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Then
        var configProps = consumerFactory.getConfigurationProperties();

        assertThat(configProps.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG))
                .isEqualTo("kafka1:9092,kafka2:9092");
        assertThat(configProps.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("test-group");
        assertThat(configProps.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("latest");
        assertThat(configProps.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(true);
        assertThat(configProps.get(ConsumerConfig.MAX_POLL_RECORDS_CONFIG)).isEqualTo(50);
    }

    @Test
    @DisplayName("Deve manter configurações padrão quando não especificado")
    void shouldMaintainDefaultConfigurationsWhenNotSpecified() {
        // Given - usar valores padrão para alguns campos
        ReflectionTestUtils.setField(kafkaConfig, "sessionTimeoutMs", 45000);
        ReflectionTestUtils.setField(kafkaConfig, "heartbeatIntervalMs", 5000);

        // When
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        // Then
        var configProps = consumerFactory.getConfigurationProperties();

        assertThat(configProps.get(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG)).isEqualTo(45000);
        assertThat(configProps.get(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG)).isEqualTo(5000);
    }
}
