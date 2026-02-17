package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.event.PaymentFailedEvent;
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
  private final EventPublisher eventPublisher;

  public PaymentProcessContext executeForPaymentRequest(PaymentProcessContext context) {
    try {
      return processForPaymentRequest(context);
    } catch (PaymentDomainException e) {
      handleFailure(context, e.getErrorCode());
      throw e;
    }
  }

  public void executeForPaymentConfirm(PaymentProcessContext context) {
    try {
      processForPaymentConfirm(context);
    } catch (PaymentDomainException e) {
      handleFailure(context, e.getErrorCode());
      throw e;
    }
  }

  /*
   * 결제 진행 가능 여부를 검증하고, PG 충전 필요 시 부족/전액 금액을 계산하여 반환한다.
   * - Request 경로(requestPayment): needPgPayment=false 로 들어옴. ProviderType 에 따라 분기.
   * - MODEUNSA_PAY: 잔액 부족분만 PG 결제 → requestPgAmount = 부족금액, needPgPayment = 부족 시 true.
   * - TOSS_PAYMENTS(PG): 전액 PG 결제 → requestPgAmount = totalAmount, needPgPayment = true.
   * 결제 실패 시에는 PaymentFailedEvent를 발행하여 결제 실패 이력을 남긴다.
   */
  private PaymentProcessContext processForPaymentRequest(PaymentProcessContext context) {

    // 1. payment 상태를 IN_PROGRESS로 변경
    Payment payment = loadAndMarkInProgress(context);

    // 2. 구매자 검증
    PaymentMember buyer = loadAndValidateBuyer(context.buyerId());

    BigDecimal totalAmount = context.totalAmount();
    final BigDecimal requestPgAmount;
    final boolean needPgPayment;

    if (context.providerType() == ProviderType.TOSS_PAYMENTS) {
      // PG(토스) 전액 결제: 잔액 사용 없이 전액을 PG로 결제
      requestPgAmount = totalAmount;
      needPgPayment = true;
    } else {
      // 뭐든사페이: 잔액 부족분만 PG로 충전
      PaymentAccount paymentAccount = loadBuyerAccount(buyer.getId());
      requestPgAmount = paymentAccount.calculateInsufficientAmount(totalAmount);
      needPgPayment = requestPgAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    // 3. 충전 필요 여부, 부족/전액 금액 정보 반영
    payment.updatePgRequestAmount(needPgPayment, requestPgAmount);

    return PaymentProcessContext.fromPaymentForInProgress(payment);
  }

  /*
   * 결제 확인 요청에서 부족 금액과 PG 결제 금액이 일치하는지 검증하고,
   * PG 결제 정보를 Payment 엔티티에 반영한다.
   * 결제 실패 시에는 PaymentFailedEvent를 발행하여 결제 실패 이력을 남긴다.
   */
  private void processForPaymentConfirm(PaymentProcessContext context) {

    // 1. payment 조회 및 유효한 상태 검증
    Payment payment = loadPayment(context);

    // 2. 구매자 검증
    loadAndValidateBuyer(context.buyerId());

    // 3. 결제 계좌 조회
    loadBuyerAccount(context.buyerId());

    // 4. 부족 금액과 PG 결제 금액 차이 검증
    payment.validateChargeAmount(context.requestPgAmount());

    // 5. PG 결제 정보 반영
    payment.updatePgCustomerAndOrderInfo(context);
  }

  private Payment loadAndMarkInProgress(PaymentProcessContext context) {
    PaymentId paymentId = PaymentId.create(context.buyerId(), context.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.changeToInProgress();
    return payment;
  }

  private Payment loadPayment(PaymentProcessContext context) {
    PaymentId paymentId = PaymentId.create(context.buyerId(), context.orderNo());
    Payment payment = paymentSupport.getPaymentById(paymentId);
    payment.validatePgProcess();
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

  private void handleFailure(PaymentProcessContext context, PaymentErrorCode exception) {
    eventPublisher.publish(
        PaymentFailedEvent.from(
            context.buyerId(),
            context.orderId(),
            context.orderNo(),
            context.totalAmount(),
            exception.getCode(),
            exception.getMessage()));
  }
}
