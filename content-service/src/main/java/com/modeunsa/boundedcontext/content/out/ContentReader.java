package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import jakarta.validation.Valid;

public interface ContentReader {

  ContentDetailDto findContentById(@Valid Long contentId);
}
