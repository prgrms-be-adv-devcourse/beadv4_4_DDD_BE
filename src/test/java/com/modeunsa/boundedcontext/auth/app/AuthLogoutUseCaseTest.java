package com.modeunsa.boundedcontext.auth.app.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthAccessTokenBlacklist;
import com.modeunsa.boundedcontext.auth.out.repository.AuthAccessTokenBlacklistRepository;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthLogoutUseCaseTest {

  @InjectMocks private AuthLogoutUseCase authLogoutUseCase;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private AuthRefreshTokenRepository refreshTokenRepository;

  @Mock private AuthAccessTokenBlacklistRepository blacklistRepository;

  private final String accessToken = "valid_access_token";
  private final Long memberId = 1L;
  private final long remainingTime = 3600L;

  @Test
  @DisplayName("로그아웃 성공 케이스: 유효한 토큰으로 로그아웃 시 Refresh Token 삭제 및 Access Token 블랙리스트 등록")
  void logoutSuccess() {
    // given
    setupValidToken();
    given(jwtTokenProvider.getRemainingExpiration(anyString())).willReturn(remainingTime);

    // when
    authLogoutUseCase.execute(accessToken);

    // then
    verify(refreshTokenRepository, times(1)).deleteById(memberId);
    verify(blacklistRepository, times(1)).save(any(AuthAccessTokenBlacklist.class));
  }

  @Test
  @DisplayName("이미 로그아웃된 토큰인 경우 무시 (예외 발생 안 함)")
  void logoutAlreadyLoggedOut() {
    // given
    setupValidToken();
    given(blacklistRepository.existsById(accessToken)).willReturn(true);

    // when
    authLogoutUseCase.execute(accessToken);

    // then
    verify(refreshTokenRepository, never()).deleteById(anyLong());
    verify(blacklistRepository, never()).save(any(AuthAccessTokenBlacklist.class));
  }

  @Test
  @DisplayName("Access Token이 아닌 경우 예외 발생")
  void logoutNotAccessToken() {
    // given
    willDoNothing().given(jwtTokenProvider).validateTokenOrThrow(accessToken);
    given(jwtTokenProvider.isAccessToken(accessToken)).willReturn(false);

    // when & then
    assertThrows(GeneralException.class, () -> authLogoutUseCase.execute(accessToken));
  }

  // --- Helper Method ---
  private void setupValidToken() {
    willDoNothing().given(jwtTokenProvider).validateTokenOrThrow(anyString());
    given(jwtTokenProvider.isAccessToken(anyString())).willReturn(true);
    given(jwtTokenProvider.getMemberIdFromToken(anyString())).willReturn(memberId);
  }
}
