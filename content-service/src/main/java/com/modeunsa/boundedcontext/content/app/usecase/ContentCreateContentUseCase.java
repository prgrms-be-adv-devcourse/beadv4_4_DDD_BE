package com.modeunsa.boundedcontext.content.app.usecase;

import com.modeunsa.boundedcontext.content.app.ContentSupport;
import com.modeunsa.boundedcontext.content.app.dto.content.ContentCreateCommand;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.ContentStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContentCreateContentUseCase {

  private final ContentStore contentStore;
  private final ContentSupport contentSupport;

  public void execute(Long memberId, ContentCreateCommand command) {
    ContentMember author = contentSupport.getContentMemberById(memberId);
    Content content =
        Content.create(author, command.title(), command.text(), command.tags(), command.images());
    contentStore.store(content);
  }
}
