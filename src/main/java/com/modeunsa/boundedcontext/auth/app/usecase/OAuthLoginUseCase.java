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
import java.time.Duration;
import java.util.function.Supplier;
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

    // 2. OAuth 토큰 교환
    OAuthClient oauthClient = oauthClientFactory.getClient(provider);
    OAuthProviderTokenResponse tokenResponse = oauthClient.getToken(code, redirectUri);

    // 3. 사용자 정보 조회
    OAuthUserInfo userInfo = oauthClient.getUserInfo(tokenResponse.accessToken());

    // --- [분산 락 적용] ---
    String lockKey = "lock:auth:" + provider + ":" + userInfo.providerId();
    // 3초 동안 락을 유지하며, 획득을 위해 최대 5초간 대기 (간단한 구현 예시)
    return executeWithLock(
        lockKey,
        () -> {
          // 4. 소셜 계정 조회 또는 신규 가입
          OAuthAccount socialAccount = oauthAccountResolveUseCase.execute(provider, userInfo);

          Member member = socialAccount.getMember();
          Long sellerId = memberSupport.getSellerIdByMemberId(member.getId());

          // 5. JWT 토큰 발급
          return authTokenIssueUseCase.execute(member.getId(), member.getRole(), sellerId);
        });
  }

  private JwtTokenResponse executeWithLock(String key, Supplier<JwtTokenResponse> supplier) {
    // setIfAbsent를 이용한 간단한 스핀 락 구조
    for (int i = 0; i < 10; i++) { // 최대 10번 시도
      Boolean acquired =
          redisTemplate.opsForValue().setIfAbsent(key, "lock", Duration.ofSeconds(3));
      if (Boolean.TRUE.equals(acquired)) {
        try {
          return supplier.get();
        } finally {
          redisTemplate.delete(key); // 락 해제
        }
      }
      try {
        Thread.sleep(200); // 200ms 대기 후 재시도
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    throw new GeneralException(ErrorStatus.INTERNAL_SERVER_ERROR); // 락 획득 실패 시 에러 처리
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
