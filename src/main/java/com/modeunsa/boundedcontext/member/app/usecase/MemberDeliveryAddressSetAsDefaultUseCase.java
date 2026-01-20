package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.out.repository.MemberDeliveryAddressRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressSetAsDefaultUseCase {

  private final MemberSupport memberSupport;
  private final MemberDeliveryAddressRepository addressRepository;

  @Transactional
  public void execute(Long memberId, Long addressId) {
    Member member = memberSupport.getMember(memberId);

    // 1. 설정하려는 배송지 조회
    MemberDeliveryAddress newDefaultAddress = addressRepository.findById(addressId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.ADDRESS_NOT_FOUND));

    // 2. 권한 검증 (내 배송지가 맞는지)
    if (!newDefaultAddress.getMember().getId().equals(member.getId())) {
      throw new GeneralException(ErrorStatus.ADDRESS_ACCESS_DENIED);
    }

    // 3. [추가] 이미 기본 배송지로 설정되어 있는지 검증
    MemberDeliveryAddress currentDefault = member.getDefaultDeliveryAddress(); // Member 엔티티에 해당 메서드가 있다고 가정

    if (currentDefault != null && Objects.equals(currentDefault.getId(), addressId)) {
      // 이미 해당 주소가 기본 배송지라면 예외 발생
      throw new GeneralException(ErrorStatus.ADDRESS_ALREADY_DEFAULT);
    }

    // 4. 기본 배송지 변경 (Member 엔티티 내부 로직에서 기존 기본값 해제 후 새 값 설정)
    member.setNewDefaultAddress(newDefaultAddress);
  }
}