package com.modeunsa.boundedcontext.payment.out.persistence.payment;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentListItemResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import com.modeunsa.boundedcontext.payment.out.PaymentReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentReader implements PaymentReader {

  private final PaymentQueryRepository queryRepository;

  @Override
  public Optional<Payment> findById(PaymentId paymentId) {
    return queryRepository.findById(paymentId);
  }

  @Override
  public Optional<Payment> findByOrderNo(String orderNo) {
    return queryRepository.findByOrderNo(orderNo);
  }

  @Override
  public Page<PaymentListItemResponse> findPageByMemberIdWithSearch(
      Long memberId, PaymentSearchRequest request) {
    return queryRepository.findPageByMemberIdWithSearch(memberId, request);
  }
}
