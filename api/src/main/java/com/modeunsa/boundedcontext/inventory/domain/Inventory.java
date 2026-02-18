package com.modeunsa.boundedcontext.inventory.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "inventory_inventory",
    indexes = {
      // 판매자용
      @Index(name = "idx_inventory_seller_id", columnList = "seller_id")
    })
public class Inventory extends GeneratedIdAndAuditedEntity {

  @Column(name = "product_id", nullable = false, unique = true)
  private Long productId;

  @Column(name = "seller_id", nullable = false)
  private Long sellerId;

  // 실재고
  @Builder.Default
  @Column(name = "quantity", nullable = false)
  private int quantity = 0;

  // ----- 메서드 -----
  public boolean isOwner(Long requestSellerId) {
    if (requestSellerId == null || !this.sellerId.equals(requestSellerId)) {
      return false;
    }
    return true;
  }

  public int getInventoryQuantity() {
    return this.quantity;
  }
}
