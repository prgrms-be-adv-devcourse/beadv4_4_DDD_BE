package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentCreateAccountUseCase {

  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentAccountRepository paymentAccountRepository;

  @Transactional
  public void createPaymentAccount(Long memberId) {

    boolean exist = paymentAccountRepository.existsByMemberId(memberId);
    if (exist) {
      throw new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_DUPLICATE);
    }

    PaymentMember paymentMember = paymentMemberSupport.getPaymentMemberById(memberId);

    PaymentAccount paymentAccount = PaymentAccount.create(paymentMember);

    paymentAccountRepository.save(paymentAccount);
  }
}
