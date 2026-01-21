package com.modeunsa.shared.auth.event;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;

public record MemberSignupEvent(
    Long memberId,
    String realName,
    String email,
    String phoneNumber,
    MemberRole role,
    MemberStatus status) {}
