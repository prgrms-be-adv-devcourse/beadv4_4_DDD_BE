package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAccountSupport {

  private final PaymentAccountConfig paymentAccountConfig;
  private final PaymentAccountRepository paymentAccountRepository;

  public PaymentAccount getPaymentAccountByMemberId(Long memberId) {
    return paymentAccountRepository
        .findByMemberId(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getSystemAccount() {
    Long systemMemberId = paymentAccountConfig.getSystemMemberId();
    return paymentAccountRepository
        .findByMemberId(systemMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getHolderAccount() {
    Long holderMemberId = paymentAccountConfig.getHolderMemberId();
    return paymentAccountRepository
        .findByMemberId(holderMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }
}
