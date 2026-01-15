package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthRefreshToken;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.jwt.JwtProperties;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenUseCase {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRefreshTokenRepository authRefreshTokenRepository;
  private final JwtProperties jwtProperties;

  /**
   * Access Token + Refresh Token 발급
   */
  @Transactional
  public TokenResponse issueTokens(Long memberId, MemberRole role) {
    String accessToken = jwtTokenProvider.createAccessToken(memberId, role);
    String refreshToken = jwtTokenProvider.createRefreshToken(memberId, role);

    AuthRefreshToken tokenEntity = AuthRefreshToken.builder()
        .memberId(memberId)
        .refreshToken(refreshToken)
        .expiration(jwtProperties.refreshTokenExpiration())
        .build();

    authRefreshTokenRepository.save(tokenEntity);

    return TokenResponse.of(
        accessToken,
        refreshToken,
        jwtTokenProvider.getAccessTokenExpiration(),
        jwtTokenProvider.getRefreshTokenExpiration()
    );
  }

  /**
   * Refresh Token으로 Access Token 재발급
   * TODO: Refresh Token Rotation Race Condition 방어 필요
   *   - 동시 요청 시 둘 다 성공할 수 있는 문제
   *   - Redisson 분산 락 또는 Redis Lua 스크립트로 원자적 처리 고려
   */
  @Transactional
  public TokenResponse reissueTokens(String refreshToken) {
    // 1. 토큰 자체의 유효성 검사
    jwtTokenProvider.validateTokenOrThrow(refreshToken);

    // 2. Refresh Token 타입 검증
    if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN_TYPE);
    }

    Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
    MemberRole role = jwtTokenProvider.getRoleFromToken(refreshToken);

    // 3. Redis 조회
    AuthRefreshToken savedToken = authRefreshTokenRepository.findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.AUTH_REFRESH_TOKEN_NOT_FOUND));

    if (!savedToken.getRefreshToken().equals(refreshToken)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_REFRESH_TOKEN);
    }

    // 4. 기존 토큰 삭제
    authRefreshTokenRepository.deleteById(memberId);

    // 5. 새 토큰 세트 발급
    return issueTokens(memberId, role);
  }
}