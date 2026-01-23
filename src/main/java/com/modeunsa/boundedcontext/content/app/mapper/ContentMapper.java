package com.modeunsa.boundedcontext.content.app.mapper;

import com.modeunsa.boundedcontext.content.app.dto.ContentRequest;
import com.modeunsa.boundedcontext.content.app.dto.ContentResponse;
import com.modeunsa.boundedcontext.content.domain.entity.Content;
import com.modeunsa.boundedcontext.content.domain.entity.ContentComment;
import com.modeunsa.shared.content.ContentCommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContentMapper {

  @Mapping(target = "author", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "images", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  Content toEntity(ContentRequest request);

  @Mapping(source = "id", target = "contentId")
  @Mapping(source = "author.id", target = "authorMemberId")
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "images", ignore = true)
  ContentResponse toResponse(Content content);

  @Mapping(source = "id", target = "commentId")
  @Mapping(source = "author.author", target = "authorNickname")
  ContentCommentResponse toResponse(ContentComment comment);
}
