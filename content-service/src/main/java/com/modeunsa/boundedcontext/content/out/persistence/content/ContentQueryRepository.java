package com.modeunsa.boundedcontext.content.out.persistence.content;

import static com.modeunsa.boundedcontext.content.domain.entity.QContent.content;

import com.modeunsa.boundedcontext.content.app.dto.content.ContentDetailDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepository {

  private final JPAQueryFactory queryFactory;

  public Optional<ContentDetailDto> findContentById(Long contentId) {
    return Optional.ofNullable(
        this.queryFactory
            .select(Projections.constructor(ContentDetailDto.class, content))
            .from(content)
            .where(eqContentId(contentId))
            .limit(1)
            .fetchFirst());
  }

  private BooleanExpression eqContentId(Long contentId) {
    return contentId != null ? content.id.eq(contentId) : null;
  }
}
