package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberBasicInfoUpdateRequest {
  private String realName;

  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
  private String phoneNumber;

  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;
}
