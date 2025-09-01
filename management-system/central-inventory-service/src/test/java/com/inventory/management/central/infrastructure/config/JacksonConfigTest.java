package com.inventory.management.central.infrastructure.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JacksonConfig - Testes Unitários")
class JacksonConfigTest {

    @InjectMocks
    private JacksonConfig jacksonConfig;

    @Test
    @DisplayName("Deve criar ObjectMapper configurado corretamente")
    void shouldCreateObjectMapperWithCorrectConfiguration() {
        // When
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // Then
        assertThat(objectMapper).isNotNull();

        // Verificar se o módulo JavaTimeModule foi registrado (o ID pode variar)
        assertThat(objectMapper.getRegisteredModuleIds())
                .contains("jackson-datatype-jsr310");

        // Verificar se WRITE_DATES_AS_TIMESTAMPS está desabilitado
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS))
                .isFalse();

        // Verificar se FAIL_ON_UNKNOWN_PROPERTIES está desabilitado
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))
                .isFalse();
    }

    @Test
    @DisplayName("Deve serializar LocalDateTime corretamente")
    void shouldSerializeLocalDateTimeCorrectly() throws JsonProcessingException {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 14, 30, 45);
        TestObject testObject = new TestObject(dateTime);

        // When
        String json = objectMapper.writeValueAsString(testObject);

        // Then
        assertThat(json).contains("2025-01-15T14:30:45");
        assertThat(json).doesNotContain("[2025,1,15,14,30,45,0]"); // Não deve ser array
    }

    @Test
    @DisplayName("Deve deserializar LocalDateTime corretamente")
    void shouldDeserializeLocalDateTimeCorrectly() throws JsonProcessingException {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String json = """
                {
                    "timestamp": "2025-01-15T14:30:45"
                }
                """;

        // When
        TestObject result = objectMapper.readValue(json, TestObject.class);

        // Then
        assertThat(result.timestamp).isEqualTo(LocalDateTime.of(2025, 1, 15, 14, 30, 45));
    }

    @Test
    @DisplayName("Deve ignorar propriedades desconhecidas sem falhar")
    void shouldIgnoreUnknownPropertiesWithoutFailing() {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        String jsonWithUnknownProperty = """
                {
                    "timestamp": "2025-01-15T14:30:45",
                    "unknownProperty": "this should be ignored",
                    "anotherUnknown": 123
                }
                """;

        // When & Then
        assertThatNoException().isThrownBy(() -> {
            TestObject result = objectMapper.readValue(jsonWithUnknownProperty, TestObject.class);
            assertThat(result.timestamp).isEqualTo(LocalDateTime.of(2025, 1, 15, 14, 30, 45));
        });
    }

    @Test
    @DisplayName("Deve serializar e deserializar objetos complexos")
    void shouldSerializeAndDeserializeComplexObjects() throws JsonProcessingException {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        ComplexTestObject original = new ComplexTestObject(
                LocalDateTime.of(2025, 1, 15, 10, 30),
                LocalDateTime.of(2025, 1, 15, 12, 45),
                "Test Name"
        );

        // When
        String json = objectMapper.writeValueAsString(original);
        ComplexTestObject deserialized = objectMapper.readValue(json, ComplexTestObject.class);

        // Then
        assertThat(deserialized.createdAt).isEqualTo(original.createdAt);
        assertThat(deserialized.updatedAt).isEqualTo(original.updatedAt);
        assertThat(deserialized.name).isEqualTo(original.name);
    }

    @Test
    @DisplayName("Deve tratar valores null corretamente")
    void shouldHandleNullValuesCorrectly() throws JsonProcessingException {
        // Given
        ObjectMapper objectMapper = jacksonConfig.objectMapper();
        TestObject testObjectWithNull = new TestObject(null);

        // When
        String json = objectMapper.writeValueAsString(testObjectWithNull);
        TestObject result = objectMapper.readValue(json, TestObject.class);

        // Then
        assertThat(result.timestamp).isNull();
    }

    @Test
    @DisplayName("Deve ser anotado como @Primary")
    void shouldBeAnnotatedAsPrimary() throws NoSuchMethodException {
        // Given
        var method = JacksonConfig.class.getMethod("objectMapper");

        // When & Then
        assertThat(method.isAnnotationPresent(org.springframework.context.annotation.Primary.class))
                .isTrue();
    }

    // Classes auxiliares para os testes
    public static class TestObject {
        public LocalDateTime timestamp;

        public TestObject() {}

        public TestObject(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class ComplexTestObject {
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public String name;

        public ComplexTestObject() {}

        public ComplexTestObject(LocalDateTime createdAt, LocalDateTime updatedAt, String name) {
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.name = name;
        }
    }
}
