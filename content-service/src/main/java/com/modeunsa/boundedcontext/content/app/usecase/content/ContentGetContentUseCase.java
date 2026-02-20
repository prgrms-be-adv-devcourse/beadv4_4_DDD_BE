package com.modeunsa.boundedcontext.content.app.usecase.content;

import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContentGetContentUseCase {

  public ContentDetailDto execute(@Valid Long contentId) {
    return null;
  }
}
