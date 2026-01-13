package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentCreateAccountUseCase {

  private final PaymentAccountRepository paymentAccountRepository;

  public void createPaymentAccount(PaymentAccount paymentAccount) {
    paymentAccountRepository.save(paymentAccount);
  }
}
