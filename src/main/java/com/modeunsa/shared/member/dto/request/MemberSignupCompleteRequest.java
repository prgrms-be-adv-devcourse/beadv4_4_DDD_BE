package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberSignupCompleteRequest(
    // 기본 정보
    @NotBlank String realName,
    @NotBlank String email,
    @NotBlank String phoneNumber,

    // 프로필 정보
    @NotBlank String nickname,
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType) {}
