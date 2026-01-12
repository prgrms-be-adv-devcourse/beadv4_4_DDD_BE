package com.modeunsa.boundedcontext.member.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerStatus {
  PENDING("승인 대기"),
  ACTIVE("활성"),
  REJECTED("거절"),
  SUSPENDED("정지");

  private final String description;
}
