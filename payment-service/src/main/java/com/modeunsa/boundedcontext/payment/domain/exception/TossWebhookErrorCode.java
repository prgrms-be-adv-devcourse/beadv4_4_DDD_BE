package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossWebhookErrorCode {
  UNSUPPORTED_STATUS("PAYMENT_WEBHOOK_4001", "유효하지 않은 이벤트 타입입니다."),
  NOT_FOUND_WEBHOOK_EVENT("PAYMENT_WEBHOOK_4401", "존재하지 않는 웹훅 이벤트입니다.");

  private final String code;
  private final String message;
}
