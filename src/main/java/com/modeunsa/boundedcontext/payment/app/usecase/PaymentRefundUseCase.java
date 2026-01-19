package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.lock.LockedPaymentAccounts;
import com.modeunsa.boundedcontext.payment.app.lock.PaymentAccountLockManager;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.domain.types.RefundEventType;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentRefundUseCase {

  private final PaymentAccountLockManager paymentAccountLockManager;
  private final PaymentAccountConfig paymentAccountConfig;

  public void execute(PaymentDto payment, RefundEventType refundEventType) {

    LockedPaymentAccounts accounts =
        paymentAccountLockManager.getEntitiesForUpdateInOrder(
            paymentAccountConfig.getHolderMemberId(), payment.getBuyerId());

    PaymentAccount holderAccount = accounts.get(paymentAccountConfig.getHolderMemberId());
    PaymentAccount buyerAccount = accounts.get(payment.getBuyerId());
    PaymentEventType eventType = PaymentEventType.fromRefundEventType(refundEventType);

    if (!holderAccount.canPayOrder(payment.getTotalAmount())) {
      throw new GeneralException(ErrorStatus.PAYMENT_INSUFFICIENT_BALANCE);
    }

    holderAccount.debit(
        payment.getTotalAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
    buyerAccount.credit(
        payment.getTotalAmount(), eventType, payment.getOrderId(), ReferenceType.PAYMENT);
  }
}
