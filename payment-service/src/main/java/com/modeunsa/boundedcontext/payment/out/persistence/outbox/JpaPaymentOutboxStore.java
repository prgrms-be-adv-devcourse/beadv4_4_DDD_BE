package com.modeunsa.boundedcontext.payment.out.persistence.outbox;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.boundedcontext.payment.out.PaymentOutboxStore;
import com.modeunsa.global.kafka.outbox.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentOutboxStore implements PaymentOutboxStore {

  private final PaymentOutboxCommandRepository paymentOutboxCommandRepository;

  @Override
  public PaymentOutboxEvent store(PaymentOutboxEvent newPaymentOutboxEvent) {
    return paymentOutboxCommandRepository.store(newPaymentOutboxEvent);
  }

  @Override
  public long deleteAlreadySentEventByIds(List<Long> ids) {
    return paymentOutboxCommandRepository.deleteAlreadySentEventBefore(ids);
  }

  @Override
  public void markProcessing(Long id) {
    paymentOutboxCommandRepository.updateStatus(id, OutboxStatus.PROCESSING, LocalDateTime.now());
  }

  @Override
  public void markSent(Long id) {
    paymentOutboxCommandRepository.markSent(id, OutboxStatus.SENT, LocalDateTime.now());
  }

  @Override
  public void markFailed(Long id, String errorMessage, int maxRetry) {
    paymentOutboxCommandRepository.markFailed(id, errorMessage, LocalDateTime.now(), maxRetry);
  }

  @Override
  public void markProcessing(Long id) {
    paymentOutboxRepository.updateStatus(id, OutboxStatus.PROCESSING, LocalDateTime.now());
  }

  @Override
  public void markSent(Long id) {
    paymentOutboxRepository.markSent(id, OutboxStatus.SENT, LocalDateTime.now());
  }

  @Override
  public void markFailed(Long id, String errorMessage, int maxRetry) {
    paymentOutboxRepository.markFailed(
        id, errorMessage, LocalDateTime.now(), maxRetry, OutboxStatus.PENDING, OutboxStatus.FAILED);
  }
}
