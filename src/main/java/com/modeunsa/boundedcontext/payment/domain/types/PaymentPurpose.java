package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PaymentPurpose {
  DEPOSIT_CHARGE("예치금 충전"),
  PRODUCT_PURCHASE("상품 결제");

  private final String description;

  PaymentPurpose(String description) {
    this.description = description;
  }
}
