package com.modeunsa.boundedcontext.content.app;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentImage;
import com.modeunsa.boundedcontext.content.domain.entity.ContentTag;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentWriteUseCase {

  private final ContentSupport contentSupport;

  // 생성
  @Transactional
  public Content create(
    Long authorUserId,
    String text,
    List<String> tags,
    List<ImagePayload> images
  ) {
    Content content = Content.create(authorUserId, text);

    applyTags(content, tags);
    applyImages(content, images);

    return contentSupport.save(content);
  }

  // 수정
  @Transactional
  public Content update(
    Long contentId,
    Long requesterId,
    String newText,
    List<String> tags,
    List<ImagePayload> images
  ) {
    Content content =
      contentSupport
        .findById(contentId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND));

    if (!content.isAuthor(requesterId)) {
      throw new GeneralException(ErrorStatus._FORBIDDEN);
    }

    content.update(requesterId, newText);

    content.clearTags();
    content.clearImages();

    applyTags(content, tags);
    applyImages(content, images);

    return content;
  }

  // 삭제
  @Transactional
  public void delete(Long contentId, Long requesterId) {
    Content content =
      contentSupport
        .findById(contentId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND));

    if (!content.isAuthor(requesterId)) {
      throw new GeneralException(ErrorStatus._FORBIDDEN);
    }

    content.delete(requesterId);
  }

  // 태그 적용 (필수 값 전제)
  private void applyTags(Content content, List<String> tags) {
    for (String value : tags) {
      content.addTag(
        ContentTag.builder()
          .value(value.trim())
          .build()
      );
    }
  }

  // 이미지 적용 (필수 값 전제)
  private void applyImages(Content content, List<ImagePayload> images) {
    for (ImagePayload image : images) {
      ContentImage contentImage =
        ContentImage.builder()
          .imageUrl(image.imageUrl().trim())
          .isPrimary(image.isPrimary())
          .sortOrder(image.sortOrder())
          .build();

      content.addImage(contentImage);
    }
  }

  // 내부 이미지 입력 모델
  public record ImagePayload(
    String imageUrl,
    Boolean isPrimary,
    Integer sortOrder
  ) {}
}