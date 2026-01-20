package com.modeunsa.shared.auth.dto;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import lombok.Builder;

@Builder
public record OAuthUserInfo(
    OAuthProvider provider,
    String providerId,
    String email, // nullable
    String name, // nullable
    String phoneNumber // nullable
) {
  public static OAuthUserInfo of(
      OAuthProvider provider,
      String providerId,
      String email,
      String name,
      String phoneNumber) {

    return OAuthUserInfo.builder()
        .provider(provider)
        .providerId(providerId)
        .email(email)
        .name(name)
        .phoneNumber(phoneNumber)
        .build();
  }
}
