package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentOrderCanceledEvent;
import com.modeunsa.boundedcontext.payment.app.event.PaymentPayoutCompletedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.shared.payment.dto.PaymentDto;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
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
  public void handlePayoutCompletedEvent(PaymentPayoutCompletedEvent paymentPayoutCompletedEvent) {
    log.info(
        "PayoutCompletedEvent 수신 - payoutId: {}, payeeId : {}",
        paymentPayoutCompletedEvent.getPayout().getId(),
        paymentPayoutCompletedEvent.getPayout().getPayeeId());

    try {
      PaymentPayoutDto payout = paymentPayoutCompletedEvent.getPayout();
      paymentFacade.completePayout(payout);
      log.info(
          "PayoutCompletedEvent 처리 완료 - payoutId: {}, payeeId : {}",
          paymentPayoutCompletedEvent.getPayout().getId(),
          paymentPayoutCompletedEvent.getPayout().getPayeeId());
    } catch (Exception e) {
      log.error(
          "PayoutCompletedEvent 처리 실패 - payoutId: {}, payeeId : {}",
          paymentPayoutCompletedEvent.getPayout().getId(),
          paymentPayoutCompletedEvent.getPayout().getPayeeId(),
          e);
      throw e;
    }
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentFailedEvent(PaymentFailedEvent paymentFailedEvent) {

    PaymentDto payment = paymentFailedEvent.getPayment();

    log.info(
        "PaymentFailedEvent 수신 - buyerId: {}, orderNo: {}",
        payment.getBuyerId(),
        payment.getOrderNo());

    try {
      paymentFacade.refund(payment, RefundEventType.PAYMENT_FAILED);
      log.info(
          "PaymentFailedEvent 처리 완료 - buyerId: {}, orderNo: {}",
          payment.getBuyerId(),
          payment.getOrderNo());
    } catch (Exception e) {
      log.error(
          "PaymentFailedEvent 처리 실패 - buyerId: {}, orderNo: {}",
          payment.getBuyerId(),
          payment.getOrderNo(),
          e);
      throw e;
    }
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Transactional(propagation = REQUIRES_NEW)
  public void handleOrderCanceledEvent(PaymentOrderCanceledEvent paymentOrderCanceledEvent) {

    PaymentDto payment = paymentMapper.toPaymentDto(paymentOrderCanceledEvent.getOrder());

    log.info(
        "PaymentOrderCanceledEvent 수신 - orderId: {}",
        paymentOrderCanceledEvent.getOrder().getOrderId());

    try {
      paymentFacade.refund(payment, RefundEventType.ORDER_CANCELLED);
      log.info(
          "PaymentOrderCanceledEvent 처리 완료 - orderId: {}",
          paymentOrderCanceledEvent.getOrder().getOrderId());
    } catch (Exception e) {
      log.error(
          "PaymentOrderCanceledEvent 처리 실패 - orderId: {}",
          paymentOrderCanceledEvent.getOrder().getOrderId(),
          e);
      throw e;
    }
  }
}
