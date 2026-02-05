package com.modeunsa.boundedcontext.payment.app.dto.accountlog;

import com.modeunsa.boundedcontext.payment.app.dto.PageableRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.time.LocalDateTime;

public record PaymentAccountSearchRequest(
    int page, int size, LocalDateTime from, LocalDateTime to, PaymentEventType type)
    implements PageableRequest {

  public PaymentAccountSearchRequest {
    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("from must be before to");
    }
  }
}
