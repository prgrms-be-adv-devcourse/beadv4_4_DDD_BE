package com.modeunsa.boundedcontext.content.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentStatus {
  DRAFT("임시저장"),
  COMPLETED("수정완료");

  private final String description;
}
