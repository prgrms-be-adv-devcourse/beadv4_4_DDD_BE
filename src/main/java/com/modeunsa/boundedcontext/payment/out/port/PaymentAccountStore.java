package com.modeunsa.boundedcontext.payment.out.port;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;

public interface PaymentAccountStore {
  PaymentAccount store(PaymentAccount newPaymentAccount);
}
