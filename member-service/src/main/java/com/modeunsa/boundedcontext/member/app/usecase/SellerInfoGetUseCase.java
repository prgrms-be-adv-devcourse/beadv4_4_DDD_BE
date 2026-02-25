package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.dto.response.SellerInfoResponse;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerInfoGetUseCase {

  private final MemberSellerRepository memberSellerRepository;

  public SellerInfoResponse execute(Long memberId) {
    MemberSeller seller =
        memberSellerRepository
            .findByMemberId(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SELLER_NOT_FOUND));

    return SellerInfoResponse.from(seller);
  }
}
