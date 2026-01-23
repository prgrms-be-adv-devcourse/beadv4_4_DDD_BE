package com.modeunsa.boundedcontext.content.app.usecase;

import static com.modeunsa.global.status.ErrorStatus.CONTENT_NOT_FOUND;

import com.modeunsa.boundedcontext.content.app.dto.ContentCommentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentCommentResponse;
import com.modeunsa.boundedcontext.content.app.mapper.ContentMapper;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentComment;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import com.modeunsa.boundedcontext.content.out.ContentCommentRepository;
import com.modeunsa.boundedcontext.content.out.ContentRepository;
import com.modeunsa.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentCreateCommentUseCase {

  private final ContentRepository contentRepository;
  private final ContentCommentRepository commentRepository;
  private final ContentMapper contentMapper;

  public ContentCommentResponse createContentComment(
      Long contentId, ContentCommentRequest request, ContentMember author) {

    Content content =
        contentRepository
            .findByIdAndDeletedAtIsNull(contentId)
            .orElseThrow(() -> new GeneralException(CONTENT_NOT_FOUND));

    ContentComment comment = ContentComment.createComment(content, author, request.getText());

    content.addComment(comment);
    commentRepository.save(comment);

    return contentMapper.toResponse(comment);
  }
}
