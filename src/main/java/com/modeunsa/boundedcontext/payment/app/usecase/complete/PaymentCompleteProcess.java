package com.modeunsa.boundedcontext.payment.app.usecase.complete;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;

public interface PaymentCompleteProcess {
  PaymentPurpose purpose();

  void execute(PaymentProcessContext context);
}
