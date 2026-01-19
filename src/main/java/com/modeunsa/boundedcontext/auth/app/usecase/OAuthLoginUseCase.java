package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthSocialAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.boundedcontext.auth.out.repository.AuthSocialAccountRepository;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.OAuthTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import com.modeunsa.shared.auth.dto.TokenResponse;
import com.modeunsa.shared.auth.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginUseCase {

  private final OAuthClientFactory oauthClientFactory;
  private final AuthSocialAccountRepository socialAccountRepository;
  private final MemberRepository memberRepository;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;
  private final ApplicationEventPublisher eventPublisher;
  private final StringRedisTemplate redisTemplate;

  @Transactional
  public TokenResponse execute(
      OAuthProvider provider, String code, String redirectUri, String state) {
    // state 검증
    String stateKey = "oauth:state:" + state;
    String savedProvider = redisTemplate.opsForValue().get(stateKey);

    if (savedProvider == null || !savedProvider.equals(provider.name())) {
      throw new GeneralException(ErrorStatus.OAUTH_INVALID_STATE);
    }
    redisTemplate.delete(stateKey);

    // 1. OAuth 토큰 교환
    OAuthClient oauthClient = oauthClientFactory.getClient(provider);
    OAuthTokenResponse tokenResponse = oauthClient.getToken(code, redirectUri);

    // 2. 사용자 정보 조회
    OAuthUserInfo userInfo = oauthClient.getUserInfo(tokenResponse.getAccessToken());
    log.info(
        "OAuth 사용자 정보 조회 완료 - provider: {}, providerId: {}", provider, userInfo.getProviderId());

    // 3. 소셜 계정 조회 또는 신규 가입
    AuthSocialAccount socialAccount =
        socialAccountRepository
            .findByOauthProviderAndProviderAccountId(provider, userInfo.getProviderId())
            .orElseGet(() -> registerNewMember(userInfo));

    Member member = socialAccount.getMember();

    // 4. JWT 토큰 발급
    return authTokenIssueUseCase.execute(member.getId(), member.getRole());
  }

  private AuthSocialAccount registerNewMember(OAuthUserInfo userInfo) {
    log.info(
        "신규 회원 가입 - provider: {}, providerId: {}",
        userInfo.getProvider(),
        userInfo.getProviderId());

    // 1. Member 생성
    Member member =
        Member.builder()
            .email(userInfo.getEmail())
            .realName(userInfo.getName())
            .phoneNumber(userInfo.getPhoneNumber())
            .build();

    memberRepository.save(member);

    // 2. 소셜 계정 연동
    AuthSocialAccount socialAccount =
        AuthSocialAccount.builder()
            .member(member)
            .oauthProvider(userInfo.getProvider())
            .providerAccountId(userInfo.getProviderId())
            .build();

    socialAccountRepository.save(socialAccount);

    // 3. 회원가입 이벤트 발행
    eventPublisher.publishEvent(
        MemberSignupEvent.of(member.getId(), userInfo.getEmail(), userInfo.getProvider()));

    return socialAccount;
  }
}
