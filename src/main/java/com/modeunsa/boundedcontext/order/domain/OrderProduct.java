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
}
