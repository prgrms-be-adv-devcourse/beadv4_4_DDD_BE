package com.modeunsa.boundedcontext.member.in.datainit;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.member.MemberRole;
import com.modeunsa.shared.member.MemberStatus;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Profile("k3s-prod")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@Configuration // 필요 시 주석 처리
public class MemberDataInit {

  private final MemberDataInit self;
  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;
  private final EventPublisher eventPublisher;
  private final JdbcTemplate jdbcTemplate;

  public MemberDataInit(
      @Lazy MemberDataInit self,
      MemberRepository memberRepository,
      MemberSellerRepository memberSellerRepository,
      EventPublisher eventPublisher,
      JdbcTemplate jdbcTemplate) {
    this.self = self;
    this.memberRepository = memberRepository;
    this.memberSellerRepository = memberSellerRepository;
    this.eventPublisher = eventPublisher;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Bean
  @Order(1)
  public ApplicationRunner memberDataInitRunner() {
    return args -> {
      if (memberRepository.count() > 0) {
        log.debug("[Init] Member data already exists, skipping initialization");
        return;
      }

      // 1. 초기화 시작 전: ID 1번부터 시작
      setSafeAutoIncrement(1);

      // 2. 시스템 계정 3개 생성 (1~3번 점유)
      self.initSystemAccounts();

      // 3. 시스템 계정 생성 직후: ID 4번부터 시작하도록 강제 설정 (예약 영역 확보)
      setSafeAutoIncrement(4);

      // 4. 일반/판매자 테스트 데이터 생성 (4번부터 시작)
      self.initDevData();
    };
  }

  private void setSafeAutoIncrement(long startValue) {
    log.debug("[Init] ID AutoIncrement 값을 {}로 설정합니다.", startValue);
    try {
      jdbcTemplate.execute("ALTER TABLE member_member AUTO_INCREMENT = " + startValue);
    } catch (Exception e) {
      log.warn("[Init] AutoIncrement 설정 실패 (DB 권한 확인 필요): {}", e.getMessage());
    }
  }

  @Transactional
  public void initSystemAccounts() {
    log.debug("[Init] 시스템 계정(System, Holder, Admin) 생성 시작");

    // 1. 시스템 계정 (ID: 1)
    Member systemMember =
        createMember("system@modeunsa.com", "SYSTEM", "010-0000-0001", MemberRole.SYSTEM);
    createProfile(
        systemMember,
        "시스템",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        null,
        null,
        null);
    memberRepository.save(systemMember);
    publishSignupEvent(systemMember);

    // 2. 홀더 계정 (ID: 2)
    Member holderMember =
        createMember("holder@modeunsa.com", "HOLDER", "010-0000-0002", MemberRole.HOLDER);
    createProfile(
        holderMember,
        "홀더",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        null,
        null,
        null);
    memberRepository.save(holderMember);
    publishSignupEvent(holderMember);

    // 3. 관리자 회원 (ID: 3)
    Member admin = createMember("admin@modeunsa.com", "관리자", "010-0000-0003", MemberRole.ADMIN);
    createProfile(
        admin,
        "관리자",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        null,
        null,
        null);
    memberRepository.save(admin);
    publishSignupEvent(admin);

    log.debug("[Init] 시스템 계정(System, Holder, Admin) 생성 완료");
  }

  @Transactional
  public void initDevData() {
    log.debug("[Init] Initializing Dev/Test Data (Users, Sellers)...");

    // 일반 회원 1 - 카카오 로그인 (ID: 4 예상)
    Member user1 = createMember("user1@example.com", "김모든", "010-1111-1111", MemberRole.MEMBER);
    createProfile(
        user1,
        "모든이",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        175,
        70,
        "지성");
    createDefaultAddress(
        user1, "김모든", "010-1111-1111", "06234", "서울시 강남구 테헤란로 123", "101동 1001호", "집");
    addOAuthAccount(user1, OAuthProvider.KAKAO, "kakao_12345");
    memberRepository.save(user1);
    publishSignupEvent(user1);

    // 일반 회원 2 - 네이버 로그인
    Member user2 = createMember("user2@example.com", "이사람", "010-2222-2222", MemberRole.MEMBER);
    createProfile(
        user2,
        "사람이",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        165,
        55,
        "건성");
    createDefaultAddress(user2, "이사람", "010-2222-2222", "04524", "서울시 중구 명동길 45", "203호", "회사");
    addOAuthAccount(user2, OAuthProvider.NAVER, "naver_67890");
    memberRepository.save(user2);
    publishSignupEvent(user2);

    // 일반 회원 3 - 복수 소셜 계정 연동
    Member user3 = createMember("user3@example.com", "박연동", "010-3333-3333", MemberRole.MEMBER);
    createProfile(
        user3,
        "연동이",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        180,
        75,
        "복합성");
    createDefaultAddress(
        user3, "박연동", "010-3333-3333", "13494", "경기도 성남시 분당구 판교로 256", "A동 502호", "집");
    addAddress(user3, "박연동", "010-3333-3333", "06164", "서울시 강남구 삼성로 512", "15층", "회사", false);
    addOAuthAccount(user3, OAuthProvider.KAKAO, "kakao_11111");
    addOAuthAccount(user3, OAuthProvider.NAVER, "naver_22222");
    memberRepository.save(user3);
    publishSignupEvent(user3);

    // 일반 회원 4 - 프로필만 있는 회원 (배송지 없음)
    Member user4 = createMember("user4@example.com", "강신규", "010-6666-6666", MemberRole.MEMBER);
    createProfile(
        user4,
        "신규회원",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        null,
        null,
        null);
    addOAuthAccount(user4, OAuthProvider.KAKAO, "kakao_newuser");
    memberRepository.save(user4);
    publishSignupEvent(user4);

    // 판매자 회원
    Member seller1 = createMember("seller1@example.com", "최판매", "010-4444-4444", MemberRole.MEMBER);
    createProfile(
        seller1,
        "판매왕",
        "https://team01-storage.s3.ap-northeast-2.amazonaws.com/dev/member/e7534e74-61ff-4a1f-b10d-1fb5ff6a3dc0.jpg",
        null,
        null,
        null);
    createDefaultAddress(
        seller1, "최판매", "010-4444-4444", "07281", "서울시 영등포구 여의대로 108", "1201호", "사무실");
    addOAuthAccount(seller1, OAuthProvider.KAKAO, "kakao_seller1");
    memberRepository.save(seller1);
    publishSignupEvent(seller1);

    // 판매자 정보 생성 및 승인
    MemberSeller activeSeller =
        MemberSeller.builder()
            .member(seller1)
            .businessName("모든상점")
            .representativeName("최판매")
            .settlementBankName("신한은행")
            .settlementBankAccount("110-123-456789")
            .businessLicenseUrl("https://example.com/license1.pdf")
            .requestedAt(LocalDateTime.now().minusDays(30))
            .build();

    activeSeller.approve();
    memberSellerRepository.save(activeSeller);
    publishSellerRegisteredEvent(activeSeller);

    log.debug(
        "[Init] Member dev data initialization completed. Total members: {}",
        memberRepository.count());
  }

  // --- Helper Methods ---

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

  private void publishSellerRegisteredEvent(MemberSeller seller) {
    eventPublisher.publish(
        new SellerRegisteredEvent(
            seller.getMember().getId(),
            seller.getId(),
            seller.getBusinessName(),
            seller.getRepresentativeName(),
            seller.getSettlementBankName(),
            seller.getSettlementBankAccount(),
            seller.getStatus().name()));
  }

  private Member createMember(String email, String realName, String phoneNumber, MemberRole role) {
    return Member.builder()
        .email(email)
        .realName(realName)
        .phoneNumber(phoneNumber)
        .role(role)
        .status(MemberStatus.ACTIVE)
        .build();
  }

  private void createProfile(
      Member member,
      String nickname,
      String profileImageUrl,
      Integer heightCm,
      Integer weightKg,
      String skinType) {
    MemberProfile profile =
        MemberProfile.builder()
            .nickname(nickname)
            .profileImageUrl(profileImageUrl)
            .heightCm(heightCm)
            .weightKg(weightKg)
            .skinType(skinType)
            .build();
    member.setProfile(profile);
  }

  private void createDefaultAddress(
      Member member,
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName) {
    MemberDeliveryAddress deliveryAddress =
        MemberDeliveryAddress.builder()
            .recipientName(recipientName)
            .recipientPhone(recipientPhone)
            .zipCode(zipCode)
            .address(address)
            .addressDetail(addressDetail)
            .addressName(addressName)
            .isDefault(true)
            .build();
    member.addAddress(deliveryAddress);
  }

  private void addAddress(
      Member member,
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName,
      boolean isDefault) {
    MemberDeliveryAddress deliveryAddress =
        MemberDeliveryAddress.builder()
            .recipientName(recipientName)
            .recipientPhone(recipientPhone)
            .zipCode(zipCode)
            .address(address)
            .addressDetail(addressDetail)
            .addressName(addressName)
            .isDefault(isDefault)
            .build();
    member.addAddress(deliveryAddress);
  }

  private void addOAuthAccount(Member member, OAuthProvider provider, String providerId) {
    OAuthAccount socialAccount =
        OAuthAccount.builder()
            .member(member)
            .oauthProvider(provider)
            .providerId(providerId)
            .build();
    member.addOAuthAccount(socialAccount);
  }
}
