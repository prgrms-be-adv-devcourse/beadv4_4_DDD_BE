package com.modeunsa.boundedcontext.member.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
  ACTIVE("활성"),
  SUSPENDED("정지"),
  WITHDRAWN_PENDING("탈퇴 대기"),
  WITHDRAWN("탈퇴");

  private final String description;
}
