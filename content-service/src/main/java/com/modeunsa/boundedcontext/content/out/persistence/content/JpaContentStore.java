package com.modeunsa.boundedcontext.content.out.persistence.content;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.out.ContentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaContentStore implements ContentStore {

  private final ContentRepository contentRepository;

  @Override
  public Content store(Content newContent) {
    return contentRepository.save(newContent);
  }
}
