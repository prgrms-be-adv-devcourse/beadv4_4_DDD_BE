package com.modeunsa.shared.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;
  private String refreshToken;
  private long accessTokenExpiresIn;
  private long refreshTokenExpiresIn;

  public static TokenResponse of(
      String accessToken,
      String refreshToken,
      long accessTokenExpiresIn,
      long refreshTokenExpiresIn
  ) {
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(accessTokenExpiresIn)
        .refreshTokenExpiresIn(refreshTokenExpiresIn)
        .build();
  }
}