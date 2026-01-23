package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminApproveSellerUseCase {

  private final MemberSellerRepository memberSellerRepository;

  public void execute(Long sellerId) {
    MemberSeller seller =
        memberSellerRepository
            .findById(sellerId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.SELLER_NOT_FOUND));
    seller.approve();
  }
}
