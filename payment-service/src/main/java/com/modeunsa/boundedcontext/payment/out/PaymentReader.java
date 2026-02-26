package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentListItemResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentSearchRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentId;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface PaymentReader {
  Optional<Payment> findById(PaymentId paymentId);

  Page<PaymentListItemResponse> findPageByMemberIdWithSearch(
      Long memberId, PaymentSearchRequest request);

  Optional<Payment> findByPgOrderId(String orderId);
}
