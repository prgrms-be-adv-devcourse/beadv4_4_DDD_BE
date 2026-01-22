package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.Getter;

@Getter
public enum PaymentStatus {
  PENDING("결제 대기"),
  READY("결제 준비"),
  IN_PROGRESS("결제 진행"),
  APPROVED("결제 승인"),
  CANCELED("결제 취소"),
  FAILED("결제 실패"),
  COMPLETED("결제 완료"),
  REFUND_REQUESTED("환불 요청"),
  REFUNDED("환불 완료");

  private final String description;

  PaymentStatus(String description) {
    this.description = description;
  }
}
