package com.modeunsa.boundedcontext.content.app;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentSupport {

  private final ContentRepository contentRepository;

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
}