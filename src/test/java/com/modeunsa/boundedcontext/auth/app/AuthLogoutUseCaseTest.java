package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthLogoutUseCase;
import com.modeunsa.boundedcontext.auth.domain.entity.AuthAccessTokenBlacklist;
import com.modeunsa.boundedcontext.auth.out.repository.AuthAccessTokenBlacklistRepository;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthLogoutUseCase 테스트")
class AuthLogoutUseCaseTest {

  @InjectMocks private AuthLogoutUseCase authLogoutUseCase;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private AuthRefreshTokenRepository refreshTokenRepository;

  @Mock private AuthAccessTokenBlacklistRepository blacklistRepository;

  private static final String VALID_ACCESS_TOKEN = "valid.access.token";
  private static final Long MEMBER_ID = 1L;
  private static final long REMAINING_EXPIRATION = 3600000L; // 1시간

  @Nested
  @DisplayName("로그아웃 성공 케이스")
  class LogoutSuccessCase {

    @Test
    @DisplayName("유효한 토큰으로 로그아웃 시 Refresh Token 삭제 및 Access Token 블랙리스트 등록")
    void logoutWithValidTokenSuccess() {
      // given
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN))
          .willReturn(REMAINING_EXPIRATION);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(refreshTokenRepository).should().deleteById(MEMBER_ID);
      then(blacklistRepository).should().save(any(AuthAccessTokenBlacklist.class));
    }

    @Test
    @DisplayName("Refresh Token이 정상적으로 삭제되는지 확인")
    void logoutRefreshTokenDeleted() {
      // given
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN))
          .willReturn(REMAINING_EXPIRATION);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(refreshTokenRepository).should().deleteById(MEMBER_ID);
    }

    @Test
    @DisplayName("블랙리스트 등록 시 올바른 TTL이 설정되는지 확인")
    void logoutBlacklistRegisteredWithCorrectTtl() {
      // given
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN))
          .willReturn(REMAINING_EXPIRATION);

      ArgumentCaptor<AuthAccessTokenBlacklist> blacklistCaptor =
          ArgumentCaptor.forClass(AuthAccessTokenBlacklist.class);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(blacklistRepository).should().save(blacklistCaptor.capture());

      AuthAccessTokenBlacklist capturedBlacklist = blacklistCaptor.getValue();
      assertThat(capturedBlacklist.getAccessToken()).isEqualTo(VALID_ACCESS_TOKEN);
      assertThat(capturedBlacklist.getMemberId()).isEqualTo(MEMBER_ID);
      // TTL 검증 (AuthAccessTokenBlacklist 엔티티 구조에 따라 수정 필요)
    }
  }

  @Nested
  @DisplayName("만료된 토큰 케이스")
  class ExpiredTokenCase {

    @Test
    @DisplayName("만료된 토큰(TTL이 0 이하)으로 로그아웃 시 블랙리스트 등록하지 않음")
    void logoutWithExpiredTokenNoBlacklistRegistration() {
      // given
      long expiredTtl = 0L;
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN)).willReturn(expiredTtl);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(refreshTokenRepository).should().deleteById(MEMBER_ID);
      then(blacklistRepository).should(never()).save(any(AuthAccessTokenBlacklist.class));
    }

    @Test
    @DisplayName("음수 TTL인 경우에도 블랙리스트 등록하지 않음")
    void logoutWithNegativeTtlNoBlacklistRegistration() {
      // given
      long negativeTtl = -1000L;
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN)).willReturn(negativeTtl);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(blacklistRepository).should(never()).save(any(AuthAccessTokenBlacklist.class));
    }
  }

  @Nested
  @DisplayName("경계값 테스트")
  class EdgeCaseTest {

    @Test
    @DisplayName("TTL이 1ms일 때도 블랙리스트에 등록됨")
    void logoutWithMinimalTtlBlacklistRegistered() {
      // given
      long minimalTtl = 1L;
      given(jwtTokenProvider.getMemberIdFromToken(VALID_ACCESS_TOKEN)).willReturn(MEMBER_ID);
      given(jwtTokenProvider.getRemainingExpiration(VALID_ACCESS_TOKEN)).willReturn(minimalTtl);

      // when
      authLogoutUseCase.execute(VALID_ACCESS_TOKEN);

      // then
      then(blacklistRepository).should().save(any(AuthAccessTokenBlacklist.class));
    }
  }
}