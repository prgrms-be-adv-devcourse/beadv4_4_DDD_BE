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

  private final String ACCESS_TOKEN = "valid_access_token";
  private final Long MEMBER_ID = 1L;
  private final long REMAINING_TIME = 3600L;

  @Test
  @DisplayName("로그아웃 성공 케이스: 유효한 토큰으로 로그아웃 시 Refresh Token 삭제 및 Access Token 블랙리스트 등록")
  void logout_Success() {
    // given
    setupValidToken();
    // 성공 케이스에서만 호출되는 설정은 여기로 이동
    given(jwtTokenProvider.getRemainingExpiration(anyString())).willReturn(REMAINING_TIME);

    // when
    authLogoutUseCase.execute(ACCESS_TOKEN);

    // then
    verify(refreshTokenRepository, times(1)).deleteById(MEMBER_ID);
    verify(blacklistRepository, times(1)).save(any(AuthAccessTokenBlacklist.class));
  }

  @Test
  @DisplayName("이미 로그아웃된 토큰인 경우 무시")
  void logout_AlreadyLoggedOut() {
    // given
    setupValidToken();
    // **여기서는 getRemainingExpiration 설정이 없으므로 에러가 나지 않음**
    given(blacklistRepository.existsById(ACCESS_TOKEN)).willReturn(true);

    // when
    authLogoutUseCase.execute(ACCESS_TOKEN);

    // then
    verify(refreshTokenRepository, never()).deleteById(anyLong());
    verify(blacklistRepository, never()).save(any(AuthAccessTokenBlacklist.class));
  }

  @Test
  @DisplayName("Access Token이 아닌 경우 예외 발생")
  void logout_NotAccessToken() {
    // given
    willDoNothing().given(jwtTokenProvider).validateTokenOrThrow(ACCESS_TOKEN);
    given(jwtTokenProvider.isAccessToken(ACCESS_TOKEN)).willReturn(false);

    // when & then
    assertThrows(GeneralException.class, () -> authLogoutUseCase.execute(ACCESS_TOKEN));
  }

  // --- Helper Method ---
  private void setupValidToken() {
    willDoNothing().given(jwtTokenProvider).validateTokenOrThrow(anyString());
    given(jwtTokenProvider.isAccessToken(anyString())).willReturn(true);
    given(jwtTokenProvider.getMemberIdFromToken(anyString())).willReturn(MEMBER_ID);
  }
}
