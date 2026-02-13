package com.modeunsa.boundedcontext.payment.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TossWebhookErrorCode {
  INVALID_EVENT_TYPE("유효하지 않은 이벤트 타입입니다.");

  private final String message;
}
