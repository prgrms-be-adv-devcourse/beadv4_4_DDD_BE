package com.modeunsa.boundedcontext.payment.app.dto.member;

import java.math.BigDecimal;

public record PaymentMemberDto(
    String customerKey, String customerEmail, String customerName, BigDecimal balance) {

  public static PaymentMemberDto of(
      String customerKey, String email, String name, BigDecimal balance) {
    return new PaymentMemberDto(customerKey, email, name, balance);
  }
}
