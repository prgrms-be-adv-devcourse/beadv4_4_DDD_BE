package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentOutboxEvent;
import java.util.List;

public interface PaymentOutboxStore {
  PaymentOutboxEvent store(PaymentOutboxEvent newPaymentOutboxEvent);

  int deleteAlreadySentEventByIds(List<Long> ids);
}
