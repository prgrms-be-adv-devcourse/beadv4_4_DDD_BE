package com.modeunsa.boundedcontext.content.out.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ContentSearchMapper {

  // "#봄_코디 #가을룩" → ["봄","코디","가을룩"]
  public List<String> parseTags(String rawTags) {
    if (rawTags == null || rawTags.isBlank()) {
      return List.of();
    }

    return Arrays.stream(rawTags.split("\\s+"))
        .map(tag -> tag.replace("#", ""))
        .flatMap(tag -> Arrays.stream(tag.split("_")))
        .filter(s -> !s.isBlank())
        .collect(Collectors.toList());
  }
}
