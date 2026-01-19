package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.domain.entity.Content;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

  long countByDeletedAtIsNull();

  Optional<Content> findByIdAndDeletedAtIsNull(Long id);

  Page<Content> findByDeletedAtIsNullOrderByIdDesc(Pageable pageable);
}
