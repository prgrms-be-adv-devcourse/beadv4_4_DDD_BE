package com.modeunsa.boundedcontext.settlement.in;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
  private final JobOperator jobOperator;
  private final Job settlementCollectItemsAndCalculatePayoutsJob;

  // 매일 03:00 (KST) - 테스트 시 application.yml에서 override 가능
  @Scheduled(cron = "${settlement.scheduler.cron:0 0 3 * * *}", zone = "Asia/Seoul")
  public void runAt03()
      throws JobInstanceAlreadyCompleteException,
          InvalidJobParametersException,
          JobExecutionAlreadyRunningException,
          JobRestartException {
    runCollectItemsAndCalculatePayoutBatchJob();
  }

  private void runCollectItemsAndCalculatePayoutBatchJob()
      throws JobInstanceAlreadyCompleteException,
          InvalidJobParametersException,
          JobExecutionAlreadyRunningException,
          JobRestartException {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString(
                "runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .toJobParameters();

    JobExecution execution =
        jobOperator.start(settlementCollectItemsAndCalculatePayoutsJob, jobParameters);
  }
}
