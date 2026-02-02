package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthProviderTokenResponse;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import java.lang.reflect.Field;
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

@ExtendWith(MockitoExtension.class)
class OAuthLoginUseCaseTest {

  @InjectMocks private OAuthLoginUseCase oauthLoginUseCase;

  @Mock private OAuthClientFactory oauthClientFactory;
  @Mock private OAuthAccountResolveUseCase oauthAccountResolveUseCase;
  @Mock private AuthTokenIssueUseCase authTokenIssueUseCase;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;
  @Mock private OAuthClient oauthClient;
  @Mock private MemberSupport memberSupport;

  private final OAuthProvider provider = OAuthProvider.KAKAO;
  private final String code = "auth_code";
  private final String redirectUri = "http://localhost:3000/callback";
  private final String state = "random_state";
  private final String stateKey = "oauth:state:" + state;
  private final Long memberId = 1L;

  @BeforeEach
  void setUp() {
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
  }

  @Nested
  @DisplayName("state 검증")
  class StateValidation {

    @Test
    @DisplayName("state가 Redis에 없는 경우 예외 발생")
    void stateNotFound() {
      // given
      given(valueOperations.get(stateKey)).willReturn(null);

      // when & then
      assertThrows(
          GeneralException.class,
          () -> oauthLoginUseCase.execute(provider, code, redirectUri, state));

      verify(oauthClientFactory, never()).getClient(any());
    }

    @Test
    @DisplayName("state의 provider가 불일치하는 경우 예외 발생")
    void stateProviderMismatch() {
      // given
      given(valueOperations.get(stateKey)).willReturn("NAVER");

      // when & then
      assertThrows(
          GeneralException.class,
          () -> oauthLoginUseCase.execute(provider, code, redirectUri, state));

      verify(oauthClientFactory, never()).getClient(any());
    }
  }

  @Nested
  @DisplayName("OAuth 로그인")
  class OAuthLogin {

    @Test
    @DisplayName("기존 회원 로그인 성공")
    void loginExistingMember() {
      // given
      setupValidState();
      setupOAuthClient();
      given(memberSupport.getSellerIdByMemberId(memberId)).willReturn(0L);

      Member member = createMemberWithId(memberId);
      OAuthAccount socialAccount = createSocialAccount(member);
      JwtTokenResponse expectedToken =
          JwtTokenResponse.of("access_token", "refresh_token", 3600L, 604800L);

      given(oauthAccountResolveUseCase.execute(any(OAuthProvider.class), any(OAuthUserInfo.class)))
          .willReturn(socialAccount);
      given(authTokenIssueUseCase.execute(memberId, MemberRole.MEMBER, 0L))
          .willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(redisTemplate, times(1)).delete(stateKey);
      verify(oauthAccountResolveUseCase, times(1)).execute(any(), any());
      verify(authTokenIssueUseCase, times(1)).execute(memberId, MemberRole.MEMBER, 0L);
    }

    @Test
    @DisplayName("신규 회원 가입 후 로그인 성공")
    void loginNewMember() {
      // given
      setupValidState();
      setupOAuthClient();
      given(memberSupport.getSellerIdByMemberId(memberId)).willReturn(0L);

      Member member = createMemberWithId(memberId);
      OAuthAccount socialAccount = createSocialAccount(member);
      JwtTokenResponse expectedToken =
          JwtTokenResponse.of("access_token", "refresh_token", 3600L, 604800L);

      given(oauthAccountResolveUseCase.execute(any(OAuthProvider.class), any(OAuthUserInfo.class)))
          .willReturn(socialAccount);
      given(authTokenIssueUseCase.execute(memberId, MemberRole.MEMBER, 0L))
          .willReturn(expectedToken);

      // when
      JwtTokenResponse result = oauthLoginUseCase.execute(provider, code, redirectUri, state);

      // then
      assertThat(result).isEqualTo(expectedToken);
      verify(oauthAccountResolveUseCase, times(1)).execute(any(), any());
    }
  }

  // --- Helper Methods ---

  private void setupValidState() {
    given(valueOperations.get(stateKey)).willReturn(provider.name());
  }

  private void setupOAuthClient() {
    OAuthProviderTokenResponse tokenResponse =
        OAuthProviderTokenResponse.builder()
            .accessToken("oauth_access_token")
            .refreshToken("oauth_refresh_token")
            .expiresIn(3600L)
            .build();
    OAuthUserInfo userInfo =
        OAuthUserInfo.builder()
            .provider(provider)
            .providerId("kakao_12345")
            .email("test@example.com")
            .name("테스트유저")
            .build();

    given(oauthClientFactory.getClient(provider)).willReturn(oauthClient);
    given(oauthClient.getToken(code, redirectUri)).willReturn(tokenResponse);
    given(oauthClient.getUserInfo(anyString())).willReturn(userInfo);
  }

  private Member createMemberWithId(Long id) {
    Member member = Member.builder().email("test@example.com").realName("테스트유저").build();
    setEntityId(member, id);
    return member;
  }

  private OAuthAccount createSocialAccount(Member member) {
    OAuthAccount socialAccount =
        OAuthAccount.builder().oauthProvider(provider).providerId("kakao_12345").build();
    socialAccount.assignMember(member);
    return socialAccount;
  }

  private void setEntityId(Object entity, Long id) {
    try {
      Field idField = findIdField(entity.getClass());
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set entity id", e);
    }
  }

  private Field findIdField(Class<?> clazz) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("id");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("id field not found");
  }
}
