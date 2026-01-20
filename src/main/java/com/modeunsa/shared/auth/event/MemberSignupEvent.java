package com.modeunsa.shared.auth.event;

import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberSignupEvent {

  private final Long memberId;
  private final String email; // nullable
  private final OAuthProvider provider;

  public static MemberSignupEvent of(Long memberId, String email, OAuthProvider provider) {
    return new MemberSignupEvent(memberId, email, provider);
  }
}
