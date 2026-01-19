package com.modeunsa.boundedcontext.payment.app.dto.member;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentMemberResponse {

  private final String customerKey;

  private final String customerName;

  private final String customerEmail;

  private final BigDecimal balance;
}
