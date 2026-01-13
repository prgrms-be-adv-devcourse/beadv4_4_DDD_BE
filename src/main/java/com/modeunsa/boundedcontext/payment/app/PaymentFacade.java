package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.PaymentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentFacade {

  private final PaymentSyncMemberUseCase paymentSyncMemberUseCase;

  @Transactional
  public PaymentMember syncMember(PaymentMemberDto paymentMemberDto) {
    return paymentSyncMemberUseCase.syncMember(paymentMemberDto);
  }
}
