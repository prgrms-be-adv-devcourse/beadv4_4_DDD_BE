package com.modeunsa.boundedcontext.member.out.repository;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {
  // 1. ID로 조회할 때 Profile까지 한방에 가져오기
  @Query("SELECT m FROM Member m LEFT JOIN FETCH m.profile WHERE m.id = :id")
  Optional<Member> findByIdWithProfile(@Param("id") Long id);

  // 2. 전체 목록 조회할 때 N+1 방지용
  @Query("SELECT m FROM Member m LEFT JOIN FETCH m.profile")
  List<Member> findAllWithProfile();
}
