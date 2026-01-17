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

  @NotBlank(message = "CONTENT_TEXT_LIMIT_EXCEEDED")
  @Size(max = 500, message = "CONTENT_TEXT_LENGTH_EXCEEDED")
  private String text;

  @NotEmpty(message = "CONTENT_TAG_LIMIT_EXCEEDED")
  @Size(max = 5, message = "CONTENT_TAG_SIZE_EXCEEDED")
  private List<@NotBlank @Size(max = 10, message = "CONTENT_TAG_LENGTH_EXCEEDED") String> tags;

  @NotEmpty(message = "CONTENT_IMAGE_LIMIT_EXCEEDED")
  @Size(max = 5, message = "CONTENT_IMAGE_SIZE_EXCEEDED")
  private List<ContentImageRequest> images;
}
