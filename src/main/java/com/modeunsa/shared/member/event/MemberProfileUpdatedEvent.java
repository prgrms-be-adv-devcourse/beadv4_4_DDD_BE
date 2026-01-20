package com.modeunsa.shared.member.event;

public record MemberProfileUpdatedEvent(
    Long memberId,
    String nickname,
    String profileImageUrl
) {
  public static MemberProfileUpdatedEvent of(Long memberId, String nickname, String profileImageUrl) {
    return new MemberProfileUpdatedEvent(memberId, nickname, profileImageUrl);
  }
}