package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.domain.entity.Content;

public interface ContentStore {
  Content store(Content newContent);
}
