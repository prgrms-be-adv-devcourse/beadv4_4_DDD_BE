package com.modeunsa.boundedcontext.settlement.in;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
  private final JobOperator jobOperator;
  private final Job settlementCollectItemsAndCalculatePayoutsJob;

  // 매일 03:00 (KST)
  @Scheduled(cron = "${settlement.scheduler.cron-am3:}", zone = "Asia/Seoul")
  public void runAt03() {
    log.info("[SettlementScheduler] 정산 수집 배치 시작");

    try {
      JobExecution execution = runCollectItemsAndCalculatePayoutBatchJob();

      if (execution.getStatus().isUnsuccessful()) {
        log.error("[SettlementScheduler] 정산 수집 배치 실패: {}", execution.getAllFailureExceptions());
      } else {
        log.info("[SettlementScheduler] 정산 수집 배치 완료: {}", execution.getStatus());
      }
    } catch (Exception e) {
      log.error("[SettlementScheduler] 정산 수집 배치 실행 중 예외 발생: {}", e.getMessage(), e);
    }
  }

  private JobExecution runCollectItemsAndCalculatePayoutBatchJob() throws Exception {
    JobParameters jobParameters =
        new JobParametersBuilder()
            .addString(
                "runDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .toJobParameters();

    return jobOperator.start(settlementCollectItemsAndCalculatePayoutsJob, jobParameters);
  }
}
