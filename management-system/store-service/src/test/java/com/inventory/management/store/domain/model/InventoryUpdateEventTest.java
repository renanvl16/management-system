package com.inventory.management.store.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para InventoryUpdateEvent.
 * Valida criação de eventos e métodos de factory.
 */
@DisplayName("InventoryUpdateEvent Tests")
class InventoryUpdateEventTest {

    private static final String PRODUCT_SKU = "PROD-001";
    private static final String STORE_ID = "STORE-001";
    private static final Integer PREVIOUS_QUANTITY = 100;
    private static final Integer NEW_QUANTITY = 95;
    private static final Integer RESERVED_QUANTITY = 5;
    
    // Event type string constants
    private static final String EVENT_TYPE_RESERVE = "RESERVE";
    private static final String EVENT_TYPE_COMMIT = "COMMIT";
    private static final String EVENT_TYPE_CANCEL = "CANCEL";
    private static final String EVENT_TYPE_UPDATE = "UPDATE";
    private static final String EVENT_TYPE_RESTOCK = "RESTOCK";
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create event with builder")
        void shouldCreateEventWithBuilder() {
            // Given
            UUID eventId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.now();
            String details = "Teste de evento";
            
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.builder()
                    .eventId(eventId)
                    .productSku(PRODUCT_SKU)
                    .storeId(STORE_ID)
                    .eventType(InventoryUpdateEvent.EventType.RESERVE)
                    .previousQuantity(PREVIOUS_QUANTITY)
                    .newQuantity(NEW_QUANTITY)
                    .reservedQuantity(RESERVED_QUANTITY)
                    .timestamp(timestamp)
                    .details(details)
                    .build();
            
            // Then
            assertEquals(eventId, event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertEquals(timestamp, event.getTimestamp());
            assertEquals(details, event.getDetails());
        }
        
        @Test
        @DisplayName("Should create event with default constructor")
        void shouldCreateEventWithDefaultConstructor() {
            // When
            InventoryUpdateEvent event = new InventoryUpdateEvent();
            
            // Then
            assertNull(event.getEventId());
            assertNull(event.getProductSku());
            assertNull(event.getStoreId());
            assertNull(event.getEventType());
            assertNull(event.getPreviousQuantity());
            assertNull(event.getNewQuantity());
            assertNull(event.getReservedQuantity());
            assertNull(event.getTimestamp());
            assertNull(event.getDetails());
        }
        
        @Test
        @DisplayName("Should create event with all args constructor")
        void shouldCreateEventWithAllArgsConstructor() {
            // Given
            UUID eventId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.now();
            String details = "Teste de evento";
            
            // When
            InventoryUpdateEvent event = new InventoryUpdateEvent(
                eventId, PRODUCT_SKU, STORE_ID, InventoryUpdateEvent.EventType.COMMIT,
                PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY, timestamp, details
            );
            
            // Then
            assertEquals(eventId, event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.COMMIT, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertEquals(timestamp, event.getTimestamp());
            assertEquals(details, event.getDetails());
        }
    }
    
    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {
        
        @Test
        @DisplayName("Should create reserve event correctly")
        void shouldCreateReserveEventCorrectly() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.createReserveEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            
            // Then
            assertNotNull(event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertTrue(event.getTimestamp().isAfter(beforeCreation));
            assertEquals("Produto reservado para checkout", event.getDetails());
        }
        
        @Test
        @DisplayName("Should create commit event correctly")
        void shouldCreateCommitEventCorrectly() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.createCommitEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            
            // Then
            assertNotNull(event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.COMMIT, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertTrue(event.getTimestamp().isAfter(beforeCreation));
            assertEquals("Venda confirmada", event.getDetails());
        }
        
        @Test
        @DisplayName("Should create cancel event correctly")
        void shouldCreateCancelEventCorrectly() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.createCancelEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            
            // Then
            assertNotNull(event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.CANCEL, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertTrue(event.getTimestamp().isAfter(beforeCreation));
            assertEquals("Reserva cancelada", event.getDetails());
        }
        
        @Test
        @DisplayName("Should generate unique event IDs")
        void shouldGenerateUniqueEventIds() {
            // When
            InventoryUpdateEvent event1 = InventoryUpdateEvent.createReserveEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            InventoryUpdateEvent event2 = InventoryUpdateEvent.createReserveEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            
            // Then
            assertNotEquals(event1.getEventId(), event2.getEventId());
        }
        
        @Test
        @DisplayName("Should create events with null parameters")
        void shouldCreateEventsWithNullParameters() {
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.createReserveEvent(
                null, null, null, null, null);
            
            // Then
            assertNull(event.getProductSku());
            assertNull(event.getStoreId());
            assertNull(event.getPreviousQuantity());
            assertNull(event.getNewQuantity());
            assertNull(event.getReservedQuantity());
            assertNotNull(event.getEventId());
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, event.getEventType());
        }
        
        @Test
        @DisplayName("Should create events with factory methods for all types")
        void shouldCreateEventsWithFactoryMethodsForAllTypes() {
            // When
            InventoryUpdateEvent reserveEvent = InventoryUpdateEvent.createReserveEvent(
                PRODUCT_SKU, STORE_ID, 100, 95, 5);
            InventoryUpdateEvent commitEvent = InventoryUpdateEvent.createCommitEvent(
                PRODUCT_SKU, STORE_ID, 95, 90, 0);  
            InventoryUpdateEvent cancelEvent = InventoryUpdateEvent.createCancelEvent(
                PRODUCT_SKU, STORE_ID, 90, 90, 0);
            
            // Then
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, reserveEvent.getEventType());
            assertEquals("Produto reservado para checkout", reserveEvent.getDetails());
            
            assertEquals(InventoryUpdateEvent.EventType.COMMIT, commitEvent.getEventType());
            assertEquals("Venda confirmada", commitEvent.getDetails());
            
            assertEquals(InventoryUpdateEvent.EventType.CANCEL, cancelEvent.getEventType());
            assertEquals("Reserva cancelada", cancelEvent.getDetails());
        }
    }
    
    @Nested
    @DisplayName("EventType Enum Tests")
    class EventTypeEnumTests {
        
        @Test
        @DisplayName("Should have all required event types")
        void shouldHaveAllRequiredEventTypes() {
            // Then
            assertNotNull(InventoryUpdateEvent.EventType.RESERVE);
            assertNotNull(InventoryUpdateEvent.EventType.COMMIT);
            assertNotNull(InventoryUpdateEvent.EventType.CANCEL);
            assertNotNull(InventoryUpdateEvent.EventType.UPDATE);
            assertNotNull(InventoryUpdateEvent.EventType.RESTOCK);
        }
        
        @Test
        @DisplayName("Should have exactly 5 event types")
        void shouldHaveExactlyFiveEventTypes() {
            // When
            InventoryUpdateEvent.EventType[] eventTypes = InventoryUpdateEvent.EventType.values();
            
            // Then
            assertEquals(5, eventTypes.length);
        }
        
        @Test
        @DisplayName("Should support valueOf for all event types")
        void shouldSupportValueOfForAllEventTypes() {
            // Then
            assertEquals(InventoryUpdateEvent.EventType.RESERVE, 
                InventoryUpdateEvent.EventType.valueOf(EVENT_TYPE_RESERVE));
            assertEquals(InventoryUpdateEvent.EventType.COMMIT, 
                InventoryUpdateEvent.EventType.valueOf(EVENT_TYPE_COMMIT));
            assertEquals(InventoryUpdateEvent.EventType.CANCEL, 
                InventoryUpdateEvent.EventType.valueOf(EVENT_TYPE_CANCEL));
            assertEquals(InventoryUpdateEvent.EventType.UPDATE, 
                InventoryUpdateEvent.EventType.valueOf(EVENT_TYPE_UPDATE));
            assertEquals(InventoryUpdateEvent.EventType.RESTOCK, 
                InventoryUpdateEvent.EventType.valueOf(EVENT_TYPE_RESTOCK));
        }
        
        @Test
        @DisplayName("Should throw exception for invalid event type")
        void shouldThrowExceptionForInvalidEventType() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                InventoryUpdateEvent.EventType.valueOf("INVALID_TYPE"));
        }
        
        @Test
        @DisplayName("Should support event type name method")
        void shouldSupportEventTypeNameMethod() {
            // When & Then
            assertEquals(EVENT_TYPE_RESERVE, InventoryUpdateEvent.EventType.RESERVE.name());
            assertEquals(EVENT_TYPE_COMMIT, InventoryUpdateEvent.EventType.COMMIT.name());
            assertEquals(EVENT_TYPE_CANCEL, InventoryUpdateEvent.EventType.CANCEL.name());
            assertEquals(EVENT_TYPE_UPDATE, InventoryUpdateEvent.EventType.UPDATE.name());
            assertEquals(EVENT_TYPE_RESTOCK, InventoryUpdateEvent.EventType.RESTOCK.name());
        }
        
        @Test
        @DisplayName("Should support event type ordinal")
        void shouldSupportEventTypeOrdinal() {
            // When & Then
            assertTrue(InventoryUpdateEvent.EventType.RESERVE.ordinal() >= 0);
            assertTrue(InventoryUpdateEvent.EventType.COMMIT.ordinal() >= 0);
            assertTrue(InventoryUpdateEvent.EventType.CANCEL.ordinal() >= 0);
            assertTrue(InventoryUpdateEvent.EventType.UPDATE.ordinal() >= 0);
            assertTrue(InventoryUpdateEvent.EventType.RESTOCK.ordinal() >= 0);
        }
    }
    
    @Nested
    @DisplayName("Lombok Generated Methods Tests")
    class LombokGeneratedMethodsTests {
        
        @Test
        @DisplayName("Should test all getters and setters")
        void shouldTestAllGettersAndSetters() {
            // Given
            InventoryUpdateEvent event = new InventoryUpdateEvent();
            UUID eventId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.now();
            String details = "Test details";
            
            // When
            event.setEventId(eventId);
            event.setProductSku(PRODUCT_SKU);
            event.setStoreId(STORE_ID);
            event.setEventType(InventoryUpdateEvent.EventType.UPDATE);
            event.setPreviousQuantity(PREVIOUS_QUANTITY);
            event.setNewQuantity(NEW_QUANTITY);
            event.setReservedQuantity(RESERVED_QUANTITY);
            event.setTimestamp(timestamp);
            event.setDetails(details);
            
            // Then
            assertEquals(eventId, event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.UPDATE, event.getEventType());
            assertEquals(PREVIOUS_QUANTITY, event.getPreviousQuantity());
            assertEquals(NEW_QUANTITY, event.getNewQuantity());
            assertEquals(RESERVED_QUANTITY, event.getReservedQuantity());
            assertEquals(timestamp, event.getTimestamp());
            assertEquals(details, event.getDetails());
        }
        
        @Test
        @DisplayName("Should test builder pattern with chaining")
        void shouldTestBuilderPatternWithChaining() {
            // Given
            UUID eventId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.now();
            
            // When
            InventoryUpdateEvent event = InventoryUpdateEvent.builder()
                    .eventId(eventId)
                    .productSku(PRODUCT_SKU)
                    .storeId(STORE_ID)
                    .eventType(InventoryUpdateEvent.EventType.RESTOCK)
                    .previousQuantity(50)
                    .newQuantity(150)
                    .reservedQuantity(0)
                    .timestamp(timestamp)
                    .details("Restock operation")
                    .build();
            
            // Then
            assertEquals(eventId, event.getEventId());
            assertEquals(PRODUCT_SKU, event.getProductSku());
            assertEquals(STORE_ID, event.getStoreId());
            assertEquals(InventoryUpdateEvent.EventType.RESTOCK, event.getEventType());
            assertEquals(50, event.getPreviousQuantity());
            assertEquals(150, event.getNewQuantity());
            assertEquals(0, event.getReservedQuantity());
            assertEquals(timestamp, event.getTimestamp());
            assertEquals("Restock operation", event.getDetails());
        }
        
        @Test
        @DisplayName("Should create empty builder")
        void shouldCreateEmptyBuilder() {
            // When
            InventoryUpdateEvent.InventoryUpdateEventBuilder builder = InventoryUpdateEvent.builder();
            InventoryUpdateEvent event = builder.build();
            
            // Then
            assertNull(event.getEventId());
            assertNull(event.getProductSku());
            assertNull(event.getStoreId());
            assertNull(event.getEventType());
            assertNull(event.getPreviousQuantity());
            assertNull(event.getNewQuantity());
            assertNull(event.getReservedQuantity());
            assertNull(event.getTimestamp());
            assertNull(event.getDetails());
        }
    }
    
    @Nested
    @DisplayName("Object Behavior Tests")
    class ObjectBehaviorTests {
        
        @Test
        @DisplayName("Should support equals and hashCode")
        void shouldSupportEqualsAndHashCode() {
            // Given
            UUID eventId = UUID.randomUUID();
            LocalDateTime timestamp = LocalDateTime.now();
            
            InventoryUpdateEvent event1 = InventoryUpdateEvent.builder()
                    .eventId(eventId)
                    .productSku(PRODUCT_SKU)
                    .storeId(STORE_ID)
                    .eventType(InventoryUpdateEvent.EventType.RESERVE)
                    .timestamp(timestamp)
                    .build();
            
            InventoryUpdateEvent event2 = InventoryUpdateEvent.builder()
                    .eventId(eventId)
                    .productSku(PRODUCT_SKU)
                    .storeId(STORE_ID)
                    .eventType(InventoryUpdateEvent.EventType.RESERVE)
                    .timestamp(timestamp)
                    .build();
            
            InventoryUpdateEvent event3 = InventoryUpdateEvent.builder()
                    .eventId(UUID.randomUUID())
                    .productSku(PRODUCT_SKU)
                    .storeId(STORE_ID)
                    .eventType(InventoryUpdateEvent.EventType.COMMIT)
                    .timestamp(timestamp)
                    .build();
            
            // Then
            assertEquals(event1, event2);
            assertEquals(event1.hashCode(), event2.hashCode());
            assertNotEquals(event1, event3);
        }
        
        @Test
        @DisplayName("Should have meaningful toString")
        void shouldHaveMeaningfulToString() {
            // Given
            InventoryUpdateEvent event = InventoryUpdateEvent.createReserveEvent(
                PRODUCT_SKU, STORE_ID, PREVIOUS_QUANTITY, NEW_QUANTITY, RESERVED_QUANTITY);
            
            // When
            String toString = event.toString();
            
            // Then
            assertTrue(toString.contains(PRODUCT_SKU));
            assertTrue(toString.contains(STORE_ID));
            assertTrue(toString.contains(EVENT_TYPE_RESERVE));
        }
    }
}
