package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;

@Getter
public enum PaymentErrorCode {
  MEMBER_IN_ACTIVE("회원 ID %d 는 현재 주문이 불가능한 상태입니다. 현재 상태: %s"),
  INSUFFICIENT_BALANCE("잔액이 부족합니다."),
  DUPLICATE_PAYMENT("중복 결제 요청입니다. 회원 ID: %d, 주문 번호: %s"),
  INVALID_PAYMENT("유효하지 않은 결제 요청입니다.");

  private final String messageTemplate;

  PaymentErrorCode(String messageTemplate) {
    this.messageTemplate = messageTemplate;
  }

  public String format(Object... args) {
    return String.format(this.messageTemplate, args);
  }
}
