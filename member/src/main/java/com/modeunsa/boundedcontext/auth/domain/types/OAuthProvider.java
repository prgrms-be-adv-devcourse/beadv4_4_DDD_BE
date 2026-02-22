package com.modeunsa.boundedcontext.auth.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {
  KAKAO("카카오"),
  NAVER("네이버");

  private final String description;
}
