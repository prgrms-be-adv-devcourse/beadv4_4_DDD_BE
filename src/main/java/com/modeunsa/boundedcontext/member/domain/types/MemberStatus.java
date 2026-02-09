package com.modeunsa.boundedcontext.member.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberStatus {
  PRE_ACTIVE("가입 대기"), // 소셜 로그인만 완료, 필수 정보 미입력
  ACTIVE("활성"), // 필수 정보 입력 완료 → 정상 회원
  SUSPENDED("정지"), // 정책 위반으로 인한 정지
  WITHDRAWN_PENDING("탈퇴 대기"), // 탈퇴 요청한 회원은 우선 탈퇴 대기
  WITHDRAWN("탈퇴"); // 일정 기간 이후 탈퇴 (TODO: 탈퇴 기능 구현)

  private final String description;
}
