package com.inventory.management.store.infrastructure.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do Apache Kafka para o Store Service.
 * Define os produtores Kafka para publicação de eventos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.producer.client-id:store-service-producer}")
    private String clientId;
    
    @Value("${spring.kafka.producer.acks:all}")
    private String acks;
    
    @Value("${spring.kafka.producer.retries:3}")
    private Integer retries;
    
    @Value("${spring.kafka.producer.batch-size:16384}")
    private Integer batchSize;
    
    @Value("${spring.kafka.producer.linger-ms:1}")
    private Integer lingerMs;
    
    @Value("${spring.kafka.producer.buffer-memory:33554432}")
    private Long bufferMemory;
    
    /**
     * Configura as propriedades do produtor Kafka.
     * 
     * @return mapa com configurações do produtor
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Configurações para garantir idempotência e ordem
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        
        // Configurações de compressão
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return props;
    }
    
    /**
     * Cria a fábrica de produtores Kafka.
     * 
     * @return fábrica de produtores configurada
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }
    
    /**
     * Cria o template Kafka para envio de mensagens.
     * 
     * @return template Kafka configurado
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
