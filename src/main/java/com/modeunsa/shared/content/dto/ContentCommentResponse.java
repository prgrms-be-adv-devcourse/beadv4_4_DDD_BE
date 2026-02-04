package com.modeunsa.shared.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ContentCommentResponse(
    @NotNull Long commentId,
    @NotBlank String text,
    @NotBlank String authorNickname,
    @NotNull LocalDateTime createdAt) {}
