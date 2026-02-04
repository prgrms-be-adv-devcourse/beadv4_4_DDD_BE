package com.modeunsa.boundedcontext.payment.out.adapter.persistence.payment;

import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.out.port.PaymentReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentReader implements PaymentReader {

  private final PaymentQueryRepository queryRepository;

  @Override
  public Optional<Payment> findById(PaymentId paymentId) {
    return queryRepository.findById(paymentId);
  }
}
