package com.modeunsa.boundedcontext.settlement.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlement_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementItem extends GeneratedIdAndAuditedEntity {
  @ManyToOne(fetch = LAZY)
  private Settlement settlement;

  @Column(nullable = false)
  private Long orderItemId;

  @Column(nullable = false)
  private Long buyerUserId;

  @Column(nullable = false)
  private Long sellerUserId;

  private long amount;

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private SettlementEventType eventType;

  @Column(nullable = false)
  private LocalDateTime paymentAt;
}
