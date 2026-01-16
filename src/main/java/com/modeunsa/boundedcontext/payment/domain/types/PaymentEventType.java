package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PaymentEventType {
  CHARGE_BANK_TRANSFER("충전 무통장입금"),
  CHARGE_PG_TOSS_PAYMENTS("충전 PG결제_토스페이먼츠"),
  USE_ORDER_PAYMENT("사용 주문결제"),
  HOLD_STORE_ORDER_PAYMENT("임시보관 주문결제"),
  REFUND_PAYMENT_FAILED("환불 결제실패"),
  REFUND_ORDER_CANCELLED("환불 주문취소"),
  SETTLEMENT_PAY_PRODUCT_SALES_FEE("정산지급 상품판매 수수료"),
  SETTLEMENT_ADJUST_PRODUCT_SALES_FEE("정산수정 상품판매 수수료"),
  SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT("정산지급 상품판매 대금"),
  SETTLEMENT_RECEIVE_PRODUCT_SALES_AMOUNT("정산수령 상품판매 대금");

  private final String description;

  PaymentEventType(String description) {
    this.description = description;
  }

  public static PaymentEventType fromPayoutEventType(PayoutEventType payoutEventType) {
    return switch (payoutEventType) {
      case FEE -> SETTLEMENT_PAY_PRODUCT_SALES_FEE;
      case AMOUNT -> SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT;
    };
  }

  public static PaymentEventType fromRefundEventType(RefundEventType refundEventType) {
    return switch (refundEventType) {
      case PAYMENT_FAILED -> REFUND_PAYMENT_FAILED;
      case ORDER_CANCELLED -> REFUND_ORDER_CANCELLED;
    };
  }
}
