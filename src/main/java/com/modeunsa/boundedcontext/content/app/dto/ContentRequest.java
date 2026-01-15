package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
public class ContentRequest {

  @NotBlank private String text;
  @NotEmpty private List<String> tags;
  @NotEmpty private List<ContentImageRequest> images;
}