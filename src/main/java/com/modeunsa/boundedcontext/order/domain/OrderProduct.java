package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_product")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderProduct extends GeneratedIdAndAuditedEntity {

  private Long sellerId;
  private String name;
  private String description;
  @Builder.Default private int salePrice = 0;
  @Builder.Default private int price = 0;
  @Builder.Default private int qty = 0;
}
