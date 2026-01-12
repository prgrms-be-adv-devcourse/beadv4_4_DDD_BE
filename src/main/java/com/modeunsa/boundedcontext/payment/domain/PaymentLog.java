package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentStatus;
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
@Table(name = "PAYMENT_LOG")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "member_id", nullable = false)
  private long memberId;

  @Column(name = "order_no", nullable = false)
  private String orderNo;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus beforeStatus;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus afterStatus;

  private String reason;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
