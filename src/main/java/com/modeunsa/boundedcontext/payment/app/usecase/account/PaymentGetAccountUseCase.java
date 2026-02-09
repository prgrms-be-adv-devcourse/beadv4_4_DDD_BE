package com.modeunsa.boundedcontext.payment.app.usecase.account;

import com.modeunsa.boundedcontext.payment.app.dto.account.PaymentAccountDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentGetAccountUseCase {

  private final PaymentAccountSupport paymentAccountSupport;

  public PaymentAccountDto execute(Long memberId) {
    PaymentAccount paymentAccount = paymentAccountSupport.getPaymentAccountByMemberId(memberId);
    return PaymentAccountDto.of(memberId, paymentAccount.getBalance());
  }
}
