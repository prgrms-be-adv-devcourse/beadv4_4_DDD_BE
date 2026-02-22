package com.modeunsa.shared.member.event;

public record MemberProfileCreatedEvent(
    Long memberId,
    Long profileId,
    String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType) {}
