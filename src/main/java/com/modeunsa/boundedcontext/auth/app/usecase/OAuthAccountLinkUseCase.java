package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.OAuthTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthAccountLinkUseCase {

  private final OAuthClientFactory oauthClientFactory;
  private final StringRedisTemplate redisTemplate;
  private final OAuthAccountBindingUseCase oauthAccountBindingUseCase;

  /**
   * 실행 메서드
   * 1. 검증 및 외부 API 호출 (Non-Transactional)
   * 2. DB 저장 로직 위임 (Transactional)
   */
  public void execute(Long memberId, OAuthProvider provider, String code, String redirectUri, String state) {
    // 1. state 검증
    validateState(state, provider);

    // 2. OAuth 토큰 교환 및 사용자 정보 조회 (외부 API 호출)
    OAuthClient oauthClient = oauthClientFactory.getClient(provider);
    OAuthTokenResponse tokenResponse = oauthClient.getToken(code, redirectUri);
    OAuthUserInfo userInfo = oauthClient.getUserInfo(tokenResponse.getAccessToken());

    // 3. DB 작업 및 검증 (별도 서비스 호출 -> 트랜잭션 적용됨)
    oauthAccountBindingUseCase.link(memberId, provider, userInfo);
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