package com.modeunsa.boundedcontext.payment.out;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import java.util.Optional;

public interface PaymentMemberReader {
  Optional<PaymentMember> findById(Long memberId);
}
