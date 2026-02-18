package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PaymentFailureReason {
  PAYMENT_MEMBER_IN_ACTIVE("결제 회원이 비활성 상태입니다."),
  PAYMENT_DUPLICATE_REQUEST("중복 결제 요청입니다."),
  PAYMENT_INSUFFICIENT_FUNDS("잔액이 부족합니다.");

  private final String description;

  PaymentFailureReason(String description) {
    this.description = description;
  }
}
