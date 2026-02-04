package com.modeunsa.boundedcontext.payment.app.usecase.complete;

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

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCompleteDepositChargeUseCase implements PaymentCompleteProcess {

  private final PaymentAccountSupport paymentAccountSupport;
  private final PaymentSupport paymentSupport;

  @Override
  public PaymentPurpose purpose() {
    return PaymentPurpose.DEPOSIT_CHARGE;
  }

  @Override
  public void execute(PaymentProcessContext context) {
    PaymentAccount buyerAccount =
        paymentAccountSupport.getPaymentAccountByMemberIdForUpdate(context.buyerId());

    buyerAccount.credit(
        context.requestPgAmount(),
        PaymentEventType.CHARGE_PG_TOSS_PAYMENTS,
        context.buyerId(),
        ReferenceType.PAYMENT_MEMBER);

    paymentSupport.changePaymentStatus(
        context.buyerId(), context.orderNo(), PaymentStatus.COMPLETED);
  }
}
