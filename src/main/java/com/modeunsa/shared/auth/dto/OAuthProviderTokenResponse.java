package com.modeunsa.shared.auth.dto;

import lombok.Builder;

@Builder
public record OAuthProviderTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {
  public static OAuthProviderTokenResponse of(
      String accessToken, String refreshToken, String tokenType, Long expiresIn) {
    return OAuthProviderTokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType(tokenType)
        .expiresIn(expiresIn)
        .build();
  }
}