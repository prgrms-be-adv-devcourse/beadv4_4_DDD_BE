package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentGetMemberUseCase {

  private final PaymentMemberSupport paymentMemberSupport;

  public PaymentMember getMember(Long memberId) {
    return paymentMemberSupport.getPaymentMemberById(memberId);
  }
}
