package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RefundEventType {
  PAYMENT_FAILED("결제 실패"),
  ORDER_CANCELLED("주문 취소");

  private String description;
}
