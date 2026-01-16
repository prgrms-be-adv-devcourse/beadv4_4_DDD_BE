package com.modeunsa.boundedcontext.content.app;

import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.usecase.ContentCreateContentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentDeleteContentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentUpdateContentUseCase;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ContentFacade {

  private final ContentCreateContentUseCase contentCreateContentUseCase;
  private final ContentUpdateContentUseCase contentUpdateContentUseCase;
  private final ContentDeleteContentUseCase contentDeleteContentUseCase;

  @Transactional
  public ContentResponse createContent(ContentRequest contentRequest, ContentMember author) {
    return contentCreateContentUseCase.createContent(contentRequest, author);
  }

  @Transactional
  public ContentResponse updateContent(
      Long contentId, ContentRequest contentRequest, ContentMember author) {
    return contentUpdateContentUseCase.updateContent(contentId, contentRequest, author);
  }

  public void deleteContent(Long contentId, ContentMember author) {
    contentDeleteContentUseCase.deleteContent(contentId, author);
  }
}
