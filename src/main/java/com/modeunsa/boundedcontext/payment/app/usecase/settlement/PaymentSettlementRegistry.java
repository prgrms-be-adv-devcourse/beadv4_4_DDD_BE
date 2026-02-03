package com.modeunsa.boundedcontext.payment.app.usecase.settlement;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PaymentSettlementRegistry {

  private final Map<PaymentPurpose, PaymentSettlementProcess> paymentProcesses;

  public PaymentSettlementRegistry(List<PaymentSettlementProcess> processes) {
    this.paymentProcesses =
        processes.stream()
            .collect(Collectors.toMap(PaymentSettlementProcess::purpose, process -> process));
  }

  public PaymentSettlementProcess get(PaymentPurpose purpose) {
    PaymentSettlementProcess process = paymentProcesses.get(purpose);
    if (process == null) {
      throw new IllegalArgumentException("PaymentPurpose is not supported.");
    }
    return process;
  }
}
