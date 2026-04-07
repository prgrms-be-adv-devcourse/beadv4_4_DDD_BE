package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementStatus;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.settlement.dto.SettlementCompletedPayoutDto;
import com.modeunsa.shared.settlement.event.SettlementCompletedPayoutEvent;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementMonthSettlementStepConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementRepository settlementRepository;
  private final EventPublisher eventPublisher;

  @Bean
  public Step reserveMonthlySettlementStep() {
    return new StepBuilder("reserveMonthlySettlementStep", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              Long batchExecutionId = contribution.getStepExecution().getJobExecution().getId();
              int settlementYear =
                  ((Long) chunkContext.getStepContext().getJobParameters().get("settlementYear"))
                      .intValue();
              int settlementMonth =
                  ((Long) chunkContext.getStepContext().getJobParameters().get("settlementMonth"))
                      .intValue();

              List<Settlement> settlements =
                  settlementRepository.findBySettlementYearAndSettlementMonthAndStatusOrderByIdAsc(
                      settlementYear, settlementMonth, SettlementStatus.PENDING);

              for (Settlement settlement : settlements) {
                settlement.markProcessing(batchExecutionId);
              }

              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  @Bean
  public Step completeMonthlySettlementStep() {
    return new StepBuilder("completeMonthlySettlementStep", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              Long batchExecutionId = contribution.getStepExecution().getJobExecution().getId();

              List<Settlement> settlements =
                  settlementRepository.findByBatchExecutionIdAndStatusOrderByIdAsc(
                      batchExecutionId, SettlementStatus.PROCESSING);

              if (settlements.isEmpty()) {
                return RepeatStatus.FINISHED;
              }

              List<SettlementCompletedPayoutDto> payouts = new ArrayList<>();
              for (Settlement settlement : settlements) {
                settlement.completePayout();
                payouts.add(
                    new SettlementCompletedPayoutDto(
                        settlement.getId(),
                        settlement.getSellerMemberId(),
                        settlement.getAmount(),
                        settlement.getType().getCompleteType(),
                        settlement.getPayoutAt()));
              }

              eventPublisher.publish(SettlementCompletedPayoutEvent.of(batchExecutionId, payouts));

              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }
}
