package com.modeunsa.boundedcontext.content.app;

import com.modeunsa.boundedcontext.content.app.usecase.ContentCreateCommentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentCreateContentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentDeleteCommentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentDeleteContentUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentGetContentsUseCase;
import com.modeunsa.boundedcontext.content.app.usecase.ContentUpdateContentUseCase;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.shared.content.dto.ContentCommentRequest;
import com.modeunsa.shared.content.dto.ContentCommentResponse;
import com.modeunsa.shared.content.dto.ContentRequest;
import com.modeunsa.shared.content.dto.ContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ContentFacade {

  private final ContentCreateContentUseCase contentCreateContentUseCase;
  private final ContentUpdateContentUseCase contentUpdateContentUseCase;
  private final ContentDeleteContentUseCase contentDeleteContentUseCase;
  private final ContentGetContentsUseCase contentGetContentsUseCase;
  private final ContentCreateCommentUseCase contentCreateCommentUseCase;
  private final ContentDeleteCommentUseCase contentDeleteCommentUseCase;

  @Transactional
  public ContentResponse createContent(ContentRequest contentRequest, ContentMember author) {
    return contentCreateContentUseCase.createContent(contentRequest, author);
  }

  @Transactional
  public ContentResponse updateContent(
      Long contentId, ContentRequest contentRequest, ContentMember author) {
    return contentUpdateContentUseCase.updateContent(contentId, contentRequest, author);
  }

  @Transactional
  public void deleteContent(Long contentId, ContentMember author) {
    contentDeleteContentUseCase.deleteContent(contentId, author);
  }

  @Transactional(readOnly = true)
  public Page<ContentResponse> getContents(int page) {
    return contentGetContentsUseCase.getContents(page);
  }

  @Transactional
  public ContentCommentResponse createContentComment(
      Long contentId, ContentCommentRequest contentCommentRequest, ContentMember author) {
    return contentCreateCommentUseCase.createContentComment(
        contentId, contentCommentRequest, author);
  }

  @Transactional
  public void deleteContentComment(Long contentId, Long commentId, ContentMember author) {
    contentDeleteCommentUseCase.deleteContentComment(contentId, commentId, author);
  }
}
