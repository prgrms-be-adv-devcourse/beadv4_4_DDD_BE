package com.modeunsa.boundedcontext.content.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class ContentRequest {

  @NotBlank
  @Size(max = 500)
  private String text;

  @NotEmpty
  @Size(max = 5)
  private List<@NotBlank @Size(max = 10) String> tags;

  @NotEmpty
  @Size(max = 5)
  private List<ContentImageRequest> images;
}
