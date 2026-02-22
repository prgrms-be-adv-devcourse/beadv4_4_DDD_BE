package com.modeunsa.boundedcontext.content.app.dto.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Content 생성/수정 시 이미지 정보 스펙. API 요청·응답과 Content 엔티티 내부에서 공통 사용. */
public record ContentImageDto(
    @NotBlank String imageUrl, @NotNull Boolean isPrimary, @NotNull Integer sortOrder) {}
