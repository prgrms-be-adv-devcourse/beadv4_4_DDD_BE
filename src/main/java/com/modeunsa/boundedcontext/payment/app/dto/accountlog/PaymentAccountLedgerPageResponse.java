package com.modeunsa.boundedcontext.payment.app.dto.accountlog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentAccountLedgerPageResponse(
    boolean isDeposit, String content, BigDecimal balance, LocalDateTime createdAt) {
  public static PaymentAccountLedgerPageResponse from(PaymentAccountLogDto dto) {
    boolean isDeposit = dto.getAmount().compareTo(BigDecimal.ZERO) > 0;
    return new PaymentAccountLedgerPageResponse(
        isDeposit, dto.getEventType().getDescription(), dto.getAmount(), dto.getCreatedAt());
  }
}
