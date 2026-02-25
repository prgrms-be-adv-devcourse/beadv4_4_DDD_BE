package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record MemberBasicInfoUpdatedEvent(
    Long memberId, String realName, String email, String phoneNumber, String traceId)
    implements TraceableEvent {

  private static final String EVENT_NAME = "MemberBasicInfoUpdatedEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
