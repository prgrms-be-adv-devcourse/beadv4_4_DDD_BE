package com.modeunsa.shared.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberProfileUpdateImageRequest(
    @NotBlank(message = "이미지 URL은 필수입니다.") String imageUrl) {}
