package com.modeunsa.boundedcontext.payment.app.usecase.process.complete;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;

public interface PaymentCompleteProcess {
  PaymentPurpose purpose();

  void execute(PaymentProcessContext context);
}
