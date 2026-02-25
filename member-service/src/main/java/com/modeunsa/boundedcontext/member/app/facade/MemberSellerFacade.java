package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.auth.domain.dto.JwtTokenResponse;
import com.modeunsa.boundedcontext.member.app.usecase.SellerInfoGetUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.SellerRegisterUseCase;
import com.modeunsa.boundedcontext.member.domain.dto.request.SellerRegisterRequest;
import com.modeunsa.boundedcontext.member.domain.dto.response.SellerInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSellerFacade {
  private final SellerRegisterUseCase sellerRegisterUseCase;
  private final SellerInfoGetUseCase sellerInfoGetUseCase;

  /** 판매자 등록 */
  @Transactional
  public JwtTokenResponse registerSeller(Long memberId, SellerRegisterRequest request) {
    return sellerRegisterUseCase.execute(memberId, request, request.businessLicenseUrl());
  }

  /** 판매자 정보 조회 */
  @Transactional(readOnly = true)
  public SellerInfoResponse getSellerInfo(Long memberId) {
    return sellerInfoGetUseCase.execute(memberId);
  }
}
