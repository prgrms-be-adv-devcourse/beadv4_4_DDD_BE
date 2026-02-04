package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;

public interface PaymentMemberStore {
  PaymentMember store(PaymentMember newPaymentMember);
}
