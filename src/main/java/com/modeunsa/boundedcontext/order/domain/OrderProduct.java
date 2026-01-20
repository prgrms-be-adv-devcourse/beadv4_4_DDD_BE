package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.ManualIdAndAuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "order_product")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class OrderProduct extends ManualIdAndAuditedEntity {

  private Long sellerId;
  private String name;
  private String description;
  @Builder.Default private BigDecimal salePrice = BigDecimal.ZERO;
  @Builder.Default private BigDecimal price = BigDecimal.ZERO;
  @Builder.Default private int qty = 0;

  // 재고 검증
  public boolean isStockAvailable(int requestQuantity) {
    return this.qty >= requestQuantity;
  }

  // 재고 차감
  public void decreaseStock(int quantity) {
    if (isStockAvailable(quantity)) {
      this.qty = this.qty - quantity;
    }
  }
}
