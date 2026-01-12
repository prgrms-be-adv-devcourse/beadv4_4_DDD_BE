package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentEventType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;

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
  @Column(name = "id")
  private Long id;

  @Column(name = "account_id")
  private Long accountId;

  @Column(name = "member_id")
  private Long memberId;

  @Column(name = "event_type", nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private PaymentEventType eventType;

  @Column(name = "amount")
  private long amount;

  @Column(name = "balance_before")
  private long balanceBefore;

  @Column(name = "balance_after")
  private long balanceAfter;

  @Column(name = "reference_id")
  private long referenceId;

  @Column(name = "reference_type")
  private String referenceType;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "created_by", nullable = false, updatable = false)
  @CreatedBy
  private Long createdBy;
}
