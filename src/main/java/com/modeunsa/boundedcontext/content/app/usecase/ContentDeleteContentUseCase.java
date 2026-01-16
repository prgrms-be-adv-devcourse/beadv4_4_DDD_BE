package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentSupport;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentDeleteContentUseCase {

  private final ContentSupport contentSupport;

  @Transactional
  public void deleteContent(Long contentId, ContentMember author) {
    Content content =
        contentSupport
            .findById(contentId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._NOT_FOUND));

    validateDelete(content, author);

    content.delete(); // 도메인 행위
  }

  private void validateDelete(Content content, ContentMember author) {
    // 작성자 검증
    if (!content.getAuthor().equals(author)) {
      throw new GeneralException(ErrorStatus._FORBIDDEN);
    }

    // 이미 삭제된 콘텐츠 방어
    if (content.isDeleted()) {
      throw new GeneralException(ErrorStatus._BAD_REQUEST);
    }
  }
}
