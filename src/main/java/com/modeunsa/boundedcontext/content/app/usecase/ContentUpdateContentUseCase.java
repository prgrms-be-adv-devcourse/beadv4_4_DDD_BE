package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentSupport;
import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.mapper.ContentMapper;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentUpdateContentUseCase {

  private final ContentSupport contentSupport;
  private final ContentMapper contentMapper;

  @Transactional
  public ContentResponse updateContent(Long contentId, ContentRequest contentRequest, ContentMember author) {
    Content content = contentSupport.findById(contentId)
      .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND));

    // validate
    this.validateContent(content, contentRequest, author);

    // 3. 상태 변경
    applyUpdate(content, contentRequest);
    // 4. 결과 반환
    return contentMapper.toResponse(content);
  }

  private void  validateContent(Content content, ContentRequest contentRequest, ContentMember author) {
    // 작성자 검증
    if (!content.getAuthor().equals(author)) {
      throw new GeneralException(ErrorStatus._FORBIDDEN);
    }

    // text 검증
    if (contentRequest.getText() == null || contentRequest.getText().isBlank()) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }

    if (contentRequest.getText().length() > 500) {
      throw new GeneralException(ErrorStatus.CONTENT_TEXT_LIMIT_EXCEEDED);
    }


    // image
    if (contentRequest.getImages() == null) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }

    for (var image : contentRequest.getImages()) {
      if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
        throw new GeneralException(ErrorStatus.CONTENT_IMAGE_LIMIT_EXCEEDED);
      }
    }

    // tag
    if (contentRequest.getTags() == null) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }

    if (contentRequest.getTags().size() > 5) {
      throw new GeneralException(ErrorStatus.CONTENT_TAG_SIZE_EXCEEDED);
    }

    for (String tag : contentRequest.getTags()) {
      if (tag == null || tag.isBlank()) {
        throw new GeneralException(ErrorStatus.CONTENT_TAG_LIMIT_EXCEEDED);
      }
      if (tag.length() > 10) {
        throw new GeneralException(ErrorStatus.CONTENT_TAG_LENGTH_EXCEEDED);
      }
    }
  }

  private void applyUpdate(Content content, ContentRequest contentRequest) {

  }
}
