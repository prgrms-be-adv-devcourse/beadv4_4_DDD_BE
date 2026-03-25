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
  private final SettlementMonthlyJobExecutionListener settlementMonthlyJobExecutionListener;

  @Bean
  public Job collectItemsAndCalculatePayoutsJob(Step collectItemsAndCalculatePayoutsStep) {
    return new JobBuilder("collectItemsAndCalculatePayoutsJob", jobRepository)
        .start(collectItemsAndCalculatePayoutsStep)
        .build();
  }

  @Bean
  public Job monthlySettlementJob(
      Step reserveMonthlySettlementStep,
      Step monthlySettlementStep,
      Step completeMonthlySettlementStep) {
    return new JobBuilder("monthlySettlementJob", jobRepository)
        .listener(settlementMonthlyJobExecutionListener)
        .start(reserveMonthlySettlementStep)
        .next(monthlySettlementStep)
        .next(completeMonthlySettlementStep)
        .build();
  }
}
