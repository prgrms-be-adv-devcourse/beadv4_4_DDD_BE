package com.modeunsa.boundedcontext.inventory.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@Table(name = "inventory_product")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryProduct extends ManualIdAndAuditedEntity {
  @Column(name = "product_id", nullable = false, unique = true)
  private Long productId;

  @Column(name = "seller_id", nullable = false)
  private Long sellerId;

  @Column(length = 100, nullable = false)
  private String name;
}
