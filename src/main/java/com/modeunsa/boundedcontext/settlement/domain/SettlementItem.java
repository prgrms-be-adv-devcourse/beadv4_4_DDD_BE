package com.modeunsa.boundedcontext.settlement.domain;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "settlement_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementItem {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

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

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
