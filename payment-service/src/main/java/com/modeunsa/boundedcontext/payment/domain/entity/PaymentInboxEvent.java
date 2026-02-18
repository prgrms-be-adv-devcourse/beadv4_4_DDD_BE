package com.modeunsa.boundedcontext.payment.domain.entity;

import com.modeunsa.global.jpa.entity.AuditedEntity;
import com.modeunsa.global.kafka.inbox.InboxEventView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "payment_inbox_event",
    uniqueConstraints = {@UniqueConstraint(columnNames = "event_id")})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentInboxEvent extends AuditedEntity implements InboxEventView {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String eventId;

  private String topic;

  @Lob private String payload;

  private String traceId;

  private LocalDateTime receivedAt;

  public static PaymentInboxEvent create(
      String eventId, String topic, String payload, String traceId) {
    return PaymentInboxEvent.builder()
        .eventId(eventId)
        .topic(topic)
        .payload(payload)
        .traceId(traceId)
        .receivedAt(LocalDateTime.now())
        .build();
  }
}
