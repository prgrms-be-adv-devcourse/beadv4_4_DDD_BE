package com.modeunsa.boundedcontext.member.in.datainit;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
// @Configuration // 필요 시 주석 해제
public class MemberDataInit {

  private final MemberDataInit self;
  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;
  private final EventPublisher eventPublisher;
  private final JdbcTemplate jdbcTemplate; // ID 리셋을 위해 추가

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
      // 데이터가 이미 있으면 초기화 건너뜀
      if (memberRepository.count() > 0) {
        log.info("[Init] Member data already exists, skipping initialization");
        return;
      }

      // ID를 1부터 시작하도록 강제 리셋 (System=1, Holder=2, Admin=3 보장)
      resetAutoIncrement();

      // 데이터 생성 시작
      self.initBaseData();
    };
  }

  private void resetAutoIncrement() {
    log.info("[Init] Resetting AutoIncrement to 1...");
    try {
      jdbcTemplate.execute("ALTER TABLE member_member AUTO_INCREMENT = 1");
    } catch (Exception e) {
      log.warn("[Init] Failed to reset AutoIncrement. (Check DB permissions): {}", e.getMessage());
    }
  }

  @Transactional
  public void initBaseData() {
    log.info("[Init] Initializing member base data...");

    // 1. 시스템 계정 (ID: 1)
    Member systemMember =
        createMember("system@modeunsa.com", "SYSTEM", "010-0000-0001", MemberRole.SYSTEM);
    memberRepository.save(systemMember);
    publishSignupEvent(systemMember);

    // 2. 홀더 계정 (ID: 2)
    Member holderMember =
        createMember("holder@modeunsa.com", "HOLDER", "010-0000-0002", MemberRole.HOLDER);
    memberRepository.save(holderMember);
    publishSignupEvent(holderMember);

    // 3. 관리자 회원 (ID: 3)
    Member admin = createMember("admin@modeunsa.com", "관리자", "010-0000-0003", MemberRole.ADMIN);
    createProfile(admin, "MainAdmin", null, null, null, null);
    memberRepository.save(admin);
    publishSignupEvent(admin);

    // --- 여기부터 개발용 일반/판매자 데이터 (ID: 4~) ---

    // 일반 회원 1 - 카카오 로그인
    Member user1 = createMember("user1@example.com", "김모든", "010-1111-1111", MemberRole.MEMBER);
    createProfile(user1, "모든이", "https://example.com/profile1.jpg", 175, 70, "지성");
    createDefaultAddress(
        user1, "김모든", "010-1111-1111", "06234", "서울시 강남구 테헤란로 123", "101동 1001호", "집");
    addOAuthAccount(user1, OAuthProvider.KAKAO, "kakao_12345");
    memberRepository.save(user1);
    publishSignupEvent(user1);

    // 일반 회원 2 - 네이버 로그인
    Member user2 = createMember("user2@example.com", "이사람", "010-2222-2222", MemberRole.MEMBER);
    createProfile(user2, "사람이", "https://example.com/profile2.jpg", 165, 55, "건성");
    createDefaultAddress(user2, "이사람", "010-2222-2222", "04524", "서울시 중구 명동길 45", "203호", "회사");
    addOAuthAccount(user2, OAuthProvider.NAVER, "naver_67890");
    memberRepository.save(user2);
    publishSignupEvent(user2);

    // 일반 회원 3 - 복수 소셜 계정 연동
    Member user3 = createMember("user3@example.com", "박연동", "010-3333-3333", MemberRole.MEMBER);
    createProfile(user3, "연동이", null, 180, 75, "복합성");
    createDefaultAddress(
        user3, "박연동", "010-3333-3333", "13494", "경기도 성남시 분당구 판교로 256", "A동 502호", "집");
    addAddress(user3, "박연동", "010-3333-3333", "06164", "서울시 강남구 삼성로 512", "15층", "회사", false);
    addOAuthAccount(user3, OAuthProvider.KAKAO, "kakao_11111");
    addOAuthAccount(user3, OAuthProvider.NAVER, "naver_22222");
    memberRepository.save(user3);
    publishSignupEvent(user3);

    // 일반 회원 4 - 프로필만 있는 회원 (배송지 없음)
    Member user4 = createMember("user4@example.com", "강신규", "010-6666-6666", MemberRole.MEMBER);
    createProfile(user4, "신규회원", null, null, null, null);
    addOAuthAccount(user4, OAuthProvider.KAKAO, "kakao_newuser");
    memberRepository.save(user4);
    publishSignupEvent(user4);

    // 판매자 회원
    Member seller1 =
        createMember(
            "seller1@example.com", "최판매", "010-4444-4444", MemberRole.MEMBER); // 초기 생성은 MEMBER로
    createProfile(seller1, "판매왕", "https://example.com/seller1.jpg", null, null, null);
    createDefaultAddress(
        seller1, "최판매", "010-4444-4444", "07281", "서울시 영등포구 여의대로 108", "1201호", "사무실");
    addOAuthAccount(seller1, OAuthProvider.KAKAO, "kakao_seller1");
    memberRepository.save(seller1);
    publishSignupEvent(seller1); // 멤버 가입 이벤트 먼저 발행

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

    // approve() 호출 시 member의 role이 SELLER로 변경됨
    activeSeller.approve();
    memberSellerRepository.save(activeSeller);
    publishSellerRegisteredEvent(activeSeller);

    log.info(
        "[Init] Member base data initialization completed. Total members: {}",
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

  // Status 기본값을 ACTIVE로 설정
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
