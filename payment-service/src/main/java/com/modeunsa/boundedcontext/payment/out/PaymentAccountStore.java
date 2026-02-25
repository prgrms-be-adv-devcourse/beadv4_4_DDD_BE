package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;

public interface PaymentAccountStore {
  PaymentAccount store(PaymentAccount newPaymentAccount);
}
