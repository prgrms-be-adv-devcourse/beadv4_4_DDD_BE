package com.modeunsa.shared.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
  MEMBER("회원"),
  SELLER("판매자"),
  ADMIN("관리자"),
  HOLDER("홀더"),
  SYSTEM("시스템");
  private final String description;
}
