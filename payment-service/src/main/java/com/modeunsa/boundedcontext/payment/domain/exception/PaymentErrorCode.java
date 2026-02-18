package com.modeunsa.boundedcontext.payment.domain.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode {
  PAYMENT_MEMBER_IN_ACTIVE("PAYMENT_4001", "Member is not active."),
  DUPLICATE_PAYMENT("PAYMENT_4002", "Duplicate payment detected."),
  INVALID_PAYMENT("PAYMENT_4003", "Invalid payment details."),
  INVALID_CHARGE_AMOUNT("PAYMENT_4004", "Charge amount is invalid."),
  INVALID_PAYMENT_STATUS("PAYMENT_4005", "Payment status is invalid for this operation."),
  OVERDUE_PAYMENT_DEADLINE("PAYMENT_4006", "Payment deadline has been exceeded."),
  INVALID_PAYMENT_PURPOSE("PAYMENT_4007", "Invalid payment purpose."),
  PG_PAYMENT_ABORTED("PAYMENT_4008", "Payment was aborted by pg"),
  PG_PAYMENT_EXPIRED("PAYMENT_4009", "Payment expired by pg"),
  PG_INVALID_REQUEST("PAYMENT_4010", "Invalid request to pg."),
  PG_TOSS_CONFIRM_FAILED("PAYMENT_4010", "Toss payment confirm failed."),
  PG_TOSS_MAX_RETRY_EXCEEDED("PAYMENT_4011", "Toss payment confirm failed after max retries."),
  PG_UNKNOWN_ERROR("PAYMENT_4099", "Unknown error from pg.");

  private final String code;
  private final String message;

  private static final Set<PaymentErrorCode> FINAL_FAILURE_CODES =
      Set.of(
          OVERDUE_PAYMENT_DEADLINE,
          PG_PAYMENT_EXPIRED,
          PG_TOSS_CONFIRM_FAILED,
          PG_TOSS_MAX_RETRY_EXCEEDED,
          PG_UNKNOWN_ERROR);

  private static final Map<String, PaymentErrorCode> BY_CODE = new HashMap<>();

  static {
    for (PaymentErrorCode e : values()) {
      BY_CODE.putIfAbsent(e.getCode(), e);
    }
  }

  public static PaymentErrorCode fromCode(String code) {
    return Optional.ofNullable(code)
        .map(BY_CODE::get)
        .orElseThrow(() -> new IllegalArgumentException("Unknown PaymentErrorCode: " + code));
  }

  public boolean isFinalFailure() {
    return FINAL_FAILURE_CODES.contains(this);
  }
}
