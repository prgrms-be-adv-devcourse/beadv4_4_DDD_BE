package com.modeunsa.boundedcontext.settlement.domain.entity;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_candidate_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class SettlementCandidateItem extends GeneratedIdAndAuditedEntity {
  @Column(nullable = false)
  private Long orderItemId;

  @Column(nullable = false)
  private Long buyerMemberId;

  @Column(nullable = false)
  private Long sellerMemberId;

  @Builder.Default private BigDecimal amount = BigDecimal.ZERO;

  private LocalDateTime collectedAt;

  @Column(nullable = false)
  private LocalDateTime purchaseConfirmedAt;

  public void markCollected() {
    this.collectedAt = LocalDateTime.now();
  }

  public static SettlementCandidateItem create(
      Long orderItemId,
      Long buyerMemberId,
      Long sellerMemberId,
      BigDecimal amount,
      int count,
      LocalDateTime purchaseConfirmedAt) {
    BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(count));

    return SettlementCandidateItem.builder()
        .orderItemId(orderItemId)
        .buyerMemberId(buyerMemberId)
        .sellerMemberId(sellerMemberId)
        .amount(totalAmount)
        .purchaseConfirmedAt(purchaseConfirmedAt)
        .build();
  }
}
