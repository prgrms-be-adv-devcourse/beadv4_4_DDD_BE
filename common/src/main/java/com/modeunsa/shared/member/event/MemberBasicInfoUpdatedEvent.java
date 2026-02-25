package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record MemberBasicInfoUpdatedEvent(
    Long memberId, String realName, String phoneNumber, String email, String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "MemberBasicInfoUpdatedEvent";

  public MemberBasicInfoUpdatedEvent(
      Long memberId,
      String realName,
      String email,
      String phoneNumber) {

    this(
        memberId,
        realName,
        email,
        phoneNumber,
        EventUtils.extractTraceId()
    );
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
