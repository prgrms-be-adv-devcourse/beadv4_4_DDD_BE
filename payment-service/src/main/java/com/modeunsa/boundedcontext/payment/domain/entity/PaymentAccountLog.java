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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    uniqueConstraints = {
      @UniqueConstraint(
          name = "ux_payment_account_log_account_ref_evt",
          columnNames = {"account_id", "reference_type", "reference_id", "event_type"})
    })
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAccountLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private PaymentAccount paymentAccount;

  @Column(nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private PaymentEventType eventType;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balanceBefore;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balanceAfter;

  private Long referenceId;

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
      BigDecimal amount,
      PaymentEventType paymentEventType,
      BigDecimal balanceBefore,
      BigDecimal balanceAfter,
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
