package com.modeunsa.boundedcontext.order.domain;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_cart_item")
@SQLDelete(sql = "UPDATE order_cart_item SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class CartItem extends GeneratedIdAndAuditedEntity {

  @Column(name = "member_id")
  private long memberId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Positive
  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public void updateQuantity(int quantity) {
    this.quantity = quantity;
  }
}
