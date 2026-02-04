package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.persistence.PaymentMemberReader;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMemberSupport {

  private final PaymentMemberReader paymentMemberReader;

  public PaymentMember getPaymentMemberById(Long memberId) {
    return paymentMemberReader
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_MEMBER_NOT_FOUND));
  }
}
