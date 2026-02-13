package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentMapper;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
  public Page<ContentResponse> getContents(int page) {

    PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

    return contentRepository
        .findByDeletedAtIsNullOrderByIdDesc(pageRequest)
        .map(contentMapper::toResponse);
  }
}
