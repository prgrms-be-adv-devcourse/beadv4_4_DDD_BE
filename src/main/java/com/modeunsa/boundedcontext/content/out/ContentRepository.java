package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

  long countByDeletedAtIsNull();

  Optional<Content> findByIdAndDeletedAtIsNull(Long id);

  // 삭제시각이 없는 콘텐츠 조회하여 최신순 정렬
  Page<Content> findByDeletedAtIsNullOrderByIdDesc(Pageable pageable);
}
