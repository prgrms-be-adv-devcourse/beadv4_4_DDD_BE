package com.modeunsa.shared.member.event;

public record MemberProfileCreatedEvent(
    Long memberId,
    String nickname,
    String profileImageUrl
) {
  public static MemberProfileCreatedEvent of(Long memberId, String nickname, String profileImageUrl) {
    return new MemberProfileCreatedEvent(memberId, nickname, profileImageUrl);
  }
}