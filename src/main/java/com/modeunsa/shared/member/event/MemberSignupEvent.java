package com.modeunsa.shared.member.event;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;

public record MemberSignupEvent(
    Long memberId,
    String realName,
    String email,
    String phoneNumber,
    String role,
    String status) {}
