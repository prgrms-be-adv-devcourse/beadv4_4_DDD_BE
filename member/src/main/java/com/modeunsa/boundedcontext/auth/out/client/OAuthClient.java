package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthProviderTokenResponse;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthUserInfo;

public interface OAuthClient {

  // OAuth2 인가 URL 생성
  String generateOAuthUrl(String redirectUri);

  // 제공자 정보 반환
  OAuthProvider getProvider();

  // 인가 코드로 토큰 교환
  OAuthProviderTokenResponse getToken(String code, String redirectUri);

  // 토큰으로 사용자 정보 조회
  OAuthUserInfo getUserInfo(String accessToken);
}
