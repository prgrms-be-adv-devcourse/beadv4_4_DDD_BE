package com.modeunsa.shared.member.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberProfileCreateRequest {
  private String nickname;
  // TODO: MultipartFile로 변경 예정
  private String profileImageUrl;
  private Integer heightCm;
  private Integer weightKg;
  private String skinType;
}
