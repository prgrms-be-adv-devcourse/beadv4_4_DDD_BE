package com.modeunsa.boundedcontext.content.app;

import static com.modeunsa.global.status.ErrorStatus.CONTENT_MEMBER_NOT_FOUND;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.ContentMemberRepository;
import com.modeunsa.boundedcontext.content.out.persistence.content.ContentRepository;
import com.modeunsa.global.exception.GeneralException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentSupport {

  private final ContentRepository contentRepository;
  private final ContentMemberRepository contentMemberRepository;

  public Content save(Content content) {
    return contentRepository.save(content);
  }

  public long count() {
    return contentRepository.countByDeletedAtIsNull();
  }

  public Optional<Content> findById(Long id) {
    return contentRepository.findByIdAndDeletedAtIsNull(id);
  }

  public Page<Content> findLatest(Pageable pageable) {
    return contentRepository.findByDeletedAtIsNullOrderByIdDesc(pageable);
  }

  public ContentMember getContentMemberById(Long memberId) {
    return contentMemberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(CONTENT_MEMBER_NOT_FOUND));
  }
}

// repository를 감싸는 클래스, 바로 repository로 가지 않게
