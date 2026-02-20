package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentImageRequest {

  @NotBlank private final String imageUrl;

  @NotEmpty private final Boolean isPrimary;

  @NotEmpty private final Integer sortOrder;
}
