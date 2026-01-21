package com.modeunsa.boundedcontext.member.in.datainit;

import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
@Profile("!prod")
public class MemberDataInit {

  private final MemberDataInit self;
  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;

  public MemberDataInit(
      @Lazy MemberDataInit self,
      MemberRepository memberRepository,
      MemberSellerRepository memberSellerRepository) {
    this.self = self;
    this.memberRepository = memberRepository;
    this.memberSellerRepository = memberSellerRepository;
  }

  @Bean
  @Order(0)
  public ApplicationRunner memberDataInitRunner() {
    return args -> {
      self.initBaseData();
    };
  }

  @Transactional
  public void initBaseData() {
    if (memberRepository.count() > 0) {
      log.info("Member data already exists, skipping initialization");
      return;
    }

    log.info("Initializing member base data...");

    // 시스템 계정
    Member systemMember = createMember(null, "시스템", null, MemberRole.SYSTEM);
    createProfile(systemMember, "SYSTEM", null, null, null, null);
    memberRepository.save(systemMember);

    // 홀더 계정
    Member holderMember = createMember(null, "홀더", null, MemberRole.HOLDER);
    createProfile(holderMember, "HOLDER", null, null, null, null);
    memberRepository.save(holderMember);

    // 관리자 회원
    Member admin = createMember("admin@modeunsa.com", "관리자", "010-0000-0000", MemberRole.ADMIN);
    createProfile(admin, "admin", null, null, null, null);
    memberRepository.save(admin);

    // 일반 회원 - 카카오 로그인
    Member user1 = createMember("user1@example.com", "김모든", "010-1111-1111", MemberRole.MEMBER);
    createProfile(user1, "모든이", "https://example.com/profile1.jpg", 175, 70, "지성");
    createDefaultAddress(
        user1, "김모든", "010-1111-1111", "06234", "서울시 강남구 테헤란로 123", "101동 1001호", "집");
    addOAuthAccount(user1, OAuthProvider.KAKAO, "kakao_12345");
    memberRepository.save(user1);

    // 일반 회원 - 네이버 로그인
    Member user2 = createMember("user2@example.com", "이사람", "010-2222-2222", MemberRole.MEMBER);
    createProfile(user2, "사람이", "https://example.com/profile2.jpg", 165, 55, "건성");
    createDefaultAddress(user2, "이사람", "010-2222-2222", "04524", "서울시 중구 명동길 45", "203호", "회사");
    addOAuthAccount(user2, OAuthProvider.NAVER, "naver_67890");
    memberRepository.save(user2);

    // 일반 회원 - 복수 소셜 계정 연동
    Member user3 = createMember("user3@example.com", "박연동", "010-3333-3333", MemberRole.MEMBER);
    createProfile(user3, "연동이", null, 180, 75, "복합성");
    createDefaultAddress(
        user3, "박연동", "010-3333-3333", "13494", "경기도 성남시 분당구 판교로 256", "A동 502호", "집");
    addAddress(user3, "박연동", "010-3333-3333", "06164", "서울시 강남구 삼성로 512", "15층", "회사", false);
    addOAuthAccount(user3, OAuthProvider.KAKAO, "kakao_11111");
    addOAuthAccount(user3, OAuthProvider.NAVER, "naver_22222");
    memberRepository.save(user3);

    // 판매자 회원 (승인 완료)
    Member seller1 = createMember("seller1@example.com", "최판매", "010-4444-4444", MemberRole.SELLER);
    createProfile(seller1, "판매왕", "https://example.com/seller1.jpg", null, null, null);
    createDefaultAddress(
        seller1, "최판매", "010-4444-4444", "07281", "서울시 영등포구 여의대로 108", "1201호", "사무실");
    addOAuthAccount(seller1, OAuthProvider.KAKAO, "kakao_seller1");
    memberRepository.save(seller1);

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

    // 판매자 신청 대기 회원
    Member seller2 = createMember("seller2@example.com", "정대기", "010-5555-5555", MemberRole.MEMBER);
    createProfile(seller2, "예비판매자", null, null, null, null);
    addOAuthAccount(seller2, OAuthProvider.NAVER, "naver_seller2");
    memberRepository.save(seller2);

    MemberSeller pendingSeller =
        MemberSeller.builder()
            .member(seller2)
            .businessName("대기상점")
            .representativeName("정대기")
            .settlementBankName("국민은행")
            .settlementBankAccount("123-45-6789012")
            .businessLicenseUrl("https://example.com/license2.pdf")
            .requestedAt(LocalDateTime.now().minusDays(3))
            .build();
    memberSellerRepository.save(pendingSeller);

    // 프로필만 있는 회원 (배송지 없음)
    Member user4 = createMember("user4@example.com", "강신규", "010-6666-6666", MemberRole.MEMBER);
    createProfile(user4, "신규회원", null, null, null, null);
    addOAuthAccount(user4, OAuthProvider.KAKAO, "kakao_newuser");
    memberRepository.save(user4);

    log.info("System and Holder accounts created for payment/settlement processing");

    log.info(
        "Member base data initialization completed. Total members: {}", memberRepository.count());
  }

  private Member createMember(String email, String realName, String phoneNumber, MemberRole role) {
    return Member.builder()
        .email(email)
        .realName(realName)
        .phoneNumber(phoneNumber)
        .role(role)
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
