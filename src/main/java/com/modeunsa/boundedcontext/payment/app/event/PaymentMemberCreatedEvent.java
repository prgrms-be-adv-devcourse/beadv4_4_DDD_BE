package com.modeunsa.boundedcontext.payment.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modeunsa.shared.event.BaseEvent;
import lombok.Getter;

@Getter
public class PaymentMemberCreatedEvent extends BaseEvent {

  private final Long memberId;

  public PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId) {
    super();
    this.memberId = memberId;
  }
}
