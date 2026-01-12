package com.modeunsa.boundedcontext.payment.domain;

import com.modeunsa.boundedcontext.payment.domain.type.PaymentStatus;
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
@Table(name = "payment_log")
@Getter
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

  @CreationTimestamp private LocalDateTime createdAt;

  @CreatedBy private Long createdBy;
}
