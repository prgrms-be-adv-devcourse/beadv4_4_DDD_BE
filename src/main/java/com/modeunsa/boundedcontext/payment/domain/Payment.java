package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

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
public class Payment {

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

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Column(name = "created_by", updatable = false)
  @CreatedBy
  private Long createdBy;

  @Column(name = "updated_by")
  @LastModifiedBy
  private Long updatedBy;
}
