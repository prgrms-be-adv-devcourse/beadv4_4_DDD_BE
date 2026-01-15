package com.modeunsa.boundedcontext.payment.domain.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PayoutEventType {
  FEE("수수료"),
  AMOUNT("판매대금");

  private String description;
}
