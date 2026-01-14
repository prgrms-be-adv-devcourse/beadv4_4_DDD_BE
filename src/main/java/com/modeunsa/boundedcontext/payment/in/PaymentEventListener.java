package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentRequestEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final PaymentFacade paymentFacade;
  private final PaymentMapper paymentMapper;

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberCreateEvent(PaymentMemberCreatedEvent paymentMemberCreatedEvent) {
    Long memberId = paymentMemberCreatedEvent.getMemberId();
    log.info("PaymentMemberCreatedEvent 수신 - memberId: {}", memberId);

    try {
      paymentFacade.createPaymentAccount(memberId);
      log.info("PaymentAccount 생성 완료 - memberId: {}", memberId);
    } catch (Exception e) {
      log.error("PaymentAccount 생성 실패 - memberId: {}", memberId, e);
      throw e;
    }
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentRequestEvent(PaymentRequestEvent paymentRequestEvent) {
    log.info(
        "PaymentRequestEvent 수신 - buyerId: {}, orderNo: {}",
        paymentRequestEvent.getBuyerId(),
        paymentRequestEvent.getOrderNo());

    try {
      PaymentRequest request = paymentMapper.toPaymentRequestDto(paymentRequestEvent);
      paymentFacade.paymentRequest(request);
      log.info(
          "PaymentRequestEvent 생성 완료 - buyerId: {}, orderNo: {}",
          paymentRequestEvent.getBuyerId(),
          paymentRequestEvent.getOrderNo());
    } catch (Exception e) {
      log.error(
          "PaymentRequestEvent 처리 실패 - buyerId: {}, orderNo: {}",
          paymentRequestEvent.getBuyerId(),
          paymentRequestEvent.getOrderNo(),
          e);
      throw e;
    }
  }
}
