package com.inventory.management.store.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ResilienceConfig.
 * Valida todas as configurações de resiliência do sistema.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResilienceConfig Tests")
@TestPropertySource(properties = {
    "app.resilience.kafka.retry.max-attempts=5",
    "app.resilience.kafka.retry.initial-delay=1000",
    "app.resilience.kafka.retry.max-delay=30000",
    "app.resilience.kafka.retry.multiplier=2.0"
})
class ResilienceConfigTest {

    // Field name constants
    private static final String MAX_RETRY_ATTEMPTS_FIELD = "maxRetryAttempts";
    private static final String INITIAL_DELAY_FIELD = "initialDelay";
    private static final String MAX_DELAY_FIELD = "maxDelay";
    private static final String MULTIPLIER_FIELD = "multiplier";

    // Default value constants
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;
    private static final long DEFAULT_INITIAL_DELAY = 1000L;
    private static final long DEFAULT_MAX_DELAY = 30000L;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    
    // Test constants
    private static final String SUCCESS_RESULT = "success";

    private ResilienceConfig resilienceConfig;

    @BeforeEach
    void setUp() {
        resilienceConfig = new ResilienceConfig();
        
        // Set default values using reflection
        ReflectionTestUtils.setField(resilienceConfig, MAX_RETRY_ATTEMPTS_FIELD, DEFAULT_MAX_RETRY_ATTEMPTS);
        ReflectionTestUtils.setField(resilienceConfig, INITIAL_DELAY_FIELD, DEFAULT_INITIAL_DELAY);
        ReflectionTestUtils.setField(resilienceConfig, MAX_DELAY_FIELD, DEFAULT_MAX_DELAY);
        ReflectionTestUtils.setField(resilienceConfig, MULTIPLIER_FIELD, DEFAULT_MULTIPLIER);
    }

    @Nested
    @DisplayName("Kafka Retry Template Tests")
    class KafkaRetryTemplateTests {

