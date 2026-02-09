package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
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
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthProviderTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
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
    // Redis 기본 설정만 유지
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
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

      // Redis stubbing
      setupRedisForLogin(userInfo.providerId());

      given(oauthClientFactory.getClient(provider)).willReturn(oauthClient);
      given(oauthClient.getToken(code, redirectUri)).willReturn(tokenResponse);
      given(oauthClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfo);

      final Member member = createMemberWithId(1L);
      final OAuthAccount socialAccount = OAuthAccount.builder().member(member).build();

      given(oauthAccountResolveUseCase.execute(provider, userInfo)).willReturn(socialAccount);
      given(memberSupport.getSellerIdByMemberId(1L)).willReturn(null);

      final JwtTokenResponse expectedToken =
          JwtTokenResponse.of("at", "rt", 3600L, 604800L, MemberStatus.ACTIVE.name());
      given(authTokenIssueUseCase.execute(1L, MemberRole.MEMBER, null, MemberStatus.ACTIVE.name()))
          .willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(redisTemplate, times(1)).delete("oauth:state:" + state);
    }

    @Test
    @DisplayName("신규 회원 가입 후 로그인 성공")
    void loginNewMember() {
      // given
      OAuthProviderTokenResponse tokenResponse =
          new OAuthProviderTokenResponse("access", "refresh", "bearer", 3600L);
      OAuthUserInfo userInfo =
          OAuthUserInfo.of(provider, "67890", "new@test.com", "신규", "010-9876-5432");

      // Redis stubbing
      setupRedisForLogin(userInfo.providerId());

      given(oauthClientFactory.getClient(provider)).willReturn(oauthClient);
      given(oauthClient.getToken(code, redirectUri)).willReturn(tokenResponse);
      given(oauthClient.getUserInfo(tokenResponse.accessToken())).willReturn(userInfo);

      final Member member = createMemberWithId(2L);
      final OAuthAccount socialAccount = OAuthAccount.builder().member(member).build();

      given(oauthAccountResolveUseCase.execute(provider, userInfo)).willReturn(socialAccount);
      given(memberSupport.getSellerIdByMemberId(2L)).willReturn(null);

      final JwtTokenResponse expectedToken =
          JwtTokenResponse.of("new_at", "new_rt", 3600L, 604800L, MemberStatus.ACTIVE.name());
      given(authTokenIssueUseCase.execute(2L, MemberRole.MEMBER, null, MemberStatus.ACTIVE.name()))
          .willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(oauthAccountResolveUseCase).execute(provider, userInfo);
      verify(redisTemplate, times(1)).delete("oauth:state:" + state);
    }
  }

  // Helper Methods
  private void setupRedisForLogin(String providerId) {
    // State 검증용 - get 호출 시 provider 반환
    given(valueOperations.get("oauth:state:" + state)).willReturn(provider.name());
  }

  private Member createMemberWithId(Long id) {
    Member member = Member.builder().role(MemberRole.MEMBER).build();
    ReflectionTestUtils.setField(member, "id", id);
    return member;
  }
}
