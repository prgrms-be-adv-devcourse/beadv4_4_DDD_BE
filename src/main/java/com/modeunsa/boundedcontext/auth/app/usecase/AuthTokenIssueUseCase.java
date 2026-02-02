package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.auth.domain.entity.AuthRefreshToken;
import com.modeunsa.boundedcontext.auth.out.repository.AuthRefreshTokenRepository;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.security.jwt.JwtProperties;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenIssueUseCase {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthRefreshTokenRepository authRefreshTokenRepository;
  private final JwtProperties jwtProperties;

  /** Access Token + Refresh Token 발급 및 Redis 저장 */
  public JwtTokenResponse execute(Long memberId, MemberRole role, Long sellerId) {
    String accessToken = jwtTokenProvider.createAccessToken(memberId, role, sellerId);
    String refreshToken = jwtTokenProvider.createRefreshToken(memberId, role, sellerId);

    AuthRefreshToken tokenEntity =
        AuthRefreshToken.builder()
            .memberId(memberId)
            .refreshToken(refreshToken)
            .expiration(jwtProperties.refreshTokenExpiration())
            .build();

    authRefreshTokenRepository.save(tokenEntity);

    return JwtTokenResponse.of(
        accessToken,
        refreshToken,
        jwtTokenProvider.getAccessTokenExpiration(),
        jwtTokenProvider.getRefreshTokenExpiration());
  }
}
