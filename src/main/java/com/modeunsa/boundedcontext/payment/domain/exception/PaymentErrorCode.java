package com.modeunsa.boundedcontext.payment.domain.exception;

import java.util.Set;
import lombok.Getter;

@Getter
public enum PaymentErrorCode {
  MEMBER_IN_ACTIVE("회원 ID %d 는 현재 주문이 불가능한 상태입니다. 현재 상태: %s"),
  INSUFFICIENT_BALANCE("잔액이 부족합니다. 입력된 금액: %s"),
  DUPLICATE_PAYMENT("중복 결제 요청입니다. 회원 ID: %d, 주문 번호: %s"),
  INVALID_PAYMENT("유효하지 않은 결제 요청입니다. 회원 ID: %d, 주문 번호: %s"),
  INVALID_CHARGE_AMOUNT("부족한 금액과 PG 결제 금액이 다릅니다. 회원 ID: %d, 주문 번호: %s, 부족 금액: %s, PG 요청 금액: %s"),
  INVALID_PAYMENT_STATUS("유효한 결제 상태가 아닙니다. 회원 ID: %d, 주문 번호: %s, 현재 상태: %s, 변경 요청 상태: %s"),
  OVERDUE_PAYMENT_DEADLINE("결제 유효기간이 만료되었습니다. 회원 ID: %d, 주문 번호: %s, 결제 마감일: %s");

  private static final Set<PaymentErrorCode> FINAL_FAILURE_CODES = Set.of(OVERDUE_PAYMENT_DEADLINE);

  private final String messageTemplate;

  PaymentErrorCode(String messageTemplate) {
    this.messageTemplate = messageTemplate;
  }

  public String format(Object... args) {
    return String.format(this.messageTemplate, args);
  }

  public boolean isFinalFailure() {
    return FINAL_FAILURE_CODES.contains(this);
  }
}
