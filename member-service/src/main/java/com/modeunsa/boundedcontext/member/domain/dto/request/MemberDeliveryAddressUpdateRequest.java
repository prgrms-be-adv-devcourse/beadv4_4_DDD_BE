package com.modeunsa.boundedcontext.member.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberDeliveryAddressUpdateRequest {
  @NotBlank(message = "수령인 이름은 필수입니다.")
  private String recipientName;

  @NotBlank(message = "연락처는 필수입니다.")
  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
  private String recipientPhone;

  @NotBlank(message = "우편번호는 필수입니다.")
  private String zipCode;

  @NotBlank(message = "주소는 필수입니다.")
  private String address;

  @NotBlank(message = "상세 주소는 필수입니다.")
  private String addressDetail;

  private String addressName;
}
