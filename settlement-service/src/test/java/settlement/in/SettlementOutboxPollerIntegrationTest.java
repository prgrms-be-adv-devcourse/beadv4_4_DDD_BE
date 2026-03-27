package settlement.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.SettlementApplication;
import com.modeunsa.boundedcontext.settlement.app.outbox.SettlementOutboxPoller;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    classes = SettlementApplication.class,
    properties = {
      "app.event-publisher.type=outbox",
      "outbox.poller.enabled=true",
      "spring.task.scheduling.enabled=false",
      "app.data-init.enabled=false",
    })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("SettlementOutboxPoller 통합 테스트")
class SettlementOutboxPollerIntegrationTest {

  @Autowired private JobOperator jobOperator;
  @Autowired private Job monthlySettlementJob;
  @Autowired private SettlementOutboxPoller settlementOutboxPoller;
  @Autowired private SettlementItemRepository settlementItemRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @MockitoBean private KafkaTemplate<String, Object> kafkaTemplate;

  private int settlementYear;
  private int settlementMonth;

  @BeforeEach
  void setUp() {
    jdbcTemplate.update("delete from settlement_outbox_event");
    settlementItemRepository.deleteAll();
    settlementRepository.deleteAll();

    settlementYear = 2026;
    settlementMonth = 2;
  }

  @Test
  @DisplayName("poller가 PENDING outbox 이벤트를 Kafka로 발행하고 SENT 상태로 변경한다")
  void pollerPublishesPendingOutboxEventAndMarksSent() throws Exception {
    when(kafkaTemplate.send(anyString(), anyString(), any()))
        .thenReturn(CompletableFuture.completedFuture(null));

    saveSettlement(
        2L,
        settlementYear,
        settlementMonth,
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
        "9000");
    saveSettlement(
        1L,
        settlementYear,
        settlementMonth,
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
        "1000");

    JobExecution jobExecution = launchMonthlySettlementJob(settlementYear, settlementMonth);

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    assertThat(findOutboxEvents()).hasSize(1);
    assertThat(findOutboxEvents().get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);

    settlementOutboxPoller.poll();

    ArgumentCaptor<DomainEventEnvelope> envelopeCaptor =
        ArgumentCaptor.forClass(DomainEventEnvelope.class);
    verify(kafkaTemplate)
        .send(
            org.mockito.Mockito.eq("settlement-events"),
            org.mockito.Mockito.eq("settlement-" + jobExecution.getId()),
            envelopeCaptor.capture());

    DomainEventEnvelope envelope = envelopeCaptor.getValue();
    assertThat(envelope.eventId())
        .isEqualTo("settlement-monthly-completed:" + jobExecution.getId());
    assertThat(envelope.eventType()).isEqualTo("SettlementCompletedPayoutEvent");
    assertThat(envelope.topic()).isEqualTo("settlement-events");

    List<SettlementOutboxEvent> outboxEvents = findOutboxEvents();
    assertThat(outboxEvents).hasSize(1);
    assertThat(outboxEvents.get(0).getStatus()).isEqualTo(OutboxStatus.SENT);
    assertThat(outboxEvents.get(0).getSentAt()).isNotNull();
  }

  private List<SettlementOutboxEvent> findOutboxEvents() {
    return jdbcTemplate.query(
        """
        select id, aggregate_type, aggregate_id, event_type, topic, payload, status, sent_at,
               retry_count, last_error_message, event_id, trace_id
        from settlement_outbox_event
        order by id asc
        """,
        (rs, rowNum) ->
            SettlementOutboxEvent.builder()
                .id(rs.getLong("id"))
                .aggregateType(rs.getString("aggregate_type"))
                .aggregateId(rs.getString("aggregate_id"))
                .eventType(rs.getString("event_type"))
                .topic(rs.getString("topic"))
                .payload(rs.getString("payload"))
                .status(OutboxStatus.valueOf(rs.getString("status")))
                .sentAt(
                    rs.getTimestamp("sent_at") != null
                        ? rs.getTimestamp("sent_at").toLocalDateTime()
                        : null)
                .retryCount(rs.getInt("retry_count"))
                .lastErrorMessage(rs.getString("last_error_message"))
                .eventId(rs.getString("event_id"))
                .traceId(rs.getString("trace_id"))
                .build());
  }

  private JobExecution launchMonthlySettlementJob(int year, int month) throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("runDateTime", LocalDateTime.now().toString())
            .addLong("settlementYear", (long) year)
            .addLong("settlementMonth", (long) month)
            .addString("settlementPeriod", "%d-%02d".formatted(year, month))
            .toJobParameters();
    return jobOperator.start(monthlySettlementJob, jobParameters);
  }

  private Settlement saveSettlement(
      Long sellerMemberId, int year, int month, SettlementEventType eventType, String amount) {
    Settlement settlement = Settlement.create(sellerMemberId, year, month, eventType);
    settlement.addItem(
        System.nanoTime(),
        100L,
        sellerMemberId,
        new BigDecimal(amount),
        eventType,
        LocalDateTime.now().minusDays(1));
    return settlementRepository.save(settlement);
  }
}
