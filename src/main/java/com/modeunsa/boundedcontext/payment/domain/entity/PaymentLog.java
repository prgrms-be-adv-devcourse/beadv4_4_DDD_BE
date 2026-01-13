package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class PaymentLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String orderNo;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus beforeStatus;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private PaymentStatus afterStatus;

  @Lob private String reason;

  @Column(nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Column(nullable = false, updatable = false)
  @CreatedBy
  private Long createdBy;
}
