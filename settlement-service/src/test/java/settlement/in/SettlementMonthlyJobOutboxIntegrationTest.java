package settlement.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.SettlementApplication;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementOutboxEvent;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = SettlementApplication.class,
    properties = {
      "app.event-publisher.type=outbox",
      "outbox.poller.enabled=false",
      "spring.task.scheduling.enabled=false",
      "app.data-init.enabled=false",
    })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("monthlySettlementJob outbox 통합 테스트")
class SettlementMonthlyJobOutboxIntegrationTest {

  @Autowired private JobOperator jobOperator;
  @Autowired private Job monthlySettlementJob;
  @Autowired private SettlementItemRepository settlementItemRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private EntityManager entityManager;
  @Autowired private JdbcTemplate jdbcTemplate;

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
  @DisplayName("월 정산 완료 이벤트를 outbox에 멱등키와 함께 저장한다")
  void monthlySettlementJobStoresCompletedEventInOutbox() throws Exception {
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

    List<SettlementOutboxEvent> outboxEvents =
        entityManager
            .createQuery(
                "select e from SettlementOutboxEvent e order by e.id asc",
                SettlementOutboxEvent.class)
            .getResultList();

    assertThat(outboxEvents).hasSize(1);
    assertThat(outboxEvents.get(0).getEventType()).isEqualTo("SettlementCompletedPayoutEvent");
    assertThat(outboxEvents.get(0).getTopic()).isEqualTo("settlement-events");
    assertThat(outboxEvents.get(0).getEventId())
        .isEqualTo("settlement-monthly-completed:" + jobExecution.getId());
    assertThat(outboxEvents.get(0).getAggregateId())
        .isEqualTo("settlement-" + jobExecution.getId());
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
