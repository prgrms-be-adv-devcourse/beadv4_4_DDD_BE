package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.usecase.settlement.PaymentSettlementRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentSettlementUseCase {

  private final PaymentSettlementRegistry paymentSettlementRegistry;

  public void execute(PaymentProcessContext context) {
    paymentSettlementRegistry.get(context.paymentPurpose()).execute(context);
  }
}
