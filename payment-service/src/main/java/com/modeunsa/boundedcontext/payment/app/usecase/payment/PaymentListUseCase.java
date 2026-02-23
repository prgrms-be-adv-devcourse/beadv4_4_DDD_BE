package com.modeunsa.boundedcontext.payment.app.usecase.payment;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentListItemResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentSearchRequest;
import com.modeunsa.boundedcontext.payment.out.PaymentReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentListUseCase {

  private final PaymentReader paymentReader;

  public Page<PaymentListItemResponse> execute(Long memberId, PaymentSearchRequest request) {
    return paymentReader.findPageByMemberIdWithSearch(memberId, request);
  }
}
