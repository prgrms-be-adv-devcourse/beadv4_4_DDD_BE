package com.modeunsa.boundedcontext.member.out.repository;

import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {}
