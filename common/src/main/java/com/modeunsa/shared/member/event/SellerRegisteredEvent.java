package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record SellerRegisteredEvent(
    Long memberId,
    Long memberSellerId,
    String businessName,
    String representativeName,
    String settlementBankName,
    String settlementBankAccount,
    String status,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "SellerRegisteredEvent";

  public SellerRegisteredEvent(
      Long memberId,
      Long memberSellerId,
      String businessName,
      String representativeName,
      String settlementBankName,
      String settlementBankAccount,
      String status) {

    this(
        memberId,
        memberSellerId,
        businessName,
        representativeName,
        settlementBankName,
        settlementBankAccount,
        status,
        EventUtils.extractTraceId()
    );
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
