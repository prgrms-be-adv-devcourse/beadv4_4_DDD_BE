package com.modeunsa.shared.member.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {
  private Long id;
  private String nickname;
  // TODO: S3 연동 후 수정 필요
  private String profileImageUrl;
  private Integer heightCm;
  private Integer weightKg;
  private String skinType;

  public static MemberProfileResponse from(MemberProfile memberProfile) {
    return MemberProfileResponse.builder()
        .id(memberProfile.getId())
        .nickname(memberProfile.getNickname())
        .profileImageUrl(memberProfile.getProfileImageUrl())
        .heightCm(memberProfile.getHeightCm())
        .weightKg(memberProfile.getWeightKg())
        .skinType(memberProfile.getSkinType())
        .build();
  }
}
