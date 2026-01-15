package com.modeunsa.boundedcontext.settlement.in.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {
  private final JobRepository jobRepository;

  @Bean
  public Job settlementCollectItemsAndCalculatePayoutsJob(Step collectItemsStep) {
    return new JobBuilder("settlementCollectItemsAndCalculatePayoutsJob", jobRepository)
        .start(collectItemsStep)
        .build();
  }
}
