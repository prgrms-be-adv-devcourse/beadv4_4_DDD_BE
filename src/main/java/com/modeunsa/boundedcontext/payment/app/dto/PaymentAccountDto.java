package com.modeunsa.boundedcontext.payment.app.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Getter
@RequiredArgsConstructor
public class PaymentAccountDto {
  private final Long id;

  private final long balance;

  private final PaymentAccountDto paymentAccount;
}
