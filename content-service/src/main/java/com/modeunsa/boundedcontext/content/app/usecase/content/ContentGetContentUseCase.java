package com.modeunsa.boundedcontext.content.app.usecase.content;

import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import com.modeunsa.boundedcontext.content.out.ContentReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContentGetContentUseCase {

  private final ContentReader contentReader;

  public ContentDetailDto execute(Long contentId) {
    return contentReader.findContentById(contentId);
  }
}
