package com.modeunsa.boundedcontext.member.domain.dto.response;

import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import lombok.Builder;

@Builder
public record MemberProfileResponse(
    Long id,
    String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType) {
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
