package com.modeunsa.boundedcontext.payment.app.outbox;

import com.modeunsa.boundedcontext.payment.out.PaymentOutboxReader;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import com.modeunsa.global.kafka.outbox.OutboxPollerRunner;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "outbox.poller.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentOutboxPoller {

  private final OutboxPollerRunner outboxPollerRunner;
  private final PaymentOutboxReader paymentOutboxReader;
  private final PaymentOutboxStore paymentOutboxStore;

  @Value("${outbox.poller.batch-size:100}")
  private int batchSize;

  @Value("${outbox.poller.max-retry:5}")
  private int maxRetry;

  @Value("${outbox.poller.retention-days:7}")
  private int retentionDays;

  @Value("${outbox.timeoutMs:10000}")
  private int timeoutMs;

  @Value("${outbox.cleanup.batch-size:500}")
  private int cleanupBatchSize;

  @Scheduled(fixedDelayString = "${outbox.poller.interval-ms:5000}")
  @Transactional
  public void poll() {
    outboxPollerRunner.runPolling(
        paymentOutboxReader, paymentOutboxStore, batchSize, maxRetry, timeoutMs);
  }

  @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")
  @Transactional
  public void cleanupOldEvents() {
    LocalDateTime before = LocalDateTime.now().minusDays(retentionDays);
    outboxPollerRunner.runCleanup(
        paymentOutboxReader, paymentOutboxStore, before, cleanupBatchSize);
  }
}
