package com.modeunsa.boundedcontext.content.app.dto.content;

import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Content 생성/수정 명령. API 요청 바디로도 사용한다. (JSON: text, tags, images) */
public record ContentCreateCommand(
    @NotBlank @Size(max = 500) String text,
    @NotEmpty @Size(max = 5) List<@NotBlank @Size(max = 10) String> tags,
    @NotEmpty @Size(max = 5) @Valid List<ContentImageDto> images) {}
