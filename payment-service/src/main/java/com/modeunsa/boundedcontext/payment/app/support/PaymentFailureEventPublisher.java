package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFailureEventPublisher {

  private final EventPublisher eventPublisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void publish(
      Long buyerId,
      Long orderId,
      String orderNo,
      BigDecimal totalAmount,
      String errorCode,
      String message) {
    PaymentFailedEvent event =
        PaymentFailedEvent.from(buyerId, orderId, orderNo, totalAmount, errorCode, message);
    eventPublisher.publish(event);
  }
}
