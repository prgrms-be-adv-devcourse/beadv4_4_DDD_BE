package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.mapper.ContentMapper;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentImage;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.domain.entity.ContentTag;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentCreateContentUseCase {

  private final ContentRepository contentRepository;
  private final ContentMapper contentMapper;

  @Transactional
  public ContentResponse createContent(ContentRequest contentRequest, ContentMember author) {
    this.validateContent(contentRequest);
    Content content = contentMapper.toEntity(contentRequest);

    // 작성자 설정
    content.setAuthor(author);

    // 태그 생성 및 연관관계 설정
    contentRequest.getTags().forEach(tagValue -> content.addTag(new ContentTag(tagValue)));

    // 이미지 생성 및 연관관계 설정
    contentRequest
        .getImages()
        .forEach(
            imageRequest ->
                content.addImage(
                    new ContentImage(
                        imageRequest.getImageUrl(),
                        imageRequest.getIsPrimary(),
                        imageRequest.getSortOrder())));

    // 저장
    contentRepository.save(content);

    // 응답 변환
    return contentMapper.toResponse(content);
  }

  // 예외 처리
  private void validateContent(ContentRequest contentRequest) {
    // 텍스트 검증
    if (contentRequest.getText() == null || contentRequest.getText().isBlank()) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }

    if (contentRequest.getText().length() > 500) {
      throw new GeneralException(ErrorStatus.CONTENT_TEXT_LIMIT_EXCEEDED);
    }

    // 태그 검증
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

    // 이미지 검증
    if (contentRequest.getImages() == null) {
      throw new GeneralException(ErrorStatus.VALIDATION_ERROR);
    }

    for (var image : contentRequest.getImages()) {
      if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
        throw new GeneralException(ErrorStatus.CONTENT_IMAGE_LIMIT_EXCEEDED);
      }
    }
  }
}
