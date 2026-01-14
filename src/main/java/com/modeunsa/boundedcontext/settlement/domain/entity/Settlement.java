package com.modeunsa.boundedcontext.settlement.domain.entity;

import static jakarta.persistence.FetchType.LAZY;

import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_settlement")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Settlement extends GeneratedIdAndAuditedEntity {
  @Builder.Default private BigDecimal amount = BigDecimal.ZERO;

  @Column(nullable = false)
  private long sellerMemberId;

  @OneToMany(mappedBy = "settlement", cascade = CascadeType.PERSIST, fetch = LAZY)
  @Builder.Default
  private List<SettlementItem> items = new ArrayList<>();

  private LocalDateTime payoutAt;

  public SettlementItem addItem(
      long orderItemId,
      long buyerMemberId,
      long sellerMemberId,
      BigDecimal amount,
      SettlementEventType eventType,
      LocalDateTime paymentAt) {
    SettlementItem settlementItem =
        SettlementItem.builder()
            .settlement(this)
            .orderItemId(orderItemId)
            .buyerMemberId(buyerMemberId)
            .sellerMemberId(sellerMemberId)
            .amount(amount)
            .eventType(eventType)
            .paymentAt(paymentAt)
            .build();

    items.add(settlementItem);

    this.amount = this.amount.add(amount);

    return settlementItem;
  }
}
