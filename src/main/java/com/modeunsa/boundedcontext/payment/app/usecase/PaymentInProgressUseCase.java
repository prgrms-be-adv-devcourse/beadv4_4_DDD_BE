package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.event.PaymentFailedEvent;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(noRollbackFor = PaymentDomainException.class)
@RequiredArgsConstructor
public class PaymentInProgressUseCase {

  private final PaymentSupport paymentSupport;
  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentAccountSupport paymentAccountSupport;
  private final SpringDomainEventPublisher eventPublisher;

  public PaymentProcessContext execute(PaymentProcessContext context) {
    try {

      if (!context.needsCharge()) {
        return processWithoutPg(context);
      } else {
        return processWithPg(context);
      }
    } catch (PaymentDomainException e) {
      handleFailure(context, e);
      throw e;
    }
  }

  /*
   * 결제 진행 가능 여부를 검증하고, 부족 금액을 계산하여 반환한다.
   * 결제 실패 시에는 PaymentFailedEvent를 발행하여 결제 실패 이력을 남긴다.
   */
  private PaymentProcessContext processWithoutPg(PaymentProcessContext context) {

    // 1. payment 상태를 IN_PROGRESS로 변경
    Payment payment = loadAndMarkInProgress(context);

    // 2. 구매자 검증
    PaymentMember buyer = loadAndValidateBuyer(context.buyerId());

    // 3. 결제 계좌 조회
    PaymentAccount paymentAccount = loadBuyerAccount(buyer.getId());

    // 4. 부족 금액 계산
    BigDecimal totalAmount = context.totalAmount();
    BigDecimal shortAmount = paymentAccount.calculateInsufficientAmount(totalAmount);
    boolean needCharge = shortAmount.compareTo(BigDecimal.ZERO) > 0;

    // 5. 충전 필요 여부, 부족 금액 정보 반영
    payment.updateChargeInfo(needCharge, shortAmount);

    return PaymentProcessContext.fromPaymentForInProgress(payment);
  }

  private PaymentProcessContext processWithPg(PaymentProcessContext context) {

    // 1. payment 상태를 IN_PROGRESS로 변경
    Payment payment = loadAndMarkInProgress(context);

    // 2. 구매자 검증
    loadAndValidateBuyer(context.buyerId());

    // 3. 결제 계좌 조회
    loadBuyerAccount(context.buyerId());

    // 4. 부족 금액과 PG 결제 금액 차이 검증
    payment.validateChargeAmount(context.chargeAmount());

    // 5. PG 결제 정보 반영
    payment.updatePgIngo(context);

    return PaymentProcessContext.fromPaymentForInProgress(payment);
  }

  private Payment loadAndMarkInProgress(PaymentProcessContext context) {
    PaymentId paymentId = PaymentId.create(context.buyerId(), context.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.changeInProgress();
    return payment;
  }

  private PaymentMember loadAndValidateBuyer(Long buyerId) {
    PaymentMember buyer = paymentMemberSupport.getPaymentMemberById(buyerId);
    buyer.validateCanOrder();
    return buyer;
  }

  private PaymentAccount loadBuyerAccount(Long buyerId) {
    return paymentAccountSupport.getPaymentAccountByMemberId(buyerId);
  }

  private void handleFailure(PaymentProcessContext context, PaymentDomainException exception) {
    eventPublisher.publish(PaymentFailedEvent.from(context, exception.getErrorCode()));
  }
}
