package com.modeunsa.boundedcontext.member.in.datainit;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@Profile("prod") // 필요 시 "dev", "!test" 등 원하는 프로필 지정
public class MemberDataInitProd {

  private final MemberRepository memberRepository;
  private final EventPublisher eventPublisher;
  private final JdbcTemplate jdbcTemplate; // ID 리셋을 위해 추가
  private final MemberDataInitProd self; // 자기 자신 주입 필드 추가

  // 생성자에서 @Lazy로 자기 자신(Proxy) 주입
  public MemberDataInitProd(
      MemberRepository memberRepository,
      EventPublisher eventPublisher,
      JdbcTemplate jdbcTemplate,
      @Lazy MemberDataInitProd self) {
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
    this.jdbcTemplate = jdbcTemplate;
    this.self = self;
  }

  @Bean
  @Order(1)
  public ApplicationRunner initSystemAccounts() {
    return args -> {
      // 데이터가 하나라도 있으면 초기화 로직 스킵 (운영 환경 데이터 보호)
      if (memberRepository.count() > 0) {
        log.info("[Init] 회원이 존재하여 초기화를 건너뜁니다.");
        return;
      }

      // 데이터가 0개일 때만 실행
      resetAutoIncrement(); // ID를 1번부터 시작하도록 강제 리셋
      createBaseAccounts();
    };
  }

  // ID 카운터를 1로 리셋하는 메서드
  private void resetAutoIncrement() {
    log.info("[Init] ID Sequence를 1로 리셋합니다.");
    try {
      jdbcTemplate.execute("ALTER TABLE member_member AUTO_INCREMENT = 1");
    } catch (Exception e) {
      log.warn("[Init] AutoIncrement 리셋 실패: {}", e.getMessage());
    }
  }

  @Transactional
  public void createBaseAccounts() {
    log.info("[Init] 시스템 계정(System, Holder, Admin) 생성 시작");

    // 1. System (ID: 1)
    Member systemUser =
        Member.builder()
            .role(MemberRole.SYSTEM)
            .status(MemberStatus.ACTIVE)
            .email("system@modeunsa.com")
            .realName("SYSTEM")
            .phoneNumber("010-0000-0001")
            .build();
    memberRepository.save(systemUser);
    publishSignupEvent(systemUser);

    // 2. Holder (ID: 2)
    Member holderUser =
        Member.builder()
            .role(MemberRole.HOLDER)
            .status(MemberStatus.ACTIVE)
            .email("holder@modeunsa.com")
            .realName("HOLDER")
            .phoneNumber("010-0000-0002")
            .build();
    memberRepository.save(holderUser);
    publishSignupEvent(holderUser);

    // 3. Admin (ID: 3)
    Member adminUser =
        Member.builder()
            .role(MemberRole.ADMIN)
            .status(MemberStatus.ACTIVE)
            .email("admin@modeunsa.com")
            .realName("관리자")
            .phoneNumber("010-0000-0003")
            .build();

    // Admin 프로필 추가
    MemberProfile profile = MemberProfile.builder().nickname("MainAdmin").build();
    adminUser.setProfile(profile);

    memberRepository.save(adminUser);
    publishSignupEvent(adminUser);

    log.info("[Init] 초기화 완료. System(1), Holder(2), Admin(3) 생성됨.");
  }

  private void publishSignupEvent(Member member) {
    eventPublisher.publish(
        new MemberSignupEvent(
            member.getId(),
            member.getRealName(),
            member.getEmail(),
            member.getPhoneNumber(),
            member.getRole().name(),
            member.getStatus().name()));
  }
}
