package com.modeunsa.boundedcontext.content.out;

import com.modeunsa.boundedcontext.content.domain.entity.ContentMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentMemberRepository extends JpaRepository<ContentMember, Long> {}
