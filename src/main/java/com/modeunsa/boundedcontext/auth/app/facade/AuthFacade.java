package com.modeunsa.boundedcontext.auth.app.facade;

import com.modeunsa.boundedcontext.auth.app.usecase.OAuthUrlUseCase;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

  private final OAuthUrlUseCase oauthUrlUseCase;

  /**
   * OAuth2 로그인 URL 생성
   */
  public String getOAuthLoginUrl(OAuthProvider provider, String redirectUri) {
    return oauthUrlUseCase.generateOAuthUrl(provider, redirectUri);
  }
}