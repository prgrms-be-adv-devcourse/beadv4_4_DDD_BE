package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentMapper;
import com.modeunsa.boundedcontext.content.app.ContentSupport;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.dto.content.ContentCreateCommand;
import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentImage;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.domain.entity.ContentTag;
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
  public ContentResponse updateContent(
      Long contentId, ContentCreateCommand command, ContentMember author) {
    Content content =
        contentSupport
            .findById(contentId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND));

    validateContent(content, command, author);
    applyUpdate(content, command);

    return contentMapper.toResponse(content);
  }

  private void validateContent(
      Content content, ContentCreateCommand command, ContentMember author) {
    if (!content.getAuthor().equals(author)) {
      throw new GeneralException(ErrorStatus.FORBIDDEN);
    }
    if (command.text() == null || command.text().isBlank()) {
      throw new GeneralException(ErrorStatus.CONTENT_TEXT_REQUIRED);
    }
    if (command.text().length() > 500) {
      throw new GeneralException(ErrorStatus.CONTENT_TEXT_LENGTH_EXCEEDED);
    }
    if (command.images() == null) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }
    for (ContentImageDto image : command.images()) {
      if (image.imageUrl() == null || image.imageUrl().isBlank()) {
        throw new GeneralException(ErrorStatus.CONTENT_IMAGE_REQUIRED);
      }
    }
    if (command.tags() == null) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }
    if (command.tags().size() > 5) {
      throw new GeneralException(ErrorStatus.CONTENT_TAG_SIZE_EXCEEDED);
    }
    for (String tag : command.tags()) {
      if (tag == null || tag.isBlank()) {
        throw new GeneralException(ErrorStatus.CONTENT_TAG_REQUIRED);
      }
      if (tag.length() > 10) {
        throw new GeneralException(ErrorStatus.CONTENT_TAG_LENGTH_EXCEEDED);
      }
    }
  }

  private void applyUpdate(Content content, ContentCreateCommand command) {
    content.updateText(command.text());

    content.getTags().clear();
    for (String tagValue : command.tags()) {
      content.addTag(new ContentTag(tagValue));
    }

    content.getImages().clear();
    for (ContentImageDto spec : command.images()) {
      ContentImage image =
          new ContentImage(
              spec.imageUrl(), spec.isPrimary(), spec.sortOrder() != null ? spec.sortOrder() : 0);
      if (Boolean.TRUE.equals(spec.isPrimary())) {
        content.updateMainImageUrl(spec.imageUrl());
      }
      content.addImage(image);
    }
  }
}
