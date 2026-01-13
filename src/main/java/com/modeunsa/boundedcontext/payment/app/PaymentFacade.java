package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentCreateAccountUseCase;
import com.modeunsa.boundedcontext.payment.app.usecase.PaymentSyncMemberUseCase;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
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
  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentAccountSupport paymentAccountSupport;

  @Transactional
  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  public void createPaymentAccount(Long memberId) {

    paymentAccountSupport.validDuplicateAccount(memberId);

    PaymentMember paymentMember = paymentMemberSupport.getPaymentMemberById(memberId);

    PaymentAccount saved = PaymentAccount.create(paymentMember);

    paymentCreateAccountUseCase.createPaymentAccount(saved);
  }
}
