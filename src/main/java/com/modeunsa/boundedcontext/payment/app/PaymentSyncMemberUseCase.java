package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentSyncMemberUseCase {

  private final PaymentMemberRepository paymentMemberRepository;

  public PaymentMember syncMember(PaymentMemberDto paymentMemberDto) {

    PaymentMember paymentMember =
        PaymentMember.register(
            1L,
            paymentMemberDto.getEmail(),
            paymentMemberDto.getName(),
            paymentMemberDto.getCustomerKey(),
            paymentMemberDto.getStatus());

    return paymentMemberRepository.save(paymentMember);
  }
}
