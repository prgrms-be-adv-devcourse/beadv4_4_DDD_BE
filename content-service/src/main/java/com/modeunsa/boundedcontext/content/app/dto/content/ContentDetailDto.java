package com.modeunsa.boundedcontext.content.app.dto.content;

import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import java.util.List;
import lombok.Getter;

@Getter
public class ContentDetailDto extends ContentDto {

  private List<ContentImageDto> images;
  private List<String> tags;

  public ContentDetailDto(Content content) {
    super(content);
  }
}
