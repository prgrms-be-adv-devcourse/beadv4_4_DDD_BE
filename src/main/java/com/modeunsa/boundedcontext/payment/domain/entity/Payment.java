package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AuditedEntity {

  @EmbeddedId private PaymentId id;

  @Builder.Default
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus status = PaymentStatus.READY;

  private long amount;

  private String pgProvider;

  private String pgCustomerKey;

  private String pgOrderNo;

  private String pgCustomerName;

  private String pgCustomerEmail;

  @Lob private Integer pgRawResponse;

  @Lob private String pgFailureReason;
}
