package com.modeunsa.boundedcontext.content.app.dto.content;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ContentDetailDto extends ContentDto {

  @Builder.Default private boolean empty = false;

  private static final ContentDetailDto EMPTY;

  static {
    EMPTY = ContentDetailDto.builder().empty(true).build();
  }

  public static ContentDetailDto emptyDto() {
    return EMPTY;
  }
}
