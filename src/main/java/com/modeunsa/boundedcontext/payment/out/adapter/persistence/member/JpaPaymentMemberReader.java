package com.modeunsa.boundedcontext.payment.out.adapter.persistence.member;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.port.PaymentMemberReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPaymentMemberReader implements PaymentMemberReader {

  private final PaymentMemberQueryRepository queryRepository;

  @Override
  public Optional<PaymentMember> findById(Long memberId) {
    return queryRepository.findById(memberId);
  }
}
