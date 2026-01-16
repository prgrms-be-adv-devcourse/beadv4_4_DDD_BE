package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthRefreshToken;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenReissueUseCase {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRefreshTokenRepository authRefreshTokenRepository;
  private final AuthTokenIssueUseCase authTokenIssueUseCase;

  /** Refresh Token으로 Access Token 재발급 */
  public TokenResponse execute(String refreshToken) {
    // TODO: Refresh Token Rotation 및 동시성(Race Condition) 방어 필요
    // - 동시 요청 시 둘 다 성공할 수 있는 문제
    // - Redisson 분산 락 또는 Redis Lua Script로 원자적 처리 고려

    // 1. 토큰 유효성 및 타입 검증
    validateRefreshToken(refreshToken);

    Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
    MemberRole role = jwtTokenProvider.getRoleFromToken(refreshToken);

    // 2. Redis 조회 및 검증
    AuthRefreshToken savedToken =
        authRefreshTokenRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.AUTH_REFRESH_TOKEN_NOT_FOUND));

    if (!savedToken.isTokenMatching(refreshToken)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_REFRESH_TOKEN);
    }

    // 3. 기존 토큰 삭제 (Redis)
    authRefreshTokenRepository.deleteById(memberId);

    // 4. 새 토큰 세트 발급 (AuthTokenIssueUseCase 위임)
    return authTokenIssueUseCase.execute(memberId, role);
  }

  private void validateRefreshToken(String refreshToken) {
    jwtTokenProvider.validateTokenOrThrow(refreshToken);
    if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN_TYPE);
    }
  }
}
