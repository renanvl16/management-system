package com.inventory.management.store.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para {@link EventPublishingException}.
 * Verifica o comportamento correto da exceção personalizada para falhas de publicação.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("EventPublishingException Tests")
class EventPublishingExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar exceção com mensagem")
        void shouldCreateExceptionWithMessage() {
            // Given
            String expectedMessage = "Erro na publicação do evento";

            // When
            EventPublishingException exception = new EventPublishingException(expectedMessage);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
            assertThat(exception.getCause()).isNull();
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve criar exceção com mensagem e causa")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String expectedMessage = "Falha na publicação";
            RuntimeException expectedCause = new RuntimeException("Kafka indisponível");

            // When
            EventPublishingException exception = new EventPublishingException(expectedMessage, expectedCause);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
            assertThat(exception.getCause()).isEqualTo(expectedCause);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve aceitar mensagem null")
        void shouldAcceptNullMessage() {
            // When
            EventPublishingException exception = new EventPublishingException(null);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Deve aceitar causa null")
        void shouldAcceptNullCause() {
            // Given
            String message = "Mensagem de erro";

            // When
            EventPublishingException exception = new EventPublishingException(message, null);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("Deve manter cadeia de causas")
        void shouldMaintainCauseChain() {
            // Given
            RuntimeException rootCause = new RuntimeException("Root cause");
            IllegalStateException intermediateCause = new IllegalStateException("Intermediate", rootCause);
            EventPublishingException exception = new EventPublishingException("Main error", intermediateCause);

            // Then
            assertThat(exception.getCause()).isEqualTo(intermediateCause);
            assertThat(exception.getCause().getCause()).isEqualTo(rootCause);
        }

        @Test
        @DisplayName("Deve ser lançável e capturável")
        void shouldBeThrowableAndCatchable() {
            // Given
            String errorMessage = "Teste de lançamento";

            // When & Then
            try {
                throw new EventPublishingException(errorMessage);
            } catch (EventPublishingException e) {
                assertThat(e.getMessage()).isEqualTo(errorMessage);
            } catch (Exception e) {
                // Não deveria chegar aqui
                assertThat(e).as("Exceção não deveria ser capturada como Exception genérica").isNull();
            }
        }

        @Test
        @DisplayName("Deve ser capturável como RuntimeException")
        void shouldBeCatchableAsRuntimeException() {
            // Given
            String errorMessage = "Erro de runtime";
            Throwable cause = new IllegalArgumentException("Argumento inválido");

            // When & Then
            try {
                throw new EventPublishingException(errorMessage, cause);
            } catch (RuntimeException e) {
                assertThat(e).isInstanceOf(EventPublishingException.class);
                assertThat(e.getMessage()).isEqualTo(errorMessage);
                assertThat(e.getCause()).isEqualTo(cause);
            }
        }

        @Test
        @DisplayName("Deve preservar stack trace")
        void shouldPreserveStackTrace() {
            // When
            EventPublishingException exception = new EventPublishingException("Stack trace test");

            // Then
            assertThat(exception.getStackTrace()).isNotEmpty();
            assertThat(exception.getStackTrace()[0].getClassName())
                    .contains("EventPublishingExceptionTest");
            assertThat(exception.getStackTrace()[0].getMethodName())
                    .isEqualTo("shouldPreserveStackTrace");
        }

        @Test
        @DisplayName("Deve ser serializável")
        void shouldBeSerializable() {
            // Given
            EventPublishingException exception = new EventPublishingException(
                    "Erro de serialização", 
                    new RuntimeException("Causa original")
            );

            // When & Then
            assertThat(exception).isInstanceOf(java.io.Serializable.class);
        }
    }

    @Nested
    @DisplayName("Message Formatting Tests")
    class MessageFormattingTests {

        @Test
        @DisplayName("Deve formatar mensagem com parâmetros")
        void shouldFormatMessageWithParameters() {
            // Given
            String template = "Falha ao publicar evento %s no tópico %s";
            String eventId = "event-123";
            String topic = "inventory-update";
            String formattedMessage = String.format(template, eventId, topic);

            // When
            EventPublishingException exception = new EventPublishingException(formattedMessage);

            // Then
            assertThat(exception.getMessage()).contains(eventId);
            assertThat(exception.getMessage()).contains(topic);
            assertThat(exception.getMessage()).isEqualTo(
                    "Falha ao publicar evento event-123 no tópico inventory-update"
            );
        }

        @Test
        @DisplayName("Deve lidar com mensagens longas")
        void shouldHandleLongMessages() {
            // Given
            StringBuilder longMessage = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longMessage.append("Erro muito longo ");
            }

            // When
            EventPublishingException exception = new EventPublishingException(longMessage.toString());

            // Then
            assertThat(exception.getMessage()).isNotNull();
            assertThat(exception.getMessage().length()).isGreaterThan(5000);
        }

        @Test
        @DisplayName("Deve lidar com caracteres especiais")
        void shouldHandleSpecialCharacters() {
            // Given
            String messageWithSpecialChars = "Erro com 特殊文字, émojis 🚫, e símbolos @#$%^&*()";

            // When
            EventPublishingException exception = new EventPublishingException(messageWithSpecialChars);

            // Then
            assertThat(exception.getMessage()).isEqualTo(messageWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Integration with Exception Handling Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Deve funcionar em cenário de uso real")
        void shouldWorkInRealUsageScenario() {
            // Simula cenário real de uso da exceção
            
            // Given
            String eventId = "evt-12345";
            String topicName = "inventory-updates";
            RuntimeException kafkaError = new RuntimeException("Connection timeout");

            // When
            EventPublishingException publishError = new EventPublishingException(
                    String.format("Falha ao publicar evento %s no tópico %s", eventId, topicName),
                    kafkaError
            );

            // Then - Verifica se a exceção foi criada corretamente
            assertThat(publishError.getMessage())
                    .contains(eventId)
                    .contains(topicName);
            assertThat(publishError.getCause()).isEqualTo(kafkaError);
            assertThat(publishError.getCause().getMessage()).isEqualTo("Connection timeout");
        }

        @Test
        @DisplayName("Deve integrar com logging frameworks")
        void shouldIntegrateWithLoggingFrameworks() {
            // Given
            EventPublishingException exception = new EventPublishingException(
                    "Erro para log",
                    new IllegalStateException("Estado inválido")
            );

            // When - Simula logging (toString é usado por frameworks de log)
            String logMessage = exception.toString();

            // Then
            assertThat(logMessage).contains("EventPublishingException");
            assertThat(logMessage).contains("Erro para log");
        }
    }
}
