package com.modeunsa.shared.member.event;

public record MemberBasicInfoUpdatedEvent(
    Long memberId, String realName, String email, String phoneNumber) {}
