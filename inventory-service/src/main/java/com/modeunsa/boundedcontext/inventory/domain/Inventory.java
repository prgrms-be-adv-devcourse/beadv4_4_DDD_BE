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

  // 초기화 여부 확인하는 필드
  @Column(name = "initialized", nullable = false)
  private boolean initialized = false;

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

  public void initializeQuantity(int quantity) {
    // TODO: 도메인에러로 변경
    if (this.initialized) {
      throw new IllegalStateException("이미 초기화된 재고입니다. 수량 변경은 입고/출고 API를 이용해주세요.");
    }

    if (quantity < 1) {
      throw new IllegalArgumentException("초기 재고는 1 이상이어야 합니다.");
    }

    this.quantity = quantity;
    this.initialized = true;
  }
}
