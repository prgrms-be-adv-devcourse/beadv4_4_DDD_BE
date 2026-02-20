package com.modeunsa.boundedcontext.content.in.data;

import com.modeunsa.boundedcontext.content.app.ContentFacade;
import com.modeunsa.boundedcontext.content.app.dto.content.ContentCreateCommand;
import com.modeunsa.boundedcontext.content.app.dto.image.ContentImageDto;
import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
public class ContentDataInit {

  private final ContentDataInit self;
  private final ContentFacade contentFacade;

  public ContentDataInit(@Lazy ContentDataInit self, ContentFacade contentFacade) {
    this.self = self;
    this.contentFacade = contentFacade;
  }

  @Bean
  @Order(2)
  public ApplicationRunner contentDataInitApplicationRunner() {
    return args -> self.makeBaseContents();
  }

  public void makeBaseContents() {
    ContentMember user1Member = contentFacade.findMemberById(1L);
    ContentMember user2Member = contentFacade.findMemberById(2L);

    contentFacade.createContent(
        user1Member.getId(),
        new ContentCreateCommand(
            "첫 번째 콘텐츠 내용입니다.",
            List.of("테스트"),
            List.of(new ContentImageDto("https://example.com/placeholder.png", true, 0))));

    contentFacade.createContent(
        user2Member.getId(),
        new ContentCreateCommand(
            "두 번째 콘텐츠 내용입니다.",
            List.of("샘플"),
            List.of(new ContentImageDto("https://example.com/placeholder.png", true, 0))));
  }
}
