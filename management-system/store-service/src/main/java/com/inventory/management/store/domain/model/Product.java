package com.inventory.management.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um produto no inventário da loja.
 * Esta é a entidade central do domínio que contém as informações
 * e regras de negócio relacionadas aos produtos.
 * 
 * @author Sistema de Gerenciamento de Inventário
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
public class Product {
    
    /**
     * Identificador único do produto.
     * Usado como chave primária no banco de dados.
     */
    @EqualsAndHashCode.Include
    private UUID id;
    
    /**
     * SKU (Stock Keeping Unit) do produto.
     * Código único identificador comercial.
     */
    private String sku;
    
    /**
     * Nome comercial do produto.
     */
    private String name;
    
    /**
     * Descrição detalhada do produto.
     */
    private String description;
    
    /**
     * Preço unitário do produto.
     */
    private BigDecimal price;
    
    /**
     * Quantidade disponível em estoque.
     * Representa o estoque físico disponível.
     */
    private Integer quantity;
    
    /**
     * Quantidade reservada para checkouts pendentes.
     * Produtos reservados não estão disponíveis para novas reservas.
     */
    private Integer reservedQuantity;
    
    /**
     * Identificador da loja proprietária do produto.
     */
    private String storeId;
    
    /**
     * Indica se o produto está ativo no catálogo.
     */
    private Boolean active;
    
    /**
     * Data/hora da última atualização.
     */
    private LocalDateTime updatedAt;
    
    /**
     * Calcula a quantidade disponível (não reservada).
     * 
     * @return quantidade disponível 
     */
    public Integer getAvailableQuantity() {
        return quantity;
    }
    
    /**
     * Verifica se há estoque disponível para uma quantidade específica.
     * 
     * @param requestedQuantity quantidade solicitada
     * @return true se há estoque suficiente, false caso contrário
     */
    public boolean hasAvailableQuantity(Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }
        return getAvailableQuantity() >= requestedQuantity;
    }
    
    /**
     * Reserva uma quantidade do produto para checkout.
     * Reduz a quantidade disponível e aumenta a quantidade reservada.
     * 
     * @param quantityToReserve quantidade a ser reservada
     * @throws IllegalArgumentException se não houver estoque suficiente
     */
    public void reserveQuantity(Integer quantityToReserve) {
        if (quantityToReserve == null || quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantidade a reservar deve ser maior que zero");
        }
        
        if (!hasAvailableQuantity(quantityToReserve)) {
            throw new IllegalArgumentException(
                String.format("Quantidade insuficiente em estoque. Disponível: %d, Solicitado: %d", 
                    getAvailableQuantity(), quantityToReserve)
            );
        }
        
        this.quantity -= quantityToReserve; // Remove do estoque disponível
        this.reservedQuantity = (this.reservedQuantity != null ? this.reservedQuantity : 0) + quantityToReserve;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Cancela uma reserva, devolvendo a quantidade ao estoque disponível.
     * 
     * @param quantityToCancel quantidade da reserva a cancelar
     * @throws IllegalArgumentException se não houver quantidade reservada suficiente
     */
    public void cancelReservation(Integer quantityToCancel) {
        if (quantityToCancel == null || quantityToCancel <= 0) {
            throw new IllegalArgumentException("Quantidade a cancelar deve ser maior que zero");
        }
        
        Integer currentReserved = this.reservedQuantity != null ? this.reservedQuantity : 0;
        if (currentReserved < quantityToCancel) {
            throw new IllegalArgumentException(
                String.format("Quantidade reservada insuficiente. Reservado: %d, Tentando cancelar: %d", 
                    currentReserved, quantityToCancel)
            );
        }
        
        this.quantity += quantityToCancel; // Retorna as unidades canceladas para o estoque disponível
        this.reservedQuantity = currentReserved - quantityToCancel;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Confirma uma venda, removendo definitivamente a quantidade reservada.
     * 
     * @param quantityToCommit quantidade da reserva a confirmar
     * @throws IllegalArgumentException se não houver quantidade reservada suficiente
     */
    public void commitReservation(Integer quantityToCommit) {
        if (quantityToCommit == null || quantityToCommit <= 0) {
            throw new IllegalArgumentException("Quantidade a confirmar deve ser maior que zero");
        }
        
        Integer currentReserved = this.reservedQuantity != null ? this.reservedQuantity : 0;
        if (currentReserved < quantityToCommit) {
            throw new IllegalArgumentException(
                String.format("Quantidade reservada insuficiente. Reservado: %d, Tentando confirmar: %d", 
                    currentReserved, quantityToCommit)
            );
        }
        
        this.reservedQuantity = currentReserved - quantityToCommit;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Atualiza a quantidade em estoque.
     * 
     * @param newQuantity nova quantidade
     * @throws IllegalArgumentException se a quantidade for negativa
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior ou igual a zero");
        }
        
        this.quantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica se o produto está ativo e disponível para operações.
     * 
     * @return true se ativo, false caso contrário
     */
    public boolean isActive() {
        return active != null && active;
    }
    
    /**
     * Ativa o produto.
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Desativa o produto.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}
