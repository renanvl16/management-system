
package com.inventory.management.central.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = true)
public class CentralInventory {
	@EqualsAndHashCode.Include
	private String productSku;
	private String productName;
	private String description;
	private String category;
	private Double unitPrice;
	private Integer totalQuantity;
	private Integer totalReservedQuantity;
	private Integer availableQuantity;
	private LocalDateTime lastUpdated;
	private Long version;
	private Boolean active;

	public void calculateAvailableQuantity() {
		this.availableQuantity = this.totalQuantity - this.totalReservedQuantity;
	}

	public boolean hasAvailableStock() {
		return availableQuantity != null && availableQuantity > 0;
	}

	public boolean hasStockAvailable(Integer requestedQuantity) {
		return availableQuantity != null && requestedQuantity != null && availableQuantity >= requestedQuantity;
	}

	public static CentralInventory create(String productSku, String productName) {
		return CentralInventory.builder()
				.productSku(productSku)
				.productName(productName)
				.totalQuantity(0)
				.totalReservedQuantity(0)
				.availableQuantity(0)
				.lastUpdated(LocalDateTime.now())
				.active(true)
				.build();
	}

	public void setTotalReservedQuantity(Integer reserved) {
		this.totalReservedQuantity = reserved;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
