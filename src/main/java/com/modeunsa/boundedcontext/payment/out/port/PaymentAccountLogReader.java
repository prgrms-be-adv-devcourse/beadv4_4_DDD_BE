package com.modeunsa.boundedcontext.payment.out.port;

import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;

public interface PaymentAccountLogReader {
  long countByReferenceType(ReferenceType referenceType);
}
