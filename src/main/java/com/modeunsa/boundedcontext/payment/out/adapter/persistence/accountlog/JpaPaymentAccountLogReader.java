package com.modeunsa.boundedcontext.payment.out.adapter.persistence.accountlog;

import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.out.port.PaymentAccountLogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentAccountLogReader implements PaymentAccountLogReader {

  private final PaymentAccountQueryRepository queryRepository;

  @Override
  public long countByReferenceType(ReferenceType referenceType) {
    return queryRepository.countByReferenceType(referenceType);
  }
}
