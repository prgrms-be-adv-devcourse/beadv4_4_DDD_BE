package com.modeunsa.boundedcontext.payment.out.port;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;

public interface PaymentMemberStore {
  PaymentMember store(PaymentMember newPaymentMember);
}
