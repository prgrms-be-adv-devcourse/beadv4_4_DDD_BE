package com.modeunsa.boundedcontext.payment.app.usecase.ledger;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_INVALID_DATE_REQUEST;

import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountLogDto;
import com.modeunsa.boundedcontext.payment.app.dto.accountlog.PaymentAccountSearchRequest;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountLogReader;
import com.modeunsa.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentAccountLedgerUseCase {

  private final PaymentAccountLogReader paymentAccountLogReader;

  public Page<PaymentAccountLogDto> execute(
      Long memberId, PaymentAccountSearchRequest paymentAccountSearchRequest) {

    if (paymentAccountSearchRequest.from().isAfter(paymentAccountSearchRequest.to())) {
      throw new GeneralException(PAYMENT_INVALID_DATE_REQUEST);
    }

    return paymentAccountLogReader.getAccountLedgerPageBySearch(
        memberId, paymentAccountSearchRequest);
  }
}
