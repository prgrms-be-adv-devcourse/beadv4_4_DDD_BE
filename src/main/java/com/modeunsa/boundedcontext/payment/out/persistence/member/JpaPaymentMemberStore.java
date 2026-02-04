package com.modeunsa.boundedcontext.payment.out.persistence.member;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.persistence.PaymentMemberStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentMemberStore implements PaymentMemberStore {

  private final PaymentMemberRepository paymentMemberRepository;

  @Override
  public PaymentMember store(PaymentMember newPaymentMember) {
    return paymentMemberRepository.save(newPaymentMember);
  }
}
