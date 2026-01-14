package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class PaymentAccountDepositResponse {
  private final BigDecimal balance;

  public PaymentAccountDepositResponse(BigDecimal balance) {
    this.balance = balance;
  }
}
