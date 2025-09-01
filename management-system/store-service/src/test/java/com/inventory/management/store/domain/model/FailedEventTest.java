package com.inventory.management.store.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para FailedEvent.
 * Valida lógica de retry, status e criação de eventos falhados.
 */
@DisplayName("FailedEvent Tests")
class FailedEventTest {

    private static final String EVENT_ID = "event-123";
    private static final String EVENT_TYPE = "INVENTORY_UPDATE";
    private static final String TOPIC = "inventory-events";
    private static final String PARTITION_KEY = "STORE-001";
    private static final String EVENT_PAYLOAD = "{\"productSku\":\"PROD-001\"}";
    private static final String ERROR_MESSAGE = "Connection timeout";
    
    // Test constants
    private static final String TEST_TYPE = "TEST_TYPE";
    private static final String TEST_TOPIC = "test-topic";
    private static final String TEST_PARTITION = "test-partition";
    private static final String SAME_ID = "same-id";
    private static final String SAME_TYPE = "SAME_TYPE";
    private static final String DIFFERENT_ID = "different-id";
    private static final String DIFFERENT_TYPE = "DIFFERENT_TYPE";
    
    private FailedEvent failedEvent;
    
    @BeforeEach
    void setUp() {
        failedEvent = new FailedEvent();
        failedEvent.setEventId(EVENT_ID);
        failedEvent.setEventType(EVENT_TYPE);
        failedEvent.setTopic(TOPIC);
        failedEvent.setPartitionKey(PARTITION_KEY);
        failedEvent.setEventPayload(EVENT_PAYLOAD);
        failedEvent.setLastError(ERROR_MESSAGE);
        failedEvent.setCreatedAt(LocalDateTime.now());
        failedEvent.setRetryCount(0);
        failedEvent.setMaxRetries(10);
        failedEvent.setStatus(FailedEvent.FailedEventStatus.PENDING);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create with default constructor")
        void shouldCreateWithDefaultConstructor() {
            // When
            FailedEvent event = new FailedEvent();
            
            // Then
            assertNull(event.getId());
            assertNull(event.getEventId());
            assertEquals(0, event.getRetryCount());
            assertEquals(10, event.getMaxRetries());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
        }
        
        @Test
        @DisplayName("Should create with all args constructor")
        void shouldCreateWithAllArgsConstructor() {
            // Given
            Long id = 1L;
            LocalDateTime createdAt = LocalDateTime.now();
            
            // When
            FailedEvent event = new FailedEvent(
                id, EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD,
                5, 10, ERROR_MESSAGE, createdAt, null, null,
                FailedEvent.FailedEventStatus.PROCESSING
            );
            
            // Then
            assertEquals(id, event.getId());
            assertEquals(EVENT_ID, event.getEventId());
            assertEquals(EVENT_TYPE, event.getEventType());
            assertEquals(TOPIC, event.getTopic());
            assertEquals(PARTITION_KEY, event.getPartitionKey());
            assertEquals(EVENT_PAYLOAD, event.getEventPayload());
            assertEquals(5, event.getRetryCount());
            assertEquals(10, event.getMaxRetries());
            assertEquals(ERROR_MESSAGE, event.getLastError());
            assertEquals(createdAt, event.getCreatedAt());
            assertEquals(FailedEvent.FailedEventStatus.PROCESSING, event.getStatus());
        }
        
        @Test
        @DisplayName("Should create with factory method")
        void shouldCreateWithFactoryMethod() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            
            // When
            FailedEvent event = FailedEvent.create(
                EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE);
            
            // Then
            assertEquals(EVENT_ID, event.getEventId());
            assertEquals(EVENT_TYPE, event.getEventType());
            assertEquals(TOPIC, event.getTopic());
            assertEquals(PARTITION_KEY, event.getPartitionKey());
            assertEquals(EVENT_PAYLOAD, event.getEventPayload());
            assertEquals(ERROR_MESSAGE, event.getLastError());
            assertTrue(event.getCreatedAt().isAfter(beforeCreation));
            assertEquals(1, event.getRetryCount()); // Factory method calls incrementRetry()
            assertNotNull(event.getNextRetryAt());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
        }
    }
    
    @Nested
    @DisplayName("Retry Logic Tests")
    class RetryLogicTests {
        
        @Test
        @DisplayName("Should increment retry count correctly")
        void shouldIncrementRetryCountCorrectly() {
            // Given
            Integer initialRetryCount = failedEvent.getRetryCount();
            
            // When
            failedEvent.incrementRetry();
            
            // Then
            assertEquals(initialRetryCount + 1, failedEvent.getRetryCount());
            assertNotNull(failedEvent.getLastRetryAt());
            assertNotNull(failedEvent.getNextRetryAt());
            assertTrue(failedEvent.getNextRetryAt().isAfter(LocalDateTime.now()));
        }
        
        @Test
        @DisplayName("Should set status to FAILED when max retries reached")
        void shouldSetStatusToFailedWhenMaxRetriesReached() {
            // Given
            failedEvent.setRetryCount(9); // One less than maxRetries (10)
            
            // When
            failedEvent.incrementRetry();
            
            // Then
            assertEquals(10, failedEvent.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.FAILED, failedEvent.getStatus());
            assertNull(failedEvent.getNextRetryAt());
        }
        
        @Test
        @DisplayName("Should calculate exponential backoff correctly")
        void shouldCalculateExponentialBackoffCorrectly() {
            // Given
            failedEvent.setRetryCount(0);
            LocalDateTime beforeRetry = LocalDateTime.now();
            
            // When
            failedEvent.incrementRetry();
            
            // Then
            assertTrue(failedEvent.getNextRetryAt().isAfter(beforeRetry.plusMinutes(1)));
            assertTrue(failedEvent.getNextRetryAt().isBefore(beforeRetry.plusMinutes(5))); // With jitter
        }
        
        @Test
        @DisplayName("Should cap maximum delay to 24 hours")
        void shouldCapMaximumDelayTo24Hours() {
            // Given
            failedEvent.setRetryCount(20); // Very high retry count
            failedEvent.setMaxRetries(30); // Ensure it doesn't reach max
            LocalDateTime beforeRetry = LocalDateTime.now();
            
            // When
            failedEvent.incrementRetry();
            
            // Then - Should have a next retry time (not null when max retries not reached)
            assertNotNull(failedEvent.getNextRetryAt());
            assertTrue(failedEvent.getNextRetryAt().isBefore(beforeRetry.plusHours(30))); // Max 24h + jitter
        }
        
        @Test
        @DisplayName("Should determine if ready for retry correctly")
        void shouldDetermineIfReadyForRetryCorrectly() {
            // Given
            failedEvent.setStatus(FailedEvent.FailedEventStatus.PENDING);
            failedEvent.setNextRetryAt(LocalDateTime.now().minusMinutes(1)); // Past time
            
            // When & Then
            assertTrue(failedEvent.isReadyForRetry());
            
            // Given
            failedEvent.setNextRetryAt(LocalDateTime.now().plusMinutes(1)); // Future time
            
            // When & Then
            assertFalse(failedEvent.isReadyForRetry());
            
            // Given
            failedEvent.setStatus(FailedEvent.FailedEventStatus.FAILED);
            failedEvent.setNextRetryAt(LocalDateTime.now().minusMinutes(1));
            
            // When & Then
            assertFalse(failedEvent.isReadyForRetry());
        }
    }
    
    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {
        
        @Test
        @DisplayName("Should mark as succeeded correctly")
        void shouldMarkAsSucceededCorrectly() {
            // Given
            failedEvent.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
            
            // When
            failedEvent.markAsSucceeded();
            
            // Then
            assertEquals(FailedEvent.FailedEventStatus.SUCCEEDED, failedEvent.getStatus());
            assertNull(failedEvent.getNextRetryAt());
        }
        
        @Test
        @DisplayName("Should have all required status values")
        void shouldHaveAllRequiredStatusValues() {
            // Then
            assertNotNull(FailedEvent.FailedEventStatus.PENDING);
            assertNotNull(FailedEvent.FailedEventStatus.PROCESSING);
            assertNotNull(FailedEvent.FailedEventStatus.SUCCEEDED);
            assertNotNull(FailedEvent.FailedEventStatus.FAILED);
            assertNotNull(FailedEvent.FailedEventStatus.CANCELLED);
        }
        
        @Test
        @DisplayName("Should have exactly 5 status values")
        void shouldHaveExactlyFiveStatusValues() {
            // When
            FailedEvent.FailedEventStatus[] statuses = FailedEvent.FailedEventStatus.values();
            
            // Then
            assertEquals(5, statuses.length);
        }
    }
    
    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Should handle retry sequence correctly")
        void shouldHandleRetrySequenceCorrectly() {
            // Given
            FailedEvent event = FailedEvent.create(
                EVENT_ID, EVENT_TYPE, TOPIC, PARTITION_KEY, EVENT_PAYLOAD, ERROR_MESSAGE);
            
            // Then - Initial state after creation
            assertEquals(1, event.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
            assertNotNull(event.getNextRetryAt());
            
            // When - Multiple retries
            for (int i = 2; i <= 9; i++) {
                event.incrementRetry();
                assertEquals(i, event.getRetryCount());
                assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
                assertNotNull(event.getNextRetryAt());
            }
            
            // When - Final retry (reaches max)
            event.incrementRetry();
            
            // Then - Should be marked as failed
            assertEquals(10, event.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.FAILED, event.getStatus());
            assertNull(event.getNextRetryAt());
        }
        
        @Test
        @DisplayName("Should preserve original event data during retries")
        void shouldPreserveOriginalEventDataDuringRetries() {
            // When
            failedEvent.incrementRetry();
            failedEvent.incrementRetry();
            
            // Then - Original data should remain unchanged
            assertEquals(EVENT_ID, failedEvent.getEventId());
            assertEquals(EVENT_TYPE, failedEvent.getEventType());
            assertEquals(TOPIC, failedEvent.getTopic());
            assertEquals(PARTITION_KEY, failedEvent.getPartitionKey());
            assertEquals(EVENT_PAYLOAD, failedEvent.getEventPayload());
        }
        
        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // Given
            FailedEvent event = FailedEvent.create(
                EVENT_ID, EVENT_TYPE, TOPIC, null, EVENT_PAYLOAD, null);
            
            // Then
            assertEquals(EVENT_ID, event.getEventId());
            assertEquals(EVENT_TYPE, event.getEventType());
            assertEquals(TOPIC, event.getTopic());
            assertNull(event.getPartitionKey());
            assertEquals(EVENT_PAYLOAD, event.getEventPayload());
            assertNull(event.getLastError());
        }
        
        @Test
        @DisplayName("Should handle edge cases in retry calculation")
        void shouldHandleEdgeCasesInRetryCalculation() {
            // Given
            FailedEvent event = new FailedEvent();
            event.setMaxRetries(1);
            event.setRetryCount(0);
            event.setStatus(FailedEvent.FailedEventStatus.PENDING);
            
            // When - Should fail immediately after first retry
            event.incrementRetry();
            
            // Then
            assertEquals(1, event.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.FAILED, event.getStatus());
            assertNull(event.getNextRetryAt());
        }
        
        @Test
        @DisplayName("Should handle very high retry counts")
        void shouldHandleVeryHighRetryCounts() {
            // Given
            FailedEvent event = new FailedEvent();
            event.setMaxRetries(100);
            event.setRetryCount(50);
            event.setStatus(FailedEvent.FailedEventStatus.PENDING);
            LocalDateTime beforeRetry = LocalDateTime.now();
            
            // When
            event.incrementRetry();
            
            // Then - Should calculate next retry time without errors
            assertEquals(51, event.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
            assertNotNull(event.getNextRetryAt());
            assertTrue(event.getNextRetryAt().isAfter(beforeRetry));
        }
        
        @Test
        @DisplayName("Should respect custom max retries")
        void shouldRespectCustomMaxRetries() {
            // Given
            FailedEvent event = new FailedEvent();
            event.setMaxRetries(3);
            event.setRetryCount(0);
            event.setStatus(FailedEvent.FailedEventStatus.PENDING);
            
            // When & Then - Should fail after 3 retries
            for (int i = 1; i <= 2; i++) {
                event.incrementRetry();
                assertEquals(i, event.getRetryCount());
                assertEquals(FailedEvent.FailedEventStatus.PENDING, event.getStatus());
                assertNotNull(event.getNextRetryAt());
            }
            
            // Final retry should mark as failed
            event.incrementRetry();
            assertEquals(3, event.getRetryCount());
            assertEquals(FailedEvent.FailedEventStatus.FAILED, event.getStatus());
            assertNull(event.getNextRetryAt());
        }
        
        @Test
        @DisplayName("Should handle concurrent status changes")
        void shouldHandleConcurrentStatusChanges() {
            // Given
            failedEvent.setStatus(FailedEvent.FailedEventStatus.PROCESSING);
            failedEvent.setNextRetryAt(LocalDateTime.now().minusMinutes(5));
            
            // When & Then - Should not be ready for retry when processing
            assertFalse(failedEvent.isReadyForRetry());
            
            // Given - Change to succeeded
            failedEvent.markAsSucceeded();
            
            // When & Then - Should not be ready for retry when succeeded
            assertFalse(failedEvent.isReadyForRetry());
            assertEquals(FailedEvent.FailedEventStatus.SUCCEEDED, failedEvent.getStatus());
        }
    }
    
    @Nested
    @DisplayName("Additional Lombok Generated Methods Tests")
    class AdditionalLombokGeneratedMethodsTests {
        
        @Test
        @DisplayName("Should test all getters and setters")
        void shouldTestAllGettersAndSetters() {
            // Given
            FailedEvent event = new FailedEvent();
            Long id = 123L;
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime lastRetryAt = LocalDateTime.now();
            LocalDateTime nextRetryAt = LocalDateTime.now().plusHours(1);
            String errorMessage = "New error message";
            Integer retryCount = 5;
            Integer maxRetries = 15;
            
            // When - Set all fields
            event.setId(id);
            event.setEventId("new-event-id");
            event.setEventType("NEW_TYPE");
            event.setTopic("new-topic");
            event.setPartitionKey("new-partition");
            event.setEventPayload("new payload");
            event.setRetryCount(retryCount);
            event.setMaxRetries(maxRetries);
            event.setLastError(errorMessage);
            event.setCreatedAt(createdAt);
            event.setLastRetryAt(lastRetryAt);
            event.setNextRetryAt(nextRetryAt);
            event.setStatus(FailedEvent.FailedEventStatus.PROCESSING);
            
            // Then - Verify all fields
            assertEquals(id, event.getId());
            assertEquals("new-event-id", event.getEventId());
            assertEquals("NEW_TYPE", event.getEventType());
            assertEquals("new-topic", event.getTopic());
            assertEquals("new-partition", event.getPartitionKey());
            assertEquals("new payload", event.getEventPayload());
            assertEquals(retryCount, event.getRetryCount());
            assertEquals(maxRetries, event.getMaxRetries());
            assertEquals(errorMessage, event.getLastError());
            assertEquals(createdAt, event.getCreatedAt());
            assertEquals(lastRetryAt, event.getLastRetryAt());
            assertEquals(nextRetryAt, event.getNextRetryAt());
            assertEquals(FailedEvent.FailedEventStatus.PROCESSING, event.getStatus());
        }
        
        @Test
        @DisplayName("Should test constructor variations")
        void shouldTestConstructorVariations() {
            // Test no-args constructor
            FailedEvent defaultEvent = new FailedEvent();
            assertNull(defaultEvent.getId());
            assertEquals(0, defaultEvent.getRetryCount());
            assertEquals(10, defaultEvent.getMaxRetries());
            assertEquals(FailedEvent.FailedEventStatus.PENDING, defaultEvent.getStatus());
            
            // Test all-args constructor with all fields
            LocalDateTime now = LocalDateTime.now();
            FailedEvent fullEvent = new FailedEvent(
                100L, "test-event", TEST_TYPE, TEST_TOPIC, TEST_PARTITION,
                "test payload", 3, 5, "test error", now, now.minusHours(1),
                now.plusHours(1), FailedEvent.FailedEventStatus.PROCESSING
            );
            
            assertEquals(100L, fullEvent.getId());
            assertEquals("test-event", fullEvent.getEventId());
            assertEquals(TEST_TYPE, fullEvent.getEventType());
            assertEquals(TEST_TOPIC, fullEvent.getTopic());
            assertEquals(TEST_PARTITION, fullEvent.getPartitionKey());
            assertEquals("test payload", fullEvent.getEventPayload());
            assertEquals(3, fullEvent.getRetryCount());
            assertEquals(5, fullEvent.getMaxRetries());
            assertEquals("test error", fullEvent.getLastError());
            assertEquals(now, fullEvent.getCreatedAt());
            assertEquals(now.minusHours(1), fullEvent.getLastRetryAt());
            assertEquals(now.plusHours(1), fullEvent.getNextRetryAt());
            assertEquals(FailedEvent.FailedEventStatus.PROCESSING, fullEvent.getStatus());
        }
        
        @Test
        @DisplayName("Should support equals contract")
        void shouldSupportEqualsContract() {
            // Given
            FailedEvent event1 = new FailedEvent();
            event1.setEventId(SAME_ID);
            event1.setEventType(SAME_TYPE);
            
            FailedEvent event2 = new FailedEvent();
            event2.setEventId(SAME_ID);
            event2.setEventType(SAME_TYPE);
            
            FailedEvent event3 = new FailedEvent();
            event3.setEventId(DIFFERENT_ID);
            event3.setEventType(DIFFERENT_TYPE);
            
            // Then - Test equals contract
            assertEquals(event1, event1); // Reflexive
            assertEquals(event1, event2); // Symmetric
            assertEquals(event2, event1); // Symmetric
            assertNotEquals(event1, event3); // Different objects
            assertNotEquals(null, event1); // Null comparison
            assertNotEquals("not an event", event1); // Different class
        }
        
        @Test
        @DisplayName("Should support hashCode contract")
        void shouldSupportHashCodeContract() {
            // Given
            FailedEvent event1 = new FailedEvent();
            event1.setEventId(SAME_ID);
            event1.setEventType(SAME_TYPE);
            event1.setRetryCount(5);
            
            FailedEvent event2 = new FailedEvent();
            event2.setEventId(SAME_ID);
            event2.setEventType(SAME_TYPE);
            event2.setRetryCount(5);
            
            FailedEvent event3 = new FailedEvent();
            event3.setEventId(DIFFERENT_ID);
            event3.setEventType(DIFFERENT_TYPE);
            event3.setRetryCount(3);
            
            // Then - HashCode contract
            assertEquals(event1.hashCode(), event1.hashCode()); // Consistent
            assertEquals(event1.hashCode(), event2.hashCode()); // Equal objects same hash
            
            // Different objects may have different hash codes
            // (not guaranteed, but likely)
            assertNotEquals(event1.hashCode(), event3.hashCode());
        }
        
        @Test
        @DisplayName("Should have comprehensive toString")
        void shouldHaveComprehensiveToString() {
            // Given
            FailedEvent event = new FailedEvent();
            event.setId(456L);
            event.setEventId("test-event-id");
            event.setEventType(TEST_TYPE);
            event.setTopic(TEST_TOPIC);
            event.setPartitionKey(TEST_PARTITION);
            event.setRetryCount(7);
            event.setStatus(FailedEvent.FailedEventStatus.PROCESSING);
            
            // When
            String toString = event.toString();
            
            // Then - Should contain key information
            assertTrue(toString.contains("456"));
            assertTrue(toString.contains("test-event-id"));
            assertTrue(toString.contains(TEST_TYPE));
            assertTrue(toString.contains(TEST_TOPIC));
            assertTrue(toString.contains("7"));
            assertTrue(toString.contains("PROCESSING"));
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            FailedEvent event1 = new FailedEvent();
            event1.setEventId(EVENT_ID);
            event1.setEventType(EVENT_TYPE);
            
            FailedEvent event2 = new FailedEvent();
            event2.setEventId(EVENT_ID);
            event2.setEventType(EVENT_TYPE);
            
            FailedEvent event3 = new FailedEvent();
            event3.setEventId("different-id");
            event3.setEventType(EVENT_TYPE);
            
            // Then
            assertEquals(event1, event2);
            assertEquals(event1.hashCode(), event2.hashCode());
            assertNotEquals(event1, event3);
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            failedEvent.setId(123L);
            
            // When
            String toString = failedEvent.toString();
            
            // Then
            assertTrue(toString.contains(EVENT_ID));
            assertTrue(toString.contains(EVENT_TYPE));
            assertTrue(toString.contains("123"));
        }
    }
}
