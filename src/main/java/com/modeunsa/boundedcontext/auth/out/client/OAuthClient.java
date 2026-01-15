package com.modeunsa.boundedcontext.auth.out.client;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;

public interface OAuthClient {

  // OAuth2 인가 URL 생성
  String generateOAuthUrl(String redirectUri);

  OAuthProvider getProvider();
}
