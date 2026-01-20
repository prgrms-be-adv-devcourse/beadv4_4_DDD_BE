package com.modeunsa.shared.auth.dto;

import lombok.Builder;

@Builder
public record JwtTokenResponse(
    String accessToken,
    String refreshToken,
    long accessTokenExpiresIn,
    long refreshTokenExpiresIn
) {
  public static JwtTokenResponse of(
      String accessToken,
      String refreshToken,
      long accessTokenExpiresIn,
      long refreshTokenExpiresIn) {
    return JwtTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(accessTokenExpiresIn)
        .refreshTokenExpiresIn(refreshTokenExpiresIn)
        .build();
  }
}