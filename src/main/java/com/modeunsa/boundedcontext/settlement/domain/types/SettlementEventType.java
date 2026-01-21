package com.modeunsa.boundedcontext.settlement.domain.types;

import lombok.Getter;

@Getter
public enum SettlementEventType {
  SETTLEMENT_PRODUCT_SALES_FEE("정산_상품판매_수수료", "FEE"),
  SETTLEMENT_PRODUCT_SALES_AMOUNT("정산_상품판매_대금", "AMOUNT");

  private final String description;
  private final String completeType;

  SettlementEventType(String description, String completeType) {
    this.description = description;
    this.completeType = completeType;
  }
}
