package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenIssueUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthAccountResolveUseCase;
import com.modeunsa.boundedcontext.auth.app.usecase.OAuthLoginUseCase;
import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClient;
import com.modeunsa.boundedcontext.auth.out.client.OAuthClientFactory;
import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthProviderTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuthLoginUseCaseTest {

  @Mock private OAuthClientFactory oauthClientFactory;
  @Mock private OAuthClient oauthClient;
  @Mock private OAuthAccountResolveUseCase oauthAccountResolveUseCase;
  @Mock private AuthTokenIssueUseCase authTokenIssueUseCase;
  @Mock private MemberSupport memberSupport;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private OAuthLoginUseCase oauthLoginUseCase;

  private final OAuthProvider provider = OAuthProvider.KAKAO;
  private final String code = "auth_code";
  private final String redirectUri = "http://localhost:3000/callback";
  private final String state = "test_state";

  @BeforeEach
  void setUp() {
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // 1. Redis에 저장될 값을 임시로 담을 Map 생성 (테스트 격리)
    java.util.Map<String, String> redisData = new java.util.HashMap<>();

    // 2. setIfAbsent 호출 시, 키와 값을 Map에 저장하고 true 반환
    given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
        .willAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              String value = invocation.getArgument(1);
              redisData.put(key, value);
              return true;
            });

    // 3. get 호출 시, Map에 저장된 값을 반환 (State 검증 로직 포함)
    given(valueOperations.get(anyString()))
        .willAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              // 기존 로직: state 검증용
              if (key.equals("oauth:state:" + state)) {
                return provider.name();
              }
              // 락 검증용: setIfAbsent 때 저장된 값 반환
              return redisData.get(key);
            });
  }

  @Nested
  @DisplayName("OAuth 로그인")
  class OAuthLogin {

    @Test
    @DisplayName("기존 회원 로그인 성공")
    void loginExistingMember() {
      // given
      OAuthProviderTokenResponse tokenResponse =
          new OAuthProviderTokenResponse("access", "refresh", "bearer", 3600L);
      OAuthUserInfo userInfo =
          OAuthUserInfo.of(provider, "12345", "test@test.com", "테스터", "010-1234-5678");
      Member member = createMemberWithId(1L);
      OAuthAccount socialAccount = OAuthAccount.builder().member(member).build();
      JwtTokenResponse expectedToken = JwtTokenResponse.of("at", "rt", 3600L, 604800L);

      given(oauthClientFactory.getClient(provider)).willReturn(oauthClient);
      given(oauthClient.getToken(code, redirectUri)).willReturn(tokenResponse);
      given(oauthClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfo);
      given(oauthAccountResolveUseCase.execute(provider, userInfo)).willReturn(socialAccount);
      given(memberSupport.getSellerIdByMemberId(1L)).willReturn(null);
      given(authTokenIssueUseCase.execute(1L, MemberRole.MEMBER, null)).willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(redisTemplate).delete("lock:auth:" + provider + ":" + userInfo.providerId());
    }

    @Test
    @DisplayName("신규 회원 가입 후 로그인 성공")
    void loginNewMember() {
      // given
      OAuthProviderTokenResponse tokenResponse =
          new OAuthProviderTokenResponse("access", "refresh", "bearer", 3600L);
      OAuthUserInfo userInfo =
          OAuthUserInfo.of(provider, "67890", "new@test.com", "신규", "010-9876-5432");
      Member member = createMemberWithId(2L);
      OAuthAccount socialAccount = OAuthAccount.builder().member(member).build();
      JwtTokenResponse expectedToken = JwtTokenResponse.of("new_at", "new_rt", 3600L, 604800L);

      given(oauthClientFactory.getClient(provider)).willReturn(oauthClient);
      given(oauthClient.getToken(code, redirectUri)).willReturn(tokenResponse);
      given(oauthClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfo);
      given(oauthAccountResolveUseCase.execute(provider, userInfo)).willReturn(socialAccount);
      given(memberSupport.getSellerIdByMemberId(2L)).willReturn(null);
      given(authTokenIssueUseCase.execute(2L, MemberRole.MEMBER, null)).willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(oauthAccountResolveUseCase).execute(provider, userInfo);
    }
  }

  private Member createMemberWithId(Long id) {
    Member member = Member.builder().role(MemberRole.MEMBER).build();
    ReflectionTestUtils.setField(member, "id", id);
    return member;
  }
}
