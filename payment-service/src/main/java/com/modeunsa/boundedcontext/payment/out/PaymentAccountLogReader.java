package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import org.springframework.data.domain.Page;

public interface PaymentAccountLogReader {
  long countByReferenceType(ReferenceType referenceType);

  Page<PaymentAccountLogDto> getAccountLedgerPageBySearch(
      Long memberId, PaymentAccountSearchRequest paymentAccountSearchRequest);

  boolean existsByAlreadyAccountLogEvent(
      Long accountId, ReferenceType referenceType, Long referenceId, PaymentEventType eventType);
}
