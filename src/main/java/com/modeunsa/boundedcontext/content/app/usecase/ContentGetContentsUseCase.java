package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.app.mapper.ContentMapper;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentGetContentsUseCase {

  private static final int PAGE_SIZE = 20;
  private final ContentRepository contentRepository;
  private final ContentMapper contentMapper;

  @Transactional
  public List<ContentResponse> getContents(int page) {

    PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
    return contentRepository.findByDeletedAtIsNullOrderByIdDesc(pageRequest).getContent().stream()
        .map(contentMapper::toResponse)
        .toList();
  }
}
