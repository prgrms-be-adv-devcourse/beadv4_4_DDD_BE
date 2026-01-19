package com.modeunsa.boundedcontext.payment.domain.entity;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_NOT_FOUND;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.global.exception.GeneralException;
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
import jakarta.persistence.JoinColumns;
import jakarta.persistence.Lob;
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

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
    @JoinColumn(name = "member_id", referencedColumnName = "member_id", nullable = false),
    @JoinColumn(name = "order_no", referencedColumnName = "order_no", nullable = false)
  })
  private Payment payment;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus beforeStatus;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus afterStatus;

  @Lob private String reason;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(updatable = false)
  @CreatedBy
  private Long createdBy;

  public static PaymentLog addLog(
      Payment payment, PaymentStatus beforeStatus, PaymentStatus afterStatus) {
    if (beforeStatus == null) {
      throw new GeneralException(PAYMENT_NOT_FOUND);
    }
    return PaymentLog.builder()
        .payment(payment)
        .beforeStatus(beforeStatus)
        .afterStatus(afterStatus)
        .build();
  }

  public static PaymentLog addInitialLog(Payment payment, PaymentStatus afterStatus) {
    return PaymentLog.builder().payment(payment).afterStatus(afterStatus).build();
  }
}
