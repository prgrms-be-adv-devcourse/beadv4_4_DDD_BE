package com.modeunsa.boundedcontext.member.out.repository;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {}
