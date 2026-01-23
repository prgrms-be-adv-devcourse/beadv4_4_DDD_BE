package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentCommentResponse {

  @NotNull private final Long commentId;

  @NotBlank private final String text;

  @NotBlank private final String authorNickname;

  @NotNull private final LocalDateTime createdAt;
}
