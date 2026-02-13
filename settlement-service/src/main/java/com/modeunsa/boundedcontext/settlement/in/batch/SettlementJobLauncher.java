package com.modeunsa.boundedcontext.settlement.in.batch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementJobLauncher {
  private final JobOperator jobOperator;
  private final Job collectItemsAndCalculatePayoutsJob;
  private final Job monthlySettlementJob;

  public JobExecution runCollectItemsAndCalculatePayoutsJob() throws Exception {
    return runJob(collectItemsAndCalculatePayoutsJob);
  }

  public JobExecution runMonthlyPayoutJob() throws Exception {
    return runJob(monthlySettlementJob);
  }

  private JobExecution runJob(Job job) throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString(
                "runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .toJobParameters();

    return jobOperator.start(job, jobParameters);
  }
}
