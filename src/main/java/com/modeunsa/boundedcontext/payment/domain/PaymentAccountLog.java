package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long accountId;

  private Long memberId;

  @Column(nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private PaymentEventType eventType;

  private long amount;

  private long balanceBefore;

  private long balanceAfter;

  private long referenceId;

  private String referenceType;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(nullable = false, updatable = false)
  @CreatedBy
  private Long createdBy;
}
