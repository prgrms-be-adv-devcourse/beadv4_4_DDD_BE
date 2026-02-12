package com.modeunsa.boundedcontext.auth.app.facade;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthLogoutUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenIssueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenReissueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.AuthUpdateMemberRoleUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthLoginUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthUrlUseCase;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.member.dto.response.MemberRoleUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthFacade {

  private final OAuthUrlUseCase oauthUrlUseCase;
  private final OAuthLoginUseCase oauthLoginUseCase;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;
  private final AuthTokenReissueUseCase authTokenReissueUseCase;
  private final AuthLogoutUseCase authLogoutUseCase;
  private final AuthUpdateMemberRoleUseCase authUpdateMemberRoleUseCase;

  /** OAuth2 로그인 URL 생성 */
  @Transactional
  public String getOAuthLoginUrl(OAuthProvider provider, String redirectUri) {
    return oauthUrlUseCase.generateOAuthUrl(provider, redirectUri);
  }

  /** OAuth 로그인/회원가입 처리 */
  @Transactional
  public JwtTokenResponse oauthLogin(
      OAuthProvider provider, String code, String redirectUri, String state) {
    return oauthLoginUseCase.execute(provider, code, redirectUri, state);
  }

  /** 로그인 성공 후 토큰 발급 */
  @Transactional
  public JwtTokenResponse login(Long memberId, MemberRole role, Long sellerId, String status) {
    return authTokenIssueUseCase.execute(memberId, role, sellerId, status);
  }

  /** 토큰 재발급 */
  @Transactional
  public JwtTokenResponse reissueToken(String refreshToken) {
    return authTokenReissueUseCase.execute(refreshToken);
  }

  /** 로그아웃 */
  @Transactional
  public void logout(String accessToken) {
    authLogoutUseCase.execute(accessToken);
  }

  /** role 변경 및 토큰 재발급 */
  @Transactional
  public MemberRoleUpdateResponse updateMemberRole(Long memberId, MemberRole newRole) {
    return authUpdateMemberRoleUseCase.execute(memberId, newRole);
  }
}
