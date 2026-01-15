package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum ReferenceType {
  PAYMENT_MEMBER("결제_회원"),
  PAYMENT("결제"),
  ORDER("주문"),
  PAYOUT("정산");

  private final String description;

  ReferenceType(String description) {
    this.description = description;
  }
}
