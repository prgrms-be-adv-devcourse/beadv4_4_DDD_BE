package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentEventType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table(name = "payment_account_log")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentAccountLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private long accountId;

  private long memberId;

  @Column(nullable = false, length = 100)
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
