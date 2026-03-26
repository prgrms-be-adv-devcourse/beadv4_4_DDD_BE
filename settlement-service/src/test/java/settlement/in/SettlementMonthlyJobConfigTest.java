package settlement.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modeunsa.SettlementApplication;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementStatus;
import com.modeunsa.boundedcontext.settlement.out.SettlementItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = SettlementApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("monthlySettlementJob 통합 테스트")
class SettlementMonthlyJobConfigTest {

  @Autowired private JobOperator jobOperator;
  @Autowired private Job monthlySettlementJob;
  @Autowired private SettlementItemRepository settlementItemRepository;
  @Autowired private SettlementRepository settlementRepository;

  @MockitoBean private EventPublisher eventPublisher;

  private int settlementYear;
  private int settlementMonth;

  @BeforeEach
  void setUp() {
    settlementItemRepository.deleteAll();
    settlementRepository.deleteAll();
    clearInvocations(eventPublisher);

    settlementYear = 2026;
    settlementMonth = 2;
  }

  @Test
  @DisplayName("대상 월의 PENDING 정산만 완료 처리하고 완료 이벤트를 발행한다")
  void monthlySettlementJobCompletesPendingSettlementsAndPublishesEvent() throws Exception {
    final Settlement targetAmountSettlement =
        saveSettlement(
            2L,
            settlementYear,
            settlementMonth,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            "9000");
    final Settlement targetFeeSettlement =
        saveSettlement(
            1L,
            settlementYear,
            settlementMonth,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            "1000");
    Settlement alreadyCompletedSettlement =
        saveSettlement(
            3L,
            settlementYear,
            settlementMonth,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            "5000");
    alreadyCompletedSettlement.completePayout();
    settlementRepository.save(alreadyCompletedSettlement);
    saveSettlement(
        4L,
        settlementYear,
        settlementMonth - 1,
        SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
        "7000");

    JobExecution jobExecution = launchMonthlySettlementJob(settlementYear, settlementMonth);

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

    List<Settlement> completedSettlements =
        settlementRepository.findAll().stream()
            .sorted(Comparator.comparing(Settlement::getId))
            .toList();

    Settlement completedAmountSettlement =
        findById(completedSettlements, targetAmountSettlement.getId());
    Settlement completedFeeSettlement = findById(completedSettlements, targetFeeSettlement.getId());
    Settlement unchangedCompletedSettlement =
        findById(completedSettlements, alreadyCompletedSettlement.getId());

    assertThat(completedAmountSettlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
    assertThat(completedAmountSettlement.getBatchExecutionId()).isEqualTo(jobExecution.getId());
    assertThat(completedAmountSettlement.getPayoutAt()).isNotNull();
    assertThat(completedFeeSettlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
    assertThat(completedFeeSettlement.getBatchExecutionId()).isEqualTo(jobExecution.getId());
    assertThat(completedFeeSettlement.getPayoutAt()).isNotNull();
    assertThat(unchangedCompletedSettlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);

    ArgumentCaptor<SettlementCompletedPayoutEvent> eventCaptor =
        ArgumentCaptor.forClass(SettlementCompletedPayoutEvent.class);
    verify(eventPublisher, times(1)).publish(eventCaptor.capture());

    SettlementCompletedPayoutEvent publishedEvent = eventCaptor.getValue();
    assertThat(publishedEvent.batchId()).isEqualTo(String.valueOf(jobExecution.getId()));
    assertThat(publishedEvent.eventId())
        .isEqualTo("settlement-monthly-completed:" + jobExecution.getId());
    assertThat(publishedEvent.payouts()).hasSize(2);
    assertThat(publishedEvent.payouts())
        .extracting("settlementId")
        .containsExactlyInAnyOrder(targetAmountSettlement.getId(), targetFeeSettlement.getId());
    assertThat(publishedEvent.payouts())
        .extracting("payoutAt")
        .allMatch(payoutAt -> payoutAt != null);
  }

  @Test
  @DisplayName("완료 단계에서 실패하면 PROCESSING 정산건을 다시 PENDING 으로 롤백한다")
  void monthlySettlementJobRollsBackProcessingSettlementsWhenCompletionFails() throws Exception {
    final Settlement targetAmountSettlement =
        saveSettlement(
            2L,
            settlementYear,
            settlementMonth,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
            "9000");
    final Settlement targetFeeSettlement =
        saveSettlement(
            1L,
            settlementYear,
            settlementMonth,
            SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
            "1000");

    doThrow(new RuntimeException("publish failed")).when(eventPublisher).publish(any());

    JobExecution jobExecution = launchMonthlySettlementJob(settlementYear, settlementMonth);

    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("FAILED");

    Settlement rolledBackAmountSettlement =
        findById(settlementRepository.findAll(), targetAmountSettlement.getId());
    Settlement rolledBackFeeSettlement =
        findById(settlementRepository.findAll(), targetFeeSettlement.getId());

    assertThat(rolledBackAmountSettlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
    assertThat(rolledBackAmountSettlement.getBatchExecutionId()).isNull();
    assertThat(rolledBackAmountSettlement.getProcessingAt()).isNull();
    assertThat(rolledBackAmountSettlement.getPayoutAt()).isNull();
    assertThat(rolledBackFeeSettlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
    assertThat(rolledBackFeeSettlement.getBatchExecutionId()).isNull();
    assertThat(rolledBackFeeSettlement.getProcessingAt()).isNull();
    assertThat(rolledBackFeeSettlement.getPayoutAt()).isNull();
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

  private Settlement findById(List<Settlement> settlements, Long settlementId) {
    return settlements.stream()
        .filter(settlement -> settlement.getId().equals(settlementId))
        .findFirst()
        .orElseThrow();
  }
}
