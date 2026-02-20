package com.modeunsa.boundedcontext.content.app.dto.content;

import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ContentCreateCommand(
    @NotBlank String title,
    @NotBlank @Size(max = 500) String text,
    @NotEmpty @Size(max = 5) List<@NotBlank @Size(max = 10) String> tags,
    @NotEmpty @Size(max = 5) @Valid List<ContentImageDto> images) {}
