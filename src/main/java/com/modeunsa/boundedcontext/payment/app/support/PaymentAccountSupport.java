package com.modeunsa.boundedcontext.payment.app.support;

import com.modeunsa.boundedcontext.payment.app.dto.payout.PaymentPayoutDto;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccount;
import com.modeunsa.boundedcontext.payment.domain.types.ReferenceType;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountLogReader;
import com.modeunsa.boundedcontext.payment.out.PaymentAccountReader;
import com.modeunsa.global.config.PaymentAccountConfig;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAccountSupport {

  private final PaymentAccountConfig paymentAccountConfig;
  private final PaymentAccountReader paymentAccountReader;
  private final PaymentAccountLogReader paymentAccountLogReader;

  public PaymentAccount getPaymentAccountByMemberId(Long memberId) {
    return paymentAccountReader
        .findByMemberId(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getSystemAccount() {
    Long systemMemberId = paymentAccountConfig.getSystemMemberId();
    return paymentAccountReader
        .findByMemberId(systemMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getHolderAccount() {
    Long holderMemberId = paymentAccountConfig.getHolderMemberId();
    return paymentAccountReader
        .findByMemberId(holderMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getPayeeAccount(PaymentPayoutDto payout) {
    return switch (payout.payoutEventType()) {
      case FEE -> getSystemAccount();
      case AMOUNT -> getPaymentAccountByMemberId(payout.payeeId());
    };
  }

  public PaymentAccount getPaymentAccountByMemberIdForUpdate(Long memberId) {
    return paymentAccountReader
        .findByMemberIdWithLock(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public PaymentAccount getHolderAccountByMemberIdForUpdate() {
    Long holderMemberId = paymentAccountConfig.getHolderMemberId();
    return paymentAccountReader
        .findByMemberIdWithLock(holderMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));
  }

  public long countAccountLog() {
    return paymentAccountLogReader.countByReferenceType(ReferenceType.PAYMENT_MEMBER);
  }
}
