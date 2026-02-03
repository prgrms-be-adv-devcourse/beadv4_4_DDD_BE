package com.modeunsa.boundedcontext.payment.app.usecase.settlement;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentStatus;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 토스페이먼츠 예치금 충전 완료 처리. buyer 계좌에 PG 결제 금액만 입금하고 결제 상태를 COMPLETED로 변경한다. */
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentSettlementDepositChargeUseCase implements PaymentSettlementProcess {

  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentSupport paymentSupport;

  @Override
  public PaymentPurpose purpose() {
    return PaymentPurpose.DEPOSIT_CHARGE;
  }

  @Override
  public void execute(PaymentProcessContext context) {
    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberId(context.buyerId());

    buyerAccount.credit(
        context.requestPgAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        context.orderId(),
        ReferenceType.PAYMENT_MEMBER);

    paymentSupport.changePaymentStatus(
        context.buyerId(), context.orderNo(), PaymentStatus.COMPLETED);
  }
}
