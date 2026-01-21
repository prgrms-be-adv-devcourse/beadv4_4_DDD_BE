package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMemberSupport {

  private final PaymentMemberRepository paymentMemberRepository;

  public PaymentMember getPaymentMemberById(Long memberId) {
    return paymentMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_MEMBER_NOT_FOUND));
  }

  public long countMember() {
    return paymentMemberRepository.count();
  }
}
