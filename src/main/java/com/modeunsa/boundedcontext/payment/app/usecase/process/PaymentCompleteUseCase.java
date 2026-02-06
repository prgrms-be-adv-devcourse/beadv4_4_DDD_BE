package com.modeunsa.boundedcontext.payment.app.usecase.process;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import com.modeunsa.boundedcontext.payment.app.usecase.process.complete.PaymentCompleteRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCompleteUseCase {

  private final PaymentCompleteRegistry paymentCompleteRegistry;

  public void execute(PaymentProcessContext context) {
    paymentCompleteRegistry.get(context.paymentPurpose()).execute(context);
  }
}
