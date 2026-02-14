package com.modeunsa.boundedcontext.settlement.in.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
  private final SettlementJobLauncher settlementJobLauncher;

  // 매일 03:00 (KST)
  @Scheduled(cron = "${settlement.scheduler.daily-03:}", zone = "Asia/Seoul")
  public void runAt03() {
    log.debug("[SettlementScheduler] 정산 수집 배치 시작");

    try {
      JobExecution execution = settlementJobLauncher.runCollectItemsAndCalculatePayoutsJob();

      if (execution.getStatus().isUnsuccessful()) {
        log.error("[SettlementScheduler] 정산 수집 배치 실패: {}", execution.getAllFailureExceptions());
      } else {
        log.debug("[SettlementScheduler] 정산 수집 배치 완료: {}", execution.getStatus());
      }
    } catch (Exception e) {
      log.error("[SettlementScheduler] 정산 수집 배치 실행 중 예외 발생: {}", e.getMessage(), e);
    }
  }

  // TODO: 추후 쉬는날 고려 필요
  // 매월 25일 04:00 (KST)
  @Scheduled(cron = "${settlement.scheduler.monthly-25-04:}", zone = "Asia/Seoul")
  public void runOnThe25th() {
    log.debug("[SettlementScheduler] 월별 정산 배치 시작");

    try {
      JobExecution execution = settlementJobLauncher.runMonthlyPayoutJob();

      if (execution.getStatus().isUnsuccessful()) {
        log.error("[SettlementScheduler] 월별 정산 배치 실패: {}", execution.getAllFailureExceptions());
      } else {
        log.debug("[SettlementScheduler] 월별 정산 배치 완료: {}", execution.getStatus());
      }
    } catch (Exception e) {
      log.error("[SettlementScheduler] 월별 정산 배치 실행 중 예외 발생: {}", e.getMessage(), e);
    }
  }
}
