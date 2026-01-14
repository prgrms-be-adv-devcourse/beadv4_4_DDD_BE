package com.modeunsa.boundedcontext.content.app;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentFacade {

  private final ContentSupport contentSupport;
  private final ContentWriteUseCase contentWriteUseCase;

  @Transactional
  public Content create(
    Long authorUserId,
    String text,
    List<String> tags,
    List<ContentWriteUseCase.ImagePayload> images
  ) {
    return contentWriteUseCase.create(authorUserId, text, tags, images);
  }

  @Transactional
  public Content update(
    Long contentId,
    Long requesterId,
    String text,
    List<String> tags,
    List<ContentWriteUseCase.ImagePayload> images
  ) {
    return contentWriteUseCase.update(contentId, requesterId, text, tags, images);
  }

  @Transactional
  public void delete(Long contentId, Long requesterId) {
    contentWriteUseCase.delete(contentId, requesterId);
  }

  @Transactional(readOnly = true)
  public Content findById(Long id) {
    return contentSupport
      .findById(id)
      .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public Page<Content> findLatest(Pageable pageable) {
    return contentSupport.findLatest(pageable);
  }

  @Transactional(readOnly = true)
  public long count() {
    return contentSupport.count();
  }
}