package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentOutboxStatus;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOutboxEvent extends AuditedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String aggregateType;

  @Column(nullable = false)
  private String aggregateId;

  @Column(nullable = false, length = 100)
  private String eventType;

  @Column(nullable = false, length = 100)
  private String topic;

  @Lob
  @Column(nullable = false)
  private String payload;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentOutboxStatus status = PaymentOutboxStatus.PENDING;

  private LocalDateTime sentAt;

  private int retryCount;

  private String lastErrorMessage;

  @Version private Long version;

  private static final int MAX_RETRY_COUNT = 5;

  public static PaymentOutboxEvent create(
      String aggregateType, String aggregateId, String eventType, String topic, String payload) {
    return PaymentOutboxEvent.builder()
        .aggregateType(aggregateType)
        .aggregateId(aggregateId)
        .eventType(eventType)
        .topic(topic)
        .payload(payload)
        .build();
  }

  public void markAsProcessing() {
    this.status = PaymentOutboxStatus.PROCESSING;
  }

  public void markAsSent() {
    this.status = PaymentOutboxStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  public void markAsFailed(String errorMessage) {
    this.lastErrorMessage = errorMessage;
    this.retryCount++;
    if (this.retryCount >= MAX_RETRY_COUNT) {
      this.status = PaymentOutboxStatus.FAILED;
    } else {
      this.status = PaymentOutboxStatus.PENDING;
    }
  }
}
