package com.modeunsa.boundedcontext.payment.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PaymentMemberCreatedEvent {

  private final Long memberId;

  public PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId) {
    this.memberId = memberId;
  }
}
