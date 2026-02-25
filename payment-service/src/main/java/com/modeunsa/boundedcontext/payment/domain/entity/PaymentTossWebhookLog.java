package com.modeunsa.boundedcontext.payment.domain.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import com.modeunsa.boundedcontext.payment.domain.types.TossWebhookStatus;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_toss_webhook_log")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTossWebhookLog extends AuditedEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String transmissionId;

  private OffsetDateTime transmissionTime;

  private int retryCount;

  private String eventType;

  @Builder.Default
  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TossWebhookStatus status = TossWebhookStatus.PENDING;

  @Lob private String payload;

  @Lob private String failureReason;

  private LocalDateTime succeededAt;

  private LocalDateTime failedAt;

  public static PaymentTossWebhookLog create(
      String transmissionId,
      OffsetDateTime transmissionTime,
      int retryCount,
      String eventType,
      String payload) {
    return PaymentTossWebhookLog.builder()
        .transmissionId(transmissionId)
        .transmissionTime(transmissionTime)
        .retryCount(retryCount)
        .eventType(eventType)
        .payload(payload)
        .build();
  }

  public void markSuccess() {
    this.status = TossWebhookStatus.SUCCESS;
    this.succeededAt = LocalDateTime.now();
  }

  public void markFailed(String message) {
    this.status = TossWebhookStatus.FAILED;
    this.failureReason = message;
    this.failedAt = LocalDateTime.now();
  }

  public void update(int retryCount, OffsetDateTime transmissionTime, String payload) {
    this.retryCount = retryCount;
    this.transmissionTime = transmissionTime;
    this.payload = payload;
  }
}
