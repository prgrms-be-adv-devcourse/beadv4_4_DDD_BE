package com.modeunsa.boundedcontext.content.app.dto;

import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record ContentResponse(
    @NotNull Long contentId,
    @NotNull Long authorMemberId,
    @NotBlank String text,
    @NotNull List<String> tags,
    @NotNull List<ContentImageDto> images,
    LocalDateTime createdAt) {}
