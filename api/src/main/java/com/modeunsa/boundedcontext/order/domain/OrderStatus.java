package com.modeunsa.boundedcontext.order.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
  PENDING_PAYMENT("결제 대기"), // 주문서 생성
  PAYMENT_FAILED("결제 실패"),
  PAID("결제 완료"), // 상품 준비 중
  SHIPPING("배송 중"),
  DELIVERED("배송 완료"),
  PURCHASE_CONFIRMED("구매 확정"), // 정산 가능

  // 주문 취소 요청(배송 전)
  CANCEL_REQUESTED("취소 요청됨"), // PG사 환불 대기 중인 상태

  // 환불 요청(배송 후)
  REFUND_REQUESTED("환불 요청됨"),

  // 환불까지 완료
  CANCELLED("취소 완료");

  private final String description;
}
