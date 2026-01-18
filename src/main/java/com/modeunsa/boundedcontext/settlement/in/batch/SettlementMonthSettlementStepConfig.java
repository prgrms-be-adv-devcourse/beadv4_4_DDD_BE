package com.modeunsa.boundedcontext.settlement.in.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementMonthSettlementStepConfig {
  private final JobRepository jobRepository;

  //  @Bean
  //  public Step monthSettlementStep() {
  //  TODO: step 작성
  //
  //   }
}
