package com.modeunsa.shared.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthTokenResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long expiresIn;

  public static OAuthTokenResponse of(
      String accessToken, String refreshToken, String tokenType, Long expiresIn) {
    return OAuthTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType(tokenType)
        .expiresIn(expiresIn)
        .build();
  }
}
