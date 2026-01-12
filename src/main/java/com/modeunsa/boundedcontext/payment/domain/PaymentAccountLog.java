package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table(name = "PAYMENT_ACCOUNT_LOG")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAccountLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private long accountId;

  private long memberId;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentEventType eventType;

  private long amount;

  private long balanceBefore;

  private long balanceAfter;

  private long referenceId;

  private String referenceType;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
