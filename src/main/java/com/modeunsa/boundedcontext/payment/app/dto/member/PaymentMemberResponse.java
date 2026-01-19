package com.modeunsa.boundedcontext.payment.app.dto.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentMemberResponse {

  private final String customerKey;

  private final String customerName;

  private final String customerEmail;
}
