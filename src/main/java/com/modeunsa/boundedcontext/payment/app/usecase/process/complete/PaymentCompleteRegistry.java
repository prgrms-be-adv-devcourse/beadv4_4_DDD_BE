package com.modeunsa.boundedcontext.payment.app.usecase.process.complete;

import com.modeunsa.boundedcontext.payment.domain.exception.PaymentDomainException;
import com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompleteRegistry {

  private final Map<PaymentPurpose, PaymentCompleteProcess> paymentProcesses;

  public PaymentCompleteRegistry(List<PaymentCompleteProcess> processes) {
    this.paymentProcesses =
        processes.stream()
            .collect(Collectors.toMap(PaymentCompleteProcess::purpose, process -> process));
  }

  public PaymentCompleteProcess get(PaymentPurpose purpose) {
    PaymentCompleteProcess process = paymentProcesses.get(purpose);
    if (process == null) {
      throw new PaymentDomainException(
          PaymentErrorCode.INVALID_PAYMENT_PURPOSE, "invalid payment purpose : %s", purpose);
    }
    return process;
  }
}
