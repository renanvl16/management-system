package com.inventory.management.central.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para eventos de inventário.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "inventory_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEventJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @PrePersist
    void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
    
    @Column(name = "product_sku")
    private String productSku;
    
    @Column(name = "store_id")
    private String storeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "event_data")
    private String eventData;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProcessingStatus status;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    public enum EventType {
        RESERVE,
        COMMIT,
        CANCEL,
        UPDATE,
        RESTOCK
    }
    
    public enum ProcessingStatus {
        PENDING,
        PROCESSED,
        FAILED,
        IGNORED
    }
}
