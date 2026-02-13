package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.types.TossWebhookStatus;
import com.modeunsa.global.jpa.entity.GeneratedIdAndAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
public class PaymentTossWebhookLog extends GeneratedIdAndAuditedEntity {

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
}
