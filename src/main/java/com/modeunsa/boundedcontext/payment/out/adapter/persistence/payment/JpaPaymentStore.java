package com.modeunsa.boundedcontext.payment.out.adapter.persistence.payment;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.out.port.PaymentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentStore implements PaymentStore {

  private final PaymentRepository paymentRepository;

  @Override
  public Payment store(Payment newPayment) {
    return paymentRepository.save(newPayment);
  }
}
