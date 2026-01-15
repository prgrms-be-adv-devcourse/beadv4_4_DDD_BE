package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementCollectItemsStepConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final SettlementFacade settlementFacade;

  @Bean
  public Step collectItemsStep() {
    return new StepBuilder("collectItemsStep", jobRepository)
        .tasklet(collectItemsTasklet(), transactionManager)
        .build();
  }

  @Bean
  public Tasklet collectItemsTasklet() {
    return (contribution, chunkContext) -> {
      settlementFacade.collectSettlementItems();
      return RepeatStatus.FINISHED;
    };
  }
}
