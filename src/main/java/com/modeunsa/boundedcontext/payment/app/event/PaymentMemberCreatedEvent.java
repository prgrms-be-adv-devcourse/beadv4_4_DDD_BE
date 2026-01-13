package com.modeunsa.boundedcontext.payment.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import lombok.Getter;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Getter
public class PaymentMemberCreatedEvent {

  private final PaymentMemberDto member;

  public PaymentMemberCreatedEvent(@JsonProperty("member") PaymentMemberDto member) {
    this.member = member;
  }
}
