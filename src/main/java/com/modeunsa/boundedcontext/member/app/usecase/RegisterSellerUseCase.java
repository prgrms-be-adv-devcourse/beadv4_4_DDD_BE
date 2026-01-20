package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.domain.types.SellerStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.s3.S3Uploader;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.SellerRegisterRequest;
import com.modeunsa.shared.member.event.SellerRegisteredEvent;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class RegisterSellerUseCase {

  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final S3Uploader s3Uploader;

  public void execute(Long memberId, SellerRegisterRequest request, MultipartFile licenseImage) {
    // 1. 회원 조회
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 2. 판매자 신청 내역 조회
    Optional<MemberSeller> existingSellerOp = memberSellerRepository.findByMemberId(memberId);
    MemberSeller seller = null;

    // 재신청 또는 중복 신청 검증 로직
    if (existingSellerOp.isPresent()) {
      seller = existingSellerOp.get();
      if (seller.getStatus() == SellerStatus.PENDING) {
        throw new GeneralException(ErrorStatus.SELLER_ALREADY_REQUESTED);
      } else if (seller.getStatus() != SellerStatus.REJECTED) {
        throw new GeneralException(ErrorStatus.SELLER_ALREADY_REGISTERED);
      }
    }

    // 3. 검증 통과 후 이미지 업로드 수행
    String uploadedLicenseUrl;
    if (licenseImage != null && !licenseImage.isEmpty()) {
      uploadedLicenseUrl = s3Uploader.upload(licenseImage, "sellers");
    } else {
      throw new GeneralException(ErrorStatus.IMAGE_FILE_REQUIRED);
    }

    // 4. 엔티티 반영
    if (seller != null) { // 재신청
      seller.reapply(
          request.businessName(),
          request.representativeName(),
          request.settlementBankName(),
          request.settlementBankAccount(),
          uploadedLicenseUrl);
    } else { // 신규 신청
      seller =
          MemberSeller.builder()
              .member(member)
              .businessName(request.businessName())
              .representativeName(request.representativeName())
              .settlementBankName(request.settlementBankName())
              .settlementBankAccount(request.settlementBankAccount())
              .businessLicenseUrl(uploadedLicenseUrl)
              .status(SellerStatus.PENDING)
              .requestedAt(LocalDateTime.now())
              .build();

      memberSellerRepository.save(seller);
    }

    // 5. 이벤트 발행
    eventPublisher.publishEvent(new SellerRegisteredEvent(seller));
  }
}
