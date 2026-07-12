package com.modeunsa.boundedcontext.member.domain.entity;

import com.modeunsa.global.jpa.converter.EncryptedStringConverter;
import com.modeunsa.global.jpa.entity.AuditedEntity;
import com.modeunsa.global.kafka.outbox.OutboxEventView;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "member_outbox_event",
    uniqueConstraints = {@UniqueConstraint(columnNames = "event_id")})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOutboxEvent extends AuditedEntity implements OutboxEventView {

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

  // 회원 이벤트 payload에는 이름/전화번호 등 PII가 포함되므로 엔티티 필드와 동일하게 저장 시 암호화한다
  @Lob
  @Convert(converter = EncryptedStringConverter.class)
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

  public static MemberOutboxEvent create(
      String aggregateType,
      String aggregateId,
      String eventType,
      String topic,
      String payload,
      String traceId) {
    return MemberOutboxEvent.builder()
        .aggregateType(aggregateType)
        .aggregateId(aggregateId)
        .eventType(eventType)
        .topic(topic)
        .payload(payload)
        .eventId(UUID.randomUUID().toString())
        .traceId(traceId)
        .build();
  }
}
