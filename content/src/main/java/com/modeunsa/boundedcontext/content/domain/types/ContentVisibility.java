package com.modeunsa.boundedcontext.content.domain.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentVisibility {
  PUBLIC("공개"),
  PRIVATE("비공개");

  private final String description;
}
