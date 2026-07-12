package com.modeunsa.boundedcontext.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.MemberApplication;
import com.modeunsa.boundedcontext.member.app.outbox.MemberOutboxPoller;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
    classes = MemberApplication.class,
    properties = {
      "app.event-publisher.type=outbox",
      "outbox.poller.enabled=true",
      "spring.task.scheduling.enabled=false",
      "app.data-init.enabled=false",
    })
@ActiveProfiles("test")
@DisplayName("Member Outbox 통합 테스트")
class MemberOutboxIntegrationTest {

  @Autowired private EventPublisher eventPublisher;
  @Autowired private MemberOutboxPoller memberOutboxPoller;
  @Autowired private TransactionTemplate transactionTemplate;
  @Autowired private JdbcTemplate jdbcTemplate;

  @MockitoBean private KafkaTemplate<String, Object> kafkaTemplate;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("delete from member_outbox_event");
  }

  @Test
  @DisplayName("트랜잭션 커밋 시 outbox에 PENDING으로 저장되고, poller가 Kafka로 발행 후 SENT로 변경한다")
  void commitSavesPendingEventAndPollerPublishesIt() {
    when(kafkaTemplate.send(anyString(), anyString(), any()))
        .thenReturn(CompletableFuture.completedFuture(null));

    transactionTemplate.executeWithoutResult(tx -> eventPublisher.publish(signupEvent()));

    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select aggregate_type, aggregate_id, event_type, topic, status"
                + " from member_outbox_event");
    assertThat(row.get("status")).isEqualTo(OutboxStatus.PENDING.name());
    assertThat(row.get("topic")).isEqualTo("member-events");
    assertThat(row.get("event_type")).isEqualTo("MemberSignupEvent");
    assertThat(row.get("aggregate_type")).isEqualTo("Member");
    assertThat(row.get("aggregate_id")).isEqualTo("member-1");

    memberOutboxPoller.poll();

    ArgumentCaptor<DomainEventEnvelope> envelopeCaptor =
        ArgumentCaptor.forClass(DomainEventEnvelope.class);
    verify(kafkaTemplate).send(eq("member-events"), eq("member-1"), envelopeCaptor.capture());

    DomainEventEnvelope envelope = envelopeCaptor.getValue();
    assertThat(envelope.eventType()).isEqualTo("MemberSignupEvent");
    assertThat(envelope.topic()).isEqualTo("member-events");

    Map<String, Object> sentRow =
        jdbcTemplate.queryForMap("select status, sent_at from member_outbox_event");
    assertThat(sentRow.get("status")).isEqualTo(OutboxStatus.SENT.name());
    assertThat(sentRow.get("sent_at")).isNotNull();
  }

  @Test
  @DisplayName("트랜잭션 롤백 시 outbox에 이벤트가 저장되지 않는다")
  void rollbackDoesNotSaveOutboxEvent() {
    transactionTemplate.executeWithoutResult(
        tx -> {
          eventPublisher.publish(signupEvent());
          tx.setRollbackOnly();
        });

    Integer count =
        jdbcTemplate.queryForObject("select count(*) from member_outbox_event", Integer.class);
    assertThat(count).isZero();
  }

  private MemberSignupEvent signupEvent() {
    return new MemberSignupEvent(1L, "홍길동", "test@modeunsa.com", "010-1234-5678", "USER", "ACTIVE");
  }
}
