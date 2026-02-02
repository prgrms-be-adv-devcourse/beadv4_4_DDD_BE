package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthProviderTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginUseCase {

  private final OAuthClientFactory oauthClientFactory;
  private final OAuthAccountResolveUseCase oauthAccountResolveUseCase;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;
  private final StringRedisTemplate redisTemplate;
  private final MemberSupport memberSupport;

  public JwtTokenResponse execute(
      OAuthProvider provider, String code, String redirectUri, String state) {
    // 1. state 검증
    validateState(state, provider);

    // 2. OAuth 토큰 교환 (외부 HTTP 호출)
    OAuthClient oauthClient = oauthClientFactory.getClient(provider);
    OAuthProviderTokenResponse tokenResponse = oauthClient.getToken(code, redirectUri);

    // 3. 사용자 정보 조회 (외부 HTTP 호출)
    OAuthUserInfo userInfo = oauthClient.getUserInfo(tokenResponse.accessToken());
    log.info("OAuth 사용자 정보 조회 완료 - provider: {}, providerId: {}", provider, userInfo.providerId());

    // 4. 소셜 계정 조회 또는 신규 가입
    OAuthAccount socialAccount = oauthAccountResolveUseCase.execute(provider, userInfo);

    Member member = socialAccount.getMember();
    Long sellerId = memberSupport.getSellerIdByMemberId(member.getId());

    // 5. JWT 토큰 발급
    return authTokenIssueUseCase.execute(member.getId(), member.getRole(), sellerId);
  }

  private void validateState(String state, OAuthProvider provider) {
    String stateKey = "oauth:state:" + state;
    String savedProvider = redisTemplate.opsForValue().get(stateKey);

    if (savedProvider == null || !savedProvider.equals(provider.name())) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_STATE);
    }
    redisTemplate.delete(stateKey);
  }
}
