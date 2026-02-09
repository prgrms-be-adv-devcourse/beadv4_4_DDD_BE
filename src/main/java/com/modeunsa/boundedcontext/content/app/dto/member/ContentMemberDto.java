package com.modeunsa.boundedcontext.content.app.dto.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContentMemberDto {

  private final Long memberId;

  private final String email;

  private final String author;
}
