package com.modeunsa.boundedcontext.settlement.in.batch;

import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.types.SettlementStatus;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementMonthlyJobExecutionListener implements JobExecutionListener {
  private final SettlementRepository settlementRepository;

  @Override
  @Transactional
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getJobInstance() == null
        || !"monthlySettlementJob".equals(jobExecution.getJobInstance().getJobName())) {
      return;
    }

    if (!jobExecution.getStatus().isUnsuccessful()) {
      return;
    }

    Long batchExecutionId = jobExecution.getId();
    List<Settlement> processingSettlements =
        settlementRepository.findByBatchExecutionIdAndStatusOrderByIdAsc(
            batchExecutionId, SettlementStatus.PROCESSING);

    for (Settlement settlement : processingSettlements) {
      settlement.rollbackToPending();
    }

    log.warn(
        "[SettlementMonthlyJobExecutionListener] 월 정산 배치 실패로 PROCESSING 상태 롤백:"
            + " jobExecutionId={}, count={}",
        batchExecutionId,
        processingSettlements.size());
  }
}
