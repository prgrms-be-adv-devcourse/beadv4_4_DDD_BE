package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContentCommentRequest {

  @NotBlank
  @Size(max = 100)
  private String text;
}
