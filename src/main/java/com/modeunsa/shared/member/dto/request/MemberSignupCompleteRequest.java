package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record MemberSignupCompleteRequest(
    // 기본 정보
    @NotEmpty String realName,
    @NotEmpty @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
    @NotEmpty @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phoneNumber,

    // 프로필 정보
    @NotEmpty String nickname,
    String profileImageUrl,
    @Min(value = 50, message = "키는 50cm 이상이어야 합니다.")
        @Max(value = 300, message = "키는 300cm 이하여야 합니다.")
        Integer heightCm,
    @Min(value = 10, message = "몸무게는 10kg 이상이어야 합니다.")
        @Max(value = 300, message = "몸무게는 300kg 이하여야 합니다.")
        Integer weightKg,
    String skinType) {}
