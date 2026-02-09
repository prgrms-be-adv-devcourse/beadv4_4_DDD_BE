package com.modeunsa.boundedcontext.payment.app.dto.account;

import java.math.BigDecimal;

public record PaymentAccountDto(Long memberId, BigDecimal balance) {

  public static PaymentAccountDto of(Long memberId, BigDecimal balance) {
    return new PaymentAccountDto(memberId, balance);
  }
}
