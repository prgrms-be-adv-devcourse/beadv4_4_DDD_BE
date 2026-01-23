package com.modeunsa.shared.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContentCommentRequest(@NotBlank @Size(max = 100) String text) {}
