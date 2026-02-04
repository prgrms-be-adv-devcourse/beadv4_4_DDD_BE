package com.modeunsa.shared.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentImageResponse {

  @NotBlank private String imageUrl;

  @NotNull private Boolean isPrimary;

  @NotNull private Integer sortOrder;
}
