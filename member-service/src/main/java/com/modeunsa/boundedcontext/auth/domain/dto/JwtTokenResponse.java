package com.modeunsa.boundedcontext.auth.domain.dto;

import lombok.Builder;

@Builder
public record JwtTokenResponse(
    String accessToken,
    String refreshToken,
    long accessTokenExpiresIn,
    long refreshTokenExpiresIn,
    String status) {
  public static JwtTokenResponse of(
      String accessToken,
      String refreshToken,
      long accessTokenExpiresIn,
      long refreshTokenExpiresIn,
      String status) {
    return JwtTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(accessTokenExpiresIn)
        .refreshTokenExpiresIn(refreshTokenExpiresIn)
        .status(status)
        .build();
  }
}
