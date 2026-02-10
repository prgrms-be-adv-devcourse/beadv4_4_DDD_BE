package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record MemberProfileUpdateRequest(
    @NotEmpty(message = "닉네임은 필수입니다.") String nickname,
    String profileImageUrl,
    @Min(value = 50, message = "키는 50cm 이상이어야 합니다.")
        @Max(value = 300, message = "키는 300cm 이하여야 합니다.")
        Integer heightCm,
    @Min(value = 10, message = "몸무게는 10kg 이상이어야 합니다.")
        @Max(value = 300, message = "몸무게는 300kg 이하여야 합니다.")
        Integer weightKg,

    // TODO: Enum으로 변경 예정
    String skinType) {}
