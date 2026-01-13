package com.modeunsa.boundedcontext.payment.app;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.PaymentAccount;
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
  private final PaymentCreateAccountUseCase paymentCreateAccountUseCase;
  private final PaymentSupport paymentSupport;

  @Transactional
  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {
    paymentSyncMemberUseCase.createPaymentMember(paymentMemberDto);
  }

  public void createPaymentAccount(Long memberId) {

    PaymentMember _paymentMember = paymentSupport.getPaymentMemberById(memberId);

    PaymentAccount paymentAccount = PaymentAccount.create(_paymentMember);

    paymentCreateAccountUseCase.createPaymentAccount(paymentAccount);
  }
}
