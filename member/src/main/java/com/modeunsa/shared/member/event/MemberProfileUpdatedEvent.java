package com.modeunsa.shared.member.event;

public record MemberProfileUpdatedEvent(
    Long memberId,
    Long profileId,
    String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType) {}
