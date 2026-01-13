package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_orderitem")
public class OrderItem extends GeneratedIdAndAuditedEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "seller_id", nullable = false)
  private Long sellerId;

  @Column(name = "product_name", nullable = false, length = 100)
  private String productName;

  @Positive
  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "sale_price", nullable = false)
  private BigDecimal salePrice; // 판매가

  @Column(name = "price", nullable = false)
  private BigDecimal price; // 정가
}
