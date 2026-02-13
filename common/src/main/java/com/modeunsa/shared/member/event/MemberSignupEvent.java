package com.modeunsa.shared.member.event;

public record MemberSignupEvent(
    Long memberId, String realName, String email, String phoneNumber, String role, String status) {}
