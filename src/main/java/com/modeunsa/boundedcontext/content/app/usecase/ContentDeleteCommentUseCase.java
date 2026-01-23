package com.modeunsa.boundedcontext.content.app.usecase;

import static com.modeunsa.global.status.ErrorStatus.CONTENT_COMMENT_NOT_FOUND;
import static com.modeunsa.global.status.ErrorStatus.CONTENT_NOT_FOUND;

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
public class ContentDeleteCommentUseCase {

  private final ContentRepository contentRepository;
  private final ContentCommentRepository commentRepository;

  public void deleteContentComment(Long contentId, Long commentId, ContentMember author) {

    Content content =
        contentRepository
            .findByIdAndDeletedAtIsNull(contentId)
            .orElseThrow(() -> new GeneralException(CONTENT_NOT_FOUND));

    ContentComment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new GeneralException(CONTENT_COMMENT_NOT_FOUND));

    // 댓글이 해당 콘텐츠에 속한 게 맞는지 방어
    if (!comment.getContent().getId().equals(content.getId())) {
      throw new GeneralException(CONTENT_COMMENT_NOT_FOUND);
    }

    // (선택) 작성자 본인만 삭제 가능하게 하고 싶다면
    if (!comment.getAuthor().equals(author)) {
      throw new GeneralException(CONTENT_COMMENT_NOT_FOUND);
    }

    content.removeComment(comment);
    commentRepository.delete(comment);
  }
}
