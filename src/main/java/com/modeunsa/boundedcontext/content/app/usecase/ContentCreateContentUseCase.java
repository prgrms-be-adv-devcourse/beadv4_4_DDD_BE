package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentMapper;
import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentImage;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.domain.entity.ContentTag;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
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
}
