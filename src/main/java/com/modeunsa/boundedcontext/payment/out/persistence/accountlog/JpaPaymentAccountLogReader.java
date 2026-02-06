package com.modeunsa.boundedcontext.payment.out.persistence.accountlog;

import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountLogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentAccountLogReader implements PaymentAccountLogReader {

  private final PaymentAccountLogQueryRepository queryRepository;

  @Override
  public long countByReferenceType(ReferenceType referenceType) {
    return queryRepository.countByReferenceType(referenceType);
  }

  @Override
  public Page<PaymentAccountLogDto> getAccountLedgerPageBySearch(
      Long memberId, PaymentAccountSearchRequest paymentAccountSearchRequest) {
    return queryRepository.getAccountLedgerPageBySearch(memberId, paymentAccountSearchRequest);
  }
}
