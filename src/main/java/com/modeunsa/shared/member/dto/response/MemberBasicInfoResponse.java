package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberBasicInfoResponse {
  private Long id;
  private String email;
  private String realName;
  private String phoneNumber;

  public static MemberBasicInfoResponse from(Member member) {
    return MemberBasicInfoResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .realName(member.getRealName())
        .phoneNumber(member.getPhoneNumber())
        .build();
  }
}
