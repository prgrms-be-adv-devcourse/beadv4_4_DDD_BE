package com.modeunsa.boundedcontext.payment.app.usecase.member;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.support.PaymentAccountSupport;
import com.modeunsa.boundedcontext.payment.app.support.PaymentMemberSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentGetMemberUseCase {

  private final PaymentMemberSupport paymentMemberSupport;
  private final PaymentAccountSupport paymentAccountSupport;

  public PaymentMemberDto execute(Long memberId) {
    PaymentMember member = paymentMemberSupport.getPaymentMemberById(memberId);
    PaymentAccount paymentAccount = paymentAccountSupport.getPaymentAccountByMemberId(memberId);
    return PaymentMemberDto.memberInfoWithBalance(
        member.getCustomerKey(), member.getEmail(), member.getName(), paymentAccount.getBalance());
  }
}
