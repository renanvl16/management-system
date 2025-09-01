package com.inventory.management.store.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ApplicationConfig.
 * Valida a configuração e funcionamento do ObjectMapper.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationConfig Tests")
@TestPropertySource(properties = {
    "spring.profiles.active=test"
})
class ApplicationConfigTest {

    private ApplicationConfig applicationConfig;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
        objectMapper = applicationConfig.objectMapper();
    }

    @Nested
    @DisplayName("ObjectMapper Bean Configuration")
    class ObjectMapperBeanTests {

        @Test
        @DisplayName("Should create ObjectMapper bean")
        void shouldCreateObjectMapperBean() {
            // When
            ObjectMapper result = applicationConfig.objectMapper();

            // Then
            assertNotNull(result);
            assertInstanceOf(ObjectMapper.class, result);
        }

        @Test
        @DisplayName("Should register JavaTimeModule")
        void shouldRegisterJavaTimeModule() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);

            // When & Then
            assertDoesNotThrow(() -> {
                String json = objectMapper.writeValueAsString(dateTime);
                LocalDateTime deserializedDateTime = objectMapper.readValue(json, LocalDateTime.class);
                assertEquals(dateTime, deserializedDateTime);
            });
        }

        @Test
        @DisplayName("Should serialize LocalDateTime correctly")
        void shouldSerializeLocalDateTimeCorrectly() throws JsonProcessingException {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 15, 30, 45);

            // When
            String json = objectMapper.writeValueAsString(dateTime);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("2023"));
            assertTrue(json.contains("12"));
            assertTrue(json.contains("25"));
        }

        @Test
        @DisplayName("Should deserialize LocalDateTime correctly")
        void shouldDeserializeLocalDateTimeCorrectly() throws JsonProcessingException {
            // Given
            String json = "\"2023-12-25T15:30:45\"";

            // When
            LocalDateTime result = objectMapper.readValue(json, LocalDateTime.class);

            // Then
            assertNotNull(result);
            assertEquals(2023, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(25, result.getDayOfMonth());
            assertEquals(15, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(45, result.getSecond());
        }

        @Test
        @DisplayName("Should serialize and deserialize complex object with LocalDateTime")
        void shouldSerializeAndDeserializeComplexObjectWithLocalDateTime() throws JsonProcessingException {
            // Given
            TestObject testObject = new TestObject();
            testObject.setId("test-id");
            testObject.setName("Test Name");
            testObject.setCreatedAt(LocalDateTime.of(2023, 12, 25, 15, 30, 45));

            // When
            String json = objectMapper.writeValueAsString(testObject);
            TestObject deserializedObject = objectMapper.readValue(json, TestObject.class);

            // Then
            assertNotNull(deserializedObject);
            assertEquals(testObject.getId(), deserializedObject.getId());
            assertEquals(testObject.getName(), deserializedObject.getName());
            assertEquals(testObject.getCreatedAt(), deserializedObject.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle Map serialization correctly")
        void shouldHandleMapSerializationCorrectly() throws JsonProcessingException {
            // Given
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("id", "test-id");
            testMap.put("name", "Test Name");
            testMap.put("createdAt", LocalDateTime.of(2023, 12, 25, 15, 30, 45));

            // When
            String json = objectMapper.writeValueAsString(testMap);
            @SuppressWarnings("unchecked")
            Map<String, Object> deserializedMap = objectMapper.readValue(json, Map.class);

            // Then
            assertNotNull(deserializedMap);
            assertEquals(testMap.get("id"), deserializedMap.get("id"));
            assertEquals(testMap.get("name"), deserializedMap.get("name"));
            assertNotNull(deserializedMap.get("createdAt"));
        }

        @Test
        @DisplayName("Should be thread safe")
        void shouldBeThreadSafe() {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            Exception[] exceptions = new Exception[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                threads[i] = new Thread(() -> {
                    try {
                        LocalDateTime dateTime = LocalDateTime.now().plusDays(threadIndex);
                        String json = objectMapper.writeValueAsString(dateTime);
                        LocalDateTime result = objectMapper.readValue(json, LocalDateTime.class);
                        assertEquals(dateTime, result);
                    } catch (Exception e) {
                        exceptions[threadIndex] = e;
                    }
                });
                threads[i].start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread was interrupted");
                }
            }

            // Then
            for (Exception exception : exceptions) {
                assertNull(exception, "Thread should not have thrown exception: " + 
                    (exception != null ? exception.getMessage() : ""));
            }
        }
    }

    @Nested
    @DisplayName("Configuration Annotations")
    class ConfigurationAnnotationsTests {

        @Test
        @DisplayName("Should have @Configuration annotation")
        void shouldHaveConfigurationAnnotation() {
            // When
            boolean hasConfigurationAnnotation = ApplicationConfig.class.isAnnotationPresent(
                org.springframework.context.annotation.Configuration.class);

            // Then
            assertTrue(hasConfigurationAnnotation);
        }

        @Test
        @DisplayName("Should have @EnableAsync annotation")
        void shouldHaveEnableAsyncAnnotation() {
            // When
            boolean hasEnableAsyncAnnotation = ApplicationConfig.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class);

            // Then
            assertTrue(hasEnableAsyncAnnotation);
        }
    }

    // Test helper class
    public static class TestObject {
        private String id;
        private String name;
        private LocalDateTime createdAt;

        // Default constructor for Jackson deserialization
        public TestObject() {
            // Required for Jackson deserialization
        }

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
