package com.inventory.management.central.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Kafka para o Central Inventory Service.
 * 
 * Esta configuração define consumidores Kafka para receber eventos
 * de inventário das lojas em tempo real.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;
    
    @Value("${spring.kafka.consumer.enable-auto-commit:false}")
    private Boolean enableAutoCommit;
    
    @Value("${spring.kafka.consumer.session-timeout-ms:30000}")
    private Integer sessionTimeoutMs;
    
    @Value("${spring.kafka.consumer.heartbeat-interval-ms:3000}")
    private Integer heartbeatIntervalMs;
    
    @Value("${spring.kafka.consumer.max-poll-records:10}")
    private Integer maxPollRecords;
    
    @Value("${spring.kafka.consumer.max-poll-interval-ms:300000}")
    private Integer maxPollIntervalMs;
    
    /**
     * Factory para criação de consumidores Kafka.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        log.info("🔧 Configurando Kafka Consumer Factory");
        
        Map<String, Object> configProps = new HashMap<>();
        
        // Configurações básicas
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        
        // Deserializers
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // Configurações de performance e resiliência
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        
        // Configurações de fetch
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 5000);
        
        log.info("✅ Kafka Consumer Factory configurado: bootstrapServers={}, groupId={}", 
                bootstrapServers, groupId);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Factory de container para listeners Kafka.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        log.info("🔧 Configurando Kafka Listener Container Factory");
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
                new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        
        // Configurar acknowledgment manual
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // Configurar concorrência (número de threads consumidoras) - apenas 1 para evitar conflitos
        factory.setConcurrency(1);
        
        // Configurar error handler com retry
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, 3L) // 3 tentativas com 1s de intervalo
        );
        
        factory.setCommonErrorHandler(errorHandler);
        
        log.info("✅ Kafka Listener Container Factory configurado com {} threads", 1);
        
        return factory;
    }
}
