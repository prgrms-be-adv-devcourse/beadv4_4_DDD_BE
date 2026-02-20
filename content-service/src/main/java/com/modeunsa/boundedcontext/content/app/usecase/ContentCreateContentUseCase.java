package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentSupport;
import com.modeunsa.boundedcontext.content.app.dto.content.ContentCreateCommand;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentCreateContentUseCase {

  private final ContentRepository contentRepository;
  private final ContentSupport contentSupport;

  public void createContent(Long memberId, ContentCreateCommand command) {
    ContentMember author = contentSupport.getContentMemberById(memberId);
    Content content = Content.create(author, command);
    contentRepository.save(content);
  }
}
