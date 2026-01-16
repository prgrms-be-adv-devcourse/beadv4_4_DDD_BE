package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentResponse {

  @NotNull private final Long contentId;

  @NotNull private final Long authorMemberId;

  @NotBlank private final String text;

  @NotEmpty private final List<String> tags;

  @NotEmpty private final List<ContentImageRequest> images;

  private LocalDateTime createdAt;
}
