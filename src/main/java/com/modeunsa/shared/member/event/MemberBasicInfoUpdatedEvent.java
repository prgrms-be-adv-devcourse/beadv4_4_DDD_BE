package com.modeunsa.shared.member.event;

public record MemberBasicInfoUpdatedEvent(
    Long memberId,
    String realName,
    String email,
    String phoneNumber
) {
  public static MemberBasicInfoUpdatedEvent of(Long memberId, String realName, String email, String phoneNumber) {
    return new MemberBasicInfoUpdatedEvent(memberId, realName, email, phoneNumber);
  }
}