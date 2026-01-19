package com.modeunsa.boundedcontext.auth.app.facade;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthLogoutUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenIssueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenReissueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthUrlUseCase;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.shared.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {

  private final OAuthUrlUseCase oauthUrlUseCase;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;
  private final AuthTokenReissueUseCase authTokenReissueUseCase;
  private final AuthLogoutUseCase authLogoutUseCase;

  /** OAuth2 로그인 URL 생성 */
  @Transactional
  public String getOAuthLoginUrl(OAuthProvider provider, String redirectUri) {
    return oauthUrlUseCase.generateOAuthUrl(provider, redirectUri);
  }

  /** 로그인 성공 후 토큰 발급 */
  @Transactional
  public TokenResponse login(Long memberId, MemberRole role) {
    return authTokenIssueUseCase.execute(memberId, role);
  }

  /** 토큰 재발급 */
  @Transactional
  public TokenResponse reissueToken(String refreshToken) {
    return authTokenReissueUseCase.execute(refreshToken);
  }

  /** 로그아웃 */
  @Transactional
  public void logout(String accessToken) {
    authLogoutUseCase.execute(accessToken);
  }
}
