package com.modeunsa.boundedcontext.payment.app.usecase.settlement;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;

public interface PaymentSettlementProcess {
  PaymentPurpose purpose();

  void execute(PaymentProcessContext context);
}
