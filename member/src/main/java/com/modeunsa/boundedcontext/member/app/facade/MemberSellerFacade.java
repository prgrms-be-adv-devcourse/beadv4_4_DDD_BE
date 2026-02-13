package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.app.usecase.SellerInfoGetUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.SellerRegisterUseCase;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.shared.member.dto.request.SellerRegisterRequest;
import com.modeunsa.shared.member.dto.response.SellerInfoResponse;
import com.modeunsa.shared.member.dto.response.SellerRegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSellerFacade {
  private final SellerRegisterUseCase sellerRegisterUseCase;
  private final JwtTokenProvider jwtTokenProvider;
  private final SellerInfoGetUseCase sellerInfoGetUseCase;
  private final MemberSupport memberSupport;

  /** 판매자 등록 */
  @Transactional
  public SellerRegisterResponse registerSeller(Long memberId, SellerRegisterRequest request) {
    sellerRegisterUseCase.execute(memberId, request, request.businessLicenseUrl());

    Member member = memberSupport.getMember(memberId);
    Long sellerId = memberSupport.getSellerIdByMemberId(memberId);

    String accessToken =
        jwtTokenProvider.createAccessToken(
            member.getId(), member.getRole(), sellerId, member.getStatus().name());
    String refreshToken =
        jwtTokenProvider.createRefreshToken(
            member.getId(), member.getRole(), sellerId, member.getStatus().name());

    return new SellerRegisterResponse(accessToken, refreshToken);
  }

  /** 판매자 정보 조회 */
  @Transactional(readOnly = true)
  public SellerInfoResponse getSellerInfo(Long memberId) {
    return sellerInfoGetUseCase.execute(memberId);
  }
}
