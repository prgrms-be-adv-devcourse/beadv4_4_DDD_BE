package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record MemberSignupEvent(
    Long memberId,
    String realName,
    String email,
    String phoneNumber,
    String role,
    String status,
    String traceId)
    implements TraceableEvent {

  private static final String EVENT_NAME = "MemberSignupEvent";

  public MemberSignupEvent(
      Long memberId,
      String realName,
      String email,
      String phoneNumber,
      String role,
      String status) {
    this(memberId, realName, email, phoneNumber, role, status, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
