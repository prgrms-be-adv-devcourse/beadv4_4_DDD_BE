package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum RefundEventType {
  PAYMENT_FAILED("결제_실패"),
  ORDER_CANCELLED("주문_취소");

  private final String description;

  RefundEventType(String description) {
    this.description = description;
  }
}
