package com.modeunsa.boundedcontext.payment.domain.validator;

import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookException;
import com.modeunsa.boundedcontext.payment.domain.types.TossWebhookEventType;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TossWebhookValidator {

  private static final int MAX_RETRY_COUNT = 3;

  public boolean validate(
      String transmissionId, OffsetDateTime transmissionTime, int retryCount, String rawEventType) {

    if (retryCount > MAX_RETRY_COUNT) {
      log.error(
          "[toss webhook exceed retry count] "
              + "transmissionId: {}, "
              + "transmissionTime: {}, "
              + "retryCount: {}, "
              + "eventType: {}",
          transmissionId,
          transmissionTime,
          retryCount,
          rawEventType);
      return false;
    }

    TossWebhookEventType eventType = TossWebhookEventType.from(rawEventType);
    if (eventType != TossWebhookEventType.PAYMENT_STATUS_CHANGED) {
      throw new TossWebhookException(TossWebhookErrorCode.INVALID_EVENT_TYPE);
    }

    return true;
  }
}
