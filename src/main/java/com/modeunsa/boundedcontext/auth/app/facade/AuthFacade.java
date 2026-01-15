package com.modeunsa.boundedcontext.auth.app.facade;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenIssueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenReissueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthUrlUseCase;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.shared.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacade {

  private final OAuthUrlUseCase oauthUrlUseCase;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;
  private final AuthTokenReissueUseCase authTokenReissueUseCase;

  /**
   * OAuth2 로그인 URL 생성
   */
  public String getOAuthLoginUrl(OAuthProvider provider, String redirectUri) {
    return oauthUrlUseCase.generateOAuthUrl(provider, redirectUri);
  }

  /**
   * 로그인 성공 후 토큰 발급
   */
  public TokenResponse login(Long memberId, MemberRole role) {
    return authTokenIssueUseCase.execute(memberId, role);
  }

  /**
   * 토큰 재발급
   */
  public TokenResponse reissueToken(String refreshToken) {
    return authTokenReissueUseCase.execute(refreshToken);
  }
}