package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import com.modeunsa.global.kafka.outbox.OutboxStore;
import java.util.List;

public interface PaymentOutboxStore extends OutboxStore {
  PaymentOutboxEvent store(PaymentOutboxEvent newPaymentOutboxEvent);

  @Override
  int deleteAlreadySentEventByIds(List<Long> ids);

  @Override
  void markProcessing(Long id);

  @Override
  void markSent(Long id);

  @Override
  void markFailed(Long id, String errorMessage, int maxRetry);
}
