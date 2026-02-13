package com.modeunsa.boundedcontext.payment.domain.exception;

import java.util.Set;
import lombok.Getter;

@Getter
public enum PaymentErrorCode {
  MEMBER_IN_ACTIVE,
  DUPLICATE_PAYMENT,
  INVALID_PAYMENT,
  INVALID_CHARGE_AMOUNT,
  INVALID_PAYMENT_STATUS,
  OVERDUE_PAYMENT_DEADLINE,
  INVALID_PAYMENT_PURPOSE;

  private static final Set<PaymentErrorCode> FINAL_FAILURE_CODES = Set.of(OVERDUE_PAYMENT_DEADLINE);

  public boolean isFinalFailure() {
    return FINAL_FAILURE_CODES.contains(this);
  }
}
