package com.modeunsa.shared.member.dto.request;

public record MemberProfileCreateRequest(
    String nickname,
    // TODO: MultipartFile로 변경 예정
    String profileImageUrl,
    Integer heightCm,
    Integer weightKg,
    String skinType) {}