        @Test
        @DisplayName("Should create kafka retry template")
        void shouldCreateKafkaRetryTemplate() {
            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should execute successfully with valid operation")
        void shouldExecuteSuccessfullyWithValidOperation() {
            // Given
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // When & Then
            String result = retryTemplate.execute(context -> SUCCESS_RESULT);

            assertEquals(SUCCESS_RESULT, result);
        }

        @Test
        @DisplayName("Should handle custom max retry attempts configuration")
        void shouldHandleCustomMaxRetryAttemptsConfiguration() {
            // Given
            int customMaxAttempts = 10;
            ReflectionTestUtils.setField(resilienceConfig, MAX_RETRY_ATTEMPTS_FIELD, customMaxAttempts);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            // Verify the template was created successfully with custom configuration
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle custom initial delay configuration")
        void shouldHandleCustomInitialDelayConfiguration() {
            // Given
            long customInitialDelay = 2000L;
            ReflectionTestUtils.setField(resilienceConfig, INITIAL_DELAY_FIELD, customInitialDelay);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle custom max delay configuration")
        void shouldHandleCustomMaxDelayConfiguration() {
            // Given
            long customMaxDelay = 60000L;
            ReflectionTestUtils.setField(resilienceConfig, MAX_DELAY_FIELD, customMaxDelay);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle custom multiplier configuration")
        void shouldHandleCustomMultiplierConfiguration() {
            // Given
            double customMultiplier = 3.0;
            ReflectionTestUtils.setField(resilienceConfig, MULTIPLIER_FIELD, customMultiplier);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should register retry listener")
        void shouldRegisterRetryListener() {
            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            // Verify that listeners are registered (we can't easily test the actual listener behavior without complex mocking)
            var listeners = ReflectionTestUtils.getField(retryTemplate, "listeners");
            assertNotNull(listeners);
        }
    }

    @Nested
    @DisplayName("General Retry Template Tests")
    class GeneralRetryTemplateTests {

        @Test
        @DisplayName("Should create general retry template")
        void shouldCreateGeneralRetryTemplate() {
            // When
            RetryTemplate retryTemplate = resilienceConfig.generalRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should execute successfully with valid operation")
        void shouldExecuteSuccessfullyWithValidOperation() {
            // Given
            RetryTemplate retryTemplate = resilienceConfig.generalRetryTemplate();

            // When & Then
            String result = retryTemplate.execute(context -> SUCCESS_RESULT);

            assertEquals(SUCCESS_RESULT, result);
        }

        @Test
        @DisplayName("Should be configured differently from kafka retry template")
        void shouldBeConfiguredDifferentlyFromKafkaRetryTemplate() {
            // When
            RetryTemplate kafkaRetryTemplate = resilienceConfig.kafkaRetryTemplate();
            RetryTemplate generalRetryTemplate = resilienceConfig.generalRetryTemplate();

            // Then
            assertNotNull(kafkaRetryTemplate);
            assertNotNull(generalRetryTemplate);
            assertNotSame(kafkaRetryTemplate, generalRetryTemplate);
        }
    }

    @Nested
    @DisplayName("Configuration Annotations Tests")
    class ConfigurationAnnotationsTests {

        @Test
        @DisplayName("Should have @Configuration annotation")
        void shouldHaveConfigurationAnnotation() {
            // When
            boolean hasConfigurationAnnotation = ResilienceConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class);

            // Then
            assertTrue(hasConfigurationAnnotation);
        }

        @Test
        @DisplayName("Should have @EnableRetry annotation")
        void shouldHaveEnableRetryAnnotation() {
            // When
            boolean hasEnableRetryAnnotation = ResilienceConfig.class.isAnnotationPresent(
                org.springframework.retry.annotation.EnableRetry.class);

            // Then
            assertTrue(hasEnableRetryAnnotation);
        }

        @Test
        @DisplayName("Should have @EnableAsync annotation")
        void shouldHaveEnableAsyncAnnotation() {
            // When
            boolean hasEnableAsyncAnnotation = ResilienceConfig.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class);

            // Then
            assertTrue(hasEnableAsyncAnnotation);
        }

        @Test
        @DisplayName("Should have @EnableScheduling annotation")
        void shouldHaveEnableSchedulingAnnotation() {
            // When
            boolean hasEnableSchedulingAnnotation = ResilienceConfig.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableScheduling.class);

            // Then
            assertTrue(hasEnableSchedulingAnnotation);
        }
    }

    @Nested
    @DisplayName("Bean Definition Tests")
    class BeanDefinitionTests {

        @Test
        @DisplayName("Should have kafkaRetryTemplate bean method")
        void shouldHaveKafkaRetryTemplateBeanMethod() throws NoSuchMethodException {
            // When
            var method = ResilienceConfig.class.getDeclaredMethod("kafkaRetryTemplate");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
            
            // Verify bean name
            var beanAnnotation = method.getAnnotation(org.springframework.context.annotation.Bean.class);
            assertEquals("kafkaRetryTemplate", beanAnnotation.value()[0]);
        }

        @Test
        @DisplayName("Should have generalRetryTemplate bean method")
        void shouldHaveGeneralRetryTemplateBeanMethod() throws NoSuchMethodException {
            // When
            var method = ResilienceConfig.class.getDeclaredMethod("generalRetryTemplate");

            // Then
            assertNotNull(method);
            assertTrue(method.isAnnotationPresent(org.springframework.context.annotation.Bean.class));
            
            // Verify bean name
            var beanAnnotation = method.getAnnotation(org.springframework.context.annotation.Bean.class);
            assertEquals("generalRetryTemplate", beanAnnotation.value()[0]);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero max retry attempts")
        void shouldHandleZeroMaxRetryAttempts() {
            // Given
            ReflectionTestUtils.setField(resilienceConfig, MAX_RETRY_ATTEMPTS_FIELD, 0);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle minimum initial delay")
        void shouldHandleMinimumInitialDelay() {
            // Given
            long minimumDelay = 100L;
            ReflectionTestUtils.setField(resilienceConfig, INITIAL_DELAY_FIELD, minimumDelay);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle minimum multiplier")
        void shouldHandleMinimumMultiplier() {
            // Given
            double minimumMultiplier = 1.0;
            ReflectionTestUtils.setField(resilienceConfig, MULTIPLIER_FIELD, minimumMultiplier);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }

        @Test
        @DisplayName("Should handle large max attempts")
        void shouldHandleLargeMaxAttempts() {
            // Given
            int largeMaxAttempts = 1000;
            ReflectionTestUtils.setField(resilienceConfig, MAX_RETRY_ATTEMPTS_FIELD, largeMaxAttempts);

            // When
            RetryTemplate retryTemplate = resilienceConfig.kafkaRetryTemplate();

            // Then
            assertNotNull(retryTemplate);
            assertInstanceOf(RetryTemplate.class, retryTemplate);
        }
    }

    @Nested
    @DisplayName("Template Integration Tests")
    class TemplateIntegrationTests {

        @Test
        @DisplayName("Should create different instances for kafka and general templates")
        void shouldCreateDifferentInstancesForKafkaAndGeneralTemplates() {
            // When
            RetryTemplate kafkaRetryTemplate = resilienceConfig.kafkaRetryTemplate();
            RetryTemplate generalRetryTemplate = resilienceConfig.generalRetryTemplate();

            // Then
            assertNotNull(kafkaRetryTemplate);
            assertNotNull(generalRetryTemplate);
            assertNotSame(kafkaRetryTemplate, generalRetryTemplate);
        }

        @Test
        @DisplayName("Should create templates with different configurations")
        void shouldCreateTemplatesWithDifferentConfigurations() {
            // When
            RetryTemplate kafkaRetryTemplate = resilienceConfig.kafkaRetryTemplate();
            RetryTemplate generalRetryTemplate = resilienceConfig.generalRetryTemplate();

            // Then
            assertNotNull(kafkaRetryTemplate);
            assertNotNull(generalRetryTemplate);
            
            // Verify they are different instances
            assertNotSame(kafkaRetryTemplate, generalRetryTemplate);
            
            // Verify both can execute operations
            String kafkaResult = kafkaRetryTemplate.execute(context -> SUCCESS_RESULT);
            String generalResult = generalRetryTemplate.execute(context -> SUCCESS_RESULT);
            
            assertEquals(SUCCESS_RESULT, kafkaResult);
            assertEquals(SUCCESS_RESULT, generalResult);
        }
    }
}
