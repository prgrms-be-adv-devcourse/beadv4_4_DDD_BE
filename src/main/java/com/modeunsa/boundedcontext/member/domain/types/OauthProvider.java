package com.modeunsa.boundedcontext.member.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthProvider {
  KAKAO("카카오"),
  NAVER("네이버");

  private final String description;
}
