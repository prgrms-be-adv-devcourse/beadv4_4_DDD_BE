package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberBasicInfoResponse(
    Long id,
    String email,
    String realName,
    String phoneNumber
) {
  public static MemberBasicInfoResponse from(Member member) {
    return MemberBasicInfoResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .realName(member.getRealName())
        .phoneNumber(member.getPhoneNumber())
        .build();
  }
}