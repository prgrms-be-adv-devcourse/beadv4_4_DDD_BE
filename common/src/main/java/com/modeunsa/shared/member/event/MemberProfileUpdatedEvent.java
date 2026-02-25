package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record MemberProfileUpdatedEvent(
    Long memberId,
    Long profileId,
    String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "MemberProfileUpdatedEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
