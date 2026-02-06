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
import java.util.UUID; // 중복 해제 방지용
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

  // 락 관련 상수 설정
  private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10); // 3초에서 10초로 연장
  private static final int MAX_RETRY_COUNT = 15; // 재시도 횟수 증가
  private static final long RETRY_DELAY_MS = 300; // 재시도 간격 조정

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
    // 락의 소유권을 식별하기 위한 고유 값 (다른 프로세스가 내 락을 해제하는 것 방지)
    String lockValue = UUID.randomUUID().toString();

    for (int i = 0; i < MAX_RETRY_COUNT; i++) {
      Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, lockValue, LOCK_TIMEOUT);

      if (Boolean.TRUE.equals(acquired)) {
        try {
          return supplier.get();
        } finally {
          // 내가 획득한 락인지 확인 후 해제 (안정성 강화)
          String currentValue = redisTemplate.opsForValue().get(key);
          if (lockValue.equals(currentValue)) {
            redisTemplate.delete(key);
          }
        }
      }

      try {
        Thread.sleep(RETRY_DELAY_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Social login lock acquisition failed for key: {}. System is busy.", key);
        throw new GeneralException(ErrorStatus.AUTH_TOO_MANY_REQUESTS);
      }
    }
    log.error("Failed to acquire lock for key: {}", key);
    throw new GeneralException(ErrorStatus.AUTH_TOO_MANY_REQUESTS);
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
