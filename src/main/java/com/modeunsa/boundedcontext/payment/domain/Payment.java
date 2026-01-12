package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentStatus;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Entity
@Table(name = "payment_payment")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AuditedEntity {

  @EmbeddedId private PaymentId id;

  @Builder.Default
  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus status = PaymentStatus.READY;

  @Column(name = "amount")
  private long amount;

  @Column(name = "pg_provider")
  private String pgProvider;

  @Column(name = "pg_customer_key")
  private String pgCustomerKey;

  @Column(name = "pg_order_no")
  private String pgOrderNo;

  @Column(name = "pg_customer_name")
  private String pgCustomerName;

  @Column(name = "pg_customer_email")
  private String pgCustomerEmail;

  @Column(name = "pg_raw_response")
  @Lob
  private Integer pgRawResponse;

  @Column(name = "pg_failure_reason")
  @Lob
  private String pgFailureReason;
}
