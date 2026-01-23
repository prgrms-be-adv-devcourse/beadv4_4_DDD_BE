package com.modeunsa.shared.member.event;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;

public record MemberSignupEvent(
    Long memberId,
    String realName,
    String email,
    String phoneNumber,
    MemberRole role, // TODO: enum들이 member와의 분리가 아직 안 되어있습니다.
    MemberStatus status) {}
