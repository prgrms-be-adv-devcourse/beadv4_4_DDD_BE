package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.dto.JwtTokenResponse;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthProviderTokenResponse;
import com.modeunsa.boundedcontext.auth.domain.dto.OAuthUserInfo;
import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.UUID;
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

    // 요청 ID 생성 (각 요청 구분용)
    String requestId = UUID.randomUUID().toString().substring(0, 8);

    log.debug("[{}] OAuth 로그인 시작 - provider: {}, state: {}", requestId, provider, state);

    try {
      // 1. state 검증
      validateState(state, provider);
      log.debug("[{}] State 검증 완료", requestId);

      // 2. OAuth 토큰 교환
      OAuthClient oauthClient = oauthClientFactory.getClient(provider);
      OAuthProviderTokenResponse tokenResponse = oauthClient.getToken(code, redirectUri);
      log.debug("[{}] OAuth 토큰 교환 완료", requestId);

      // 3. 사용자 정보 조회
      OAuthUserInfo userInfo = oauthClient.getUserInfo(tokenResponse.accessToken());
      log.debug("[{}] 사용자 정보 조회 완료 - providerId: {}", requestId, userInfo.providerId());

      // 4. 소셜 계정 조회 또는 신규 가입 (이제 신규가입 시 PRE_ACTIVE가 됨)
      log.debug("[{}] 소셜 계정 처리 시작", requestId);
      OAuthAccount socialAccount = oauthAccountResolveUseCase.execute(provider, userInfo);
      log.debug(
          "[{}] 소셜 계정 처리 완료 - accountId: {}, memberId: {}",
          requestId,
          socialAccount.getId(),
          socialAccount.getMember().getId());

      Member member = socialAccount.getMember();
      Long sellerId = memberSupport.getSellerIdByMemberId(member.getId());

      // 5. JWT 토큰 발급
      JwtTokenResponse jwtTokenResponse =
          authTokenIssueUseCase.execute(
              member.getId(), member.getRole(), sellerId, member.getStatus().name());

      log.debug("[{}] OAuth 로그인 성공 - memberId: {}", requestId, member.getId());
      return jwtTokenResponse;

    } catch (Exception e) {
      log.error(
          "[{}] OAuth 로그인 실패 - error: {}, message: {}",
          requestId,
          e.getClass().getSimpleName(),
          e.getMessage());
      throw e;
    }
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
