package com.modeunsa.boundedcontext.member.domain.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberBasicInfoResponse(
    Long id, String email, String realName, String phoneNumber, String role) {
  public static MemberBasicInfoResponse from(Member member) {
    return MemberBasicInfoResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .realName(member.getRealName())
        .phoneNumber(member.getPhoneNumber())
        .role(member.getRole().name())
        .build();
  }
}
