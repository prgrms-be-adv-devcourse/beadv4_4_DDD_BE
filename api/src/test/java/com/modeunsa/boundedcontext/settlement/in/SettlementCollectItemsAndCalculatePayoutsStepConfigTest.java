package com.modeunsa.boundedcontext.settlement.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementItem;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("SettlementCollectItemsAndCalculatePayoutsStepConfig 통합 테스트")
class SettlementCollectItemsAndCalculatePayoutsStepConfigTest {

  @Autowired private JobOperator jobOperator;
  @Autowired private Job collectItemsAndCalculatePayoutsJob;

  @MockitoBean private SettlementFacade settlementFacade;

  private List<SettlementItem> testItems;
  private SettlementCandidateItem candidateItem;

  @BeforeEach
  void setUp() {
    int year = LocalDateTime.now().getYear();
    int month = LocalDateTime.now().getMonthValue();

    Settlement settlement =
        Settlement.create(1L, year, month, SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT);
    testItems =
        List.of(
            settlement.addItem(
                1L,
                100L,
                1L,
                new BigDecimal("9000"),
                SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT,
                LocalDateTime.now()),
            settlement.addItem(
                1L,
                100L,
                0L,
                new BigDecimal("1000"),
                SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE,
                LocalDateTime.now()));

    candidateItem =
        SettlementCandidateItem.create(
            1L, 100L, 1L, new BigDecimal("10000"), 1, LocalDateTime.now().minusDays(1));
  }

  private JobExecution launchJob() throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString("runId", UUID.randomUUID().toString())
            .toJobParameters();
    return jobOperator.start(collectItemsAndCalculatePayoutsJob, jobParameters);
  }

  @Test
  @DisplayName("배치 Job 실행 성공")
  void job_executes_successfully() throws Exception {
    // given
    Page<SettlementCandidateItem> firstPage = new PageImpl<>(List.of(candidateItem));
    Page<SettlementCandidateItem> emptyPage = new PageImpl<>(List.of());

    when(settlementFacade.getSettlementCandidateItems(any(), any(), any(Pageable.class)))
        .thenReturn(firstPage)
        .thenReturn(emptyPage);
    when(settlementFacade.addItemsAndCalculatePayouts(any())).thenReturn(testItems);

    // when
    JobExecution jobExecution = launchJob();

    // then
    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
  }

  @Test
  @DisplayName("Step 실행 시 Facade의 addItemsAndCalculatePayouts 호출")
  void step_calls_facade_addItemsAndCalculatePayouts() throws Exception {
    // given
    Page<SettlementCandidateItem> firstPage = new PageImpl<>(List.of(candidateItem));
    Page<SettlementCandidateItem> emptyPage = new PageImpl<>(List.of());

    when(settlementFacade.getSettlementCandidateItems(any(), any(), any(Pageable.class)))
        .thenReturn(firstPage)
        .thenReturn(emptyPage);
    when(settlementFacade.addItemsAndCalculatePayouts(any())).thenReturn(testItems);

    // when
    launchJob();

    // then
    verify(settlementFacade, atLeastOnce()).addItemsAndCalculatePayouts(any());
  }

  @Test
  @DisplayName("Step 실행 시 Facade의 saveItems 호출")
  void step_calls_facade_saveItems() throws Exception {
    // given
    Page<SettlementCandidateItem> firstPage = new PageImpl<>(List.of(candidateItem));
    Page<SettlementCandidateItem> emptyPage = new PageImpl<>(List.of());

    when(settlementFacade.getSettlementCandidateItems(any(), any(), any(Pageable.class)))
        .thenReturn(firstPage)
        .thenReturn(emptyPage);
    when(settlementFacade.addItemsAndCalculatePayouts(any())).thenReturn(testItems);

    // when
    launchJob();

    // then
    verify(settlementFacade, atLeastOnce()).saveItems(anyList());
  }

  @Test
  @DisplayName("빈 주문 목록일 때 정상 종료")
  void step_completes_when_noOrders() throws Exception {
    // given
    Page<SettlementCandidateItem> emptyPage = new PageImpl<>(List.of());
    when(settlementFacade.getSettlementCandidateItems(any(), any(), any(Pageable.class)))
        .thenReturn(emptyPage);

    // when
    JobExecution jobExecution = launchJob();

    // then
    assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
  }
}
