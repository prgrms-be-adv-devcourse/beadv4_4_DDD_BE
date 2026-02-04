package com.modeunsa.boundedcontext.payment.out.persistence.account;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentAccountStore implements PaymentAccountStore {

  private final PaymentAccountRepository paymentAccountRepository;

  @Override
  public PaymentAccount store(PaymentAccount newPaymentAccount) {
    return paymentAccountRepository.save(newPaymentAccount);
  }
}
