package com.modeunsa.shared.auth.dto;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {

  private OAuthProvider provider;
  private String providerId; // 필수 (소셜 고유 ID)
  private String email; // nullable
  private String name; // nullable
  private String phoneNumber; // nullable

  public static OAuthUserInfo of(
      OAuthProvider provider, String providerId, String email, String name, String phoneNumber) {
    return OAuthUserInfo.builder()
        .provider(provider)
        .providerId(providerId)
        .email(email)
        .name(name)
        .phoneNumber(phoneNumber)
        .build();
  }
}
