package com.modeunsa.boundedcontext.settlement.domain;

public enum SettlementEventType {
  SETTLEMENT_PRODUCT_SALES_FEE("정산_상품판매_수수료"),
  SETTLEMENT_PRODUCT_SALES_AMOUNT("정산_상품판매_대금");

  private final String description;

  SettlementEventType(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }
}
