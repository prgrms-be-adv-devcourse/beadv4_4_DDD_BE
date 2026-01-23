package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.domain.entity.ContentComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCommentRepository extends JpaRepository<ContentComment, Long> {}
