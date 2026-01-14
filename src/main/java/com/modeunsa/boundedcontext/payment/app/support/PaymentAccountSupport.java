package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAccountSupport {

  private final PaymentAccountRepository paymentAccountRepository;

  public PaymentAccount getPaymentAccountByMemberId(Long memberId) {
    return paymentAccountRepository
        .findByMemberId(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }
}
