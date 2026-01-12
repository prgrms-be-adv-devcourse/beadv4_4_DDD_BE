package com.modeunsa.boundedcontext.payment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 12.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PaymentEventType {
  CHARGE_BANK_TRANSFER("충전_무통장입금"),
  CHARGE_PG_TOSS_PAYMENTS("충전_PG결제_토스페이먼츠"),
  WITHDRAW_ACCOUNT_TRANSFER("출금_통장입금"),
  USE_ORDER_PAYMENT("사용_주문결제"),
  TEMP_STORE_ORDER_PAYMENT("임시보관_주문결제"),
  SETTLEMENT_PAY_PRODUCT_SALES_FEE("정산지급_상품판매_수수료"),
  SETTLEMENT_ADJUST_PRODUCT_SALES_FEE("정산수정_상품판매_수수료"),
  SETTLEMENT_PAY_PRODUCT_SALES_AMOUNT("정산지급_상품판매_대금"),
  SETTLEMENT_RECEIVE_PRODUCT_SALES_AMOUNT("정산수령_상품판매_대금");

  private String description;
}
