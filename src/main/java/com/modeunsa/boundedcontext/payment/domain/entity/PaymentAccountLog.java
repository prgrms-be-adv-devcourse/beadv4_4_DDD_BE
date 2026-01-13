package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAccountLog {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private PaymentAccount paymentAccount;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private PaymentEventType eventType;

  private long amount;

  private long balanceBefore;

  private long balanceAfter;

  private long referenceId;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private ReferenceType referenceType;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(updatable = false)
  @CreatedBy
  private Long createdBy;

  public static PaymentAccountLog addAccountLog(
      PaymentAccount paymentAccount,
      long amount,
      PaymentEventType paymentEventType,
      long balanceBefore,
      long balanceAfter,
      Long relId,
      ReferenceType referenceType) {
    return PaymentAccountLog.builder()
        .paymentAccount(paymentAccount)
        .eventType(paymentEventType)
        .amount(amount)
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .referenceId(relId)
        .referenceType(referenceType)
        .build();
  }
}
