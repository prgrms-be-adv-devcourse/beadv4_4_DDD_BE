package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.global.jpa.entity.AuditedEntity;
import com.modeunsa.global.kafka.outbox.OutboxEventView;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
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
public class PaymentOutboxEvent extends AuditedEntity implements OutboxEventView {

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
  private OutboxStatus status = OutboxStatus.PENDING;

  private LocalDateTime sentAt;

  private int retryCount;

  private String lastErrorMessage;

  private String eventId;

  private String traceId;

  @Version private Long version;

  public static PaymentOutboxEvent create(
      String aggregateType,
      String aggregateId,
      String eventType,
      String topic,
      String payload,
      String traceId) {
    return PaymentOutboxEvent.builder()
        .aggregateType(aggregateType)
        .aggregateId(aggregateId)
        .eventType(eventType)
        .topic(topic)
        .payload(payload)
        .traceId(traceId)
        .build();
  }
}
