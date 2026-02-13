package com.modeunsa.boundedcontext.payment.domain.types;

import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public enum TossWebhookEventType {
  PAYMENT_STATUS_CHANGED("PAYMENT_STATUS_CHANGED");

  private final String value;

  public static TossWebhookEventType from(String raw) {
    if (!StringUtils.hasText(raw)) {
      throw new TossWebhookException(TossWebhookErrorCode.UNSUPPORTED_STATUS);
    }

    for (TossWebhookEventType type : values()) {
      if (type.value.equals(raw)) {
        return type;
      }
    }

    throw new TossWebhookException(TossWebhookErrorCode.UNSUPPORTED_STATUS);
  }
}
