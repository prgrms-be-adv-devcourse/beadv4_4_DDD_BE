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
  private final JdbcTemplate jdbcTemplate;
  private final MemberDataInitProd self;

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
      if (memberRepository.count() > 0) {
        log.info("[Init] 회원이 존재하여 초기화를 건너뜁니다.");
        return;
      }

      // 1. 초기화 시작 전: ID를 1번부터 시작하도록 리셋 (시스템 계정용)
      setSafeAutoIncrement(1);

      // 2. 시스템 계정 3개 생성 (ID 1, 2, 3 점유)
      self.createBaseAccounts();

      // 3. 초기화 완료 후: 다음 ID가 무조건 4번부터 시작하도록 강제 설정 (예약 영역 확보)
      // 만약 createBaseAccounts에서 2개만 만들었더라도, 다음 유저는 4번이 됨.
      setSafeAutoIncrement(4);
    };
  }

  private void setSafeAutoIncrement(long startValue) {
    log.info("[Init] ID AutoIncrement 값을 {}로 설정합니다.", startValue);
    try {
      jdbcTemplate.execute("ALTER TABLE member_member AUTO_INCREMENT = " + startValue);
    } catch (Exception e) {
      log.warn("[Init] AutoIncrement 설정 실패 (DB 권한 확인 필요): {}", e.getMessage());
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
