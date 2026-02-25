package com.modeunsa.boundedcontext.content.out.persistence.content;

import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import com.modeunsa.boundedcontext.content.out.ContentReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaContentReader implements ContentReader {

  private final ContentQueryRepository queryRepository;

  @Override
  public ContentDetailDto findContentById(Long contentId) {
    Optional<ContentDetailDto> find = queryRepository.findContentById(contentId);
    if (find.isEmpty()) {
      throw new IllegalArgumentException("Content not found with id: " + contentId);
    }

    return find.get();
  }
}
