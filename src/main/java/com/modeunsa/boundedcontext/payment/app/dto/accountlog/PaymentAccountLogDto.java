package com.modeunsa.boundedcontext.payment.app.dto.accountlog;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentAccountLog;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentAccountLogDto {

  private final Long id;

  private final PaymentEventType eventType;

  private final BigDecimal amount;

  private final BigDecimal balanceAfter;

  private final LocalDateTime createdAt;

  public PaymentAccountLogDto(PaymentAccountLog paymentAccountLog) {
    this.id = paymentAccountLog.getId();
    this.eventType = paymentAccountLog.getEventType();
    this.amount = paymentAccountLog.getAmount();
    this.balanceAfter = paymentAccountLog.getBalanceAfter();
    this.createdAt = paymentAccountLog.getCreatedAt();
  }
}
