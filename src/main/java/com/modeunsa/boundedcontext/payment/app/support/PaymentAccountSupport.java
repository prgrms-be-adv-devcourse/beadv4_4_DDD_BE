package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountRepository;
import com.modeunsa.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Component
@RequiredArgsConstructor
public class PaymentAccountSupport {

  private final PaymentAccountRepository paymentAccountRepository;

  public void validDuplicateAccount(Long memberId) {
    boolean exist = paymentAccountRepository.existsByMemberId(memberId);
    if (exist) {
      throw new GeneralException("PaymentAccount already exists for memberId: " + memberId);
    }
  }

  public PaymentAccount getPaymentAccountByMemberId(Long memberId) {
    return paymentAccountRepository
        .findByMemberId(memberId)
        .orElseThrow(
            () -> new GeneralException("PaymentAccount not found with memberId: " + memberId));
  }
}
