package com.modeunsa.boundedcontext.payment.domain.types;

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
public enum PaymentStatus {
  READY("결제_준비"),
  IN_PROGRESS("결제_진행"),
  APPROVED("결제_승인"),
  CANCELED("결제_취소"),
  FAILED("결제_실패"),
  COMPLETED("결제_완료"),
  REFUND_REQUESTED("환불_요청"),
  REFUNDED("환불_완료");

  private String description;
}
