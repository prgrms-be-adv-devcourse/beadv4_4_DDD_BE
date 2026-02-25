package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record MemberProfileCreatedEvent(
    Long memberId,
    Long profileId,
    String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType,
    String traceId)
    implements TraceableEvent {

  private static final String EVENT_NAME = "MemberProfileCreatedEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
