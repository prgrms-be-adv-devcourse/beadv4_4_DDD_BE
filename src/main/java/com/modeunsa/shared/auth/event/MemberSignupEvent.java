package com.modeunsa.shared.auth.event;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;

public record MemberSignupEvent(
    Long memberId,
    String email, // nullable
    OAuthProvider provider
) {
  public static MemberSignupEvent of(Long memberId, String email, OAuthProvider provider) {
    return new MemberSignupEvent(memberId, email, provider);
  }
}