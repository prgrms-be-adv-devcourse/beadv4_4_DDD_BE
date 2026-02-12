package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.MemberRole;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerRegisterUseCase {

  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;
  private final EventPublisher eventPublisher;

  public void execute(Long memberId, SellerRegisterRequest request, String finalLicenseUrl) {
    // 1. 회원 조회
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 2. 중복 신청 검증
    Optional<MemberSeller> existingSellerOp = memberSellerRepository.findByMemberId(memberId);
    MemberSeller seller = null;

    if (existingSellerOp.isPresent()) {
      seller = existingSellerOp.get();
      if (seller.getStatus() == SellerStatus.PENDING) {
        throw new GeneralException(ErrorStatus.SELLER_ALREADY_REQUESTED);
      } else if (seller.getStatus() != SellerStatus.REJECTED) {
        throw new GeneralException(ErrorStatus.SELLER_ALREADY_REGISTERED);
      }
    }

    // 3. 이미지 URL 검증
    if (finalLicenseUrl == null || finalLicenseUrl.isBlank()) {
      throw new GeneralException(ErrorStatus.IMAGE_FILE_REQUIRED);
    }

    // 4. 엔티티 반영
    if (seller != null) { // 재신청 (기존 엔티티 업데이트)
      seller.reapply(
          request.businessName(),
          request.representativeName(),
          request.settlementBankName(),
          request.settlementBankAccount(),
          finalLicenseUrl);
    } else { // 신규 신청 (새 엔티티 생성)
      MemberSeller.validateBankAccount(request.settlementBankAccount());

      seller =
          MemberSeller.builder()
              .member(member)
              .businessName(request.businessName())
              .representativeName(request.representativeName())
              .settlementBankName(request.settlementBankName())
              .settlementBankAccount(request.settlementBankAccount())
              .businessLicenseUrl(finalLicenseUrl)
              .status(SellerStatus.ACTIVE) // TODO: PENDING으로 변경
              .requestedAt(LocalDateTime.now())
              .build();

      memberSellerRepository.save(seller);
    }

    // ROLE SELLER 부여
    member.changeRole(MemberRole.SELLER);

    // 5. 이벤트 발행
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
}
