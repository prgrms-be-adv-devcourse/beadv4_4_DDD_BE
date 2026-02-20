package com.modeunsa.boundedcontext.content.app.dto.content;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import lombok.Getter;

@Getter
public class ContentDto {

  private final Long contentId;

  private final String title;

  private final String text;

  private final String mainImageUrl;

  private final int likeCount;

  private final int commentCount;

  public ContentDto(Content c) {
    this.contentId = c.getId();
    this.title = c.getTitle();
    this.text = c.getText();
    this.mainImageUrl = c.getMainImageUrl();
    this.likeCount = c.getLikeCount();
    this.commentCount = c.getCommentCount();
  }
}
