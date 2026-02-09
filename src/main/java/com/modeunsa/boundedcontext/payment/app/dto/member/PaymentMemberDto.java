package com.modeunsa.boundedcontext.payment.app.dto.member;

import java.math.BigDecimal;

public record PaymentMemberDto(
    String customerKey, String customerName, String customerEmail, BigDecimal balance) {

  public static PaymentMemberDto memberInfoWithBalance(
      String customerKey, String customerName, String customerEmail, BigDecimal balance) {
    return new PaymentMemberDto(customerKey, customerName, customerEmail, balance);
  }
}
