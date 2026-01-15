package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementCalculatePayoutsStepConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementFacade settlementFacade;

  @Bean
  public Step calculatePayoutsStep() {
    return new StepBuilder("calculatePayoutsStep", jobRepository)
        .tasklet(calculatePayoutsTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet calculatePayoutsTasklet() {
    return ((contribution, chunkContext) -> {
      settlementFacade.calculatePayouts();
      return RepeatStatus.FINISHED;
    });
  }
}
