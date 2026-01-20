package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.member.app.usecase.ApproveSellerUseCase;
import com.modeunsa.boundedcontext.member.app.usecase.RegisterSellerUseCase;
import com.modeunsa.shared.member.dto.SellerRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberFacade {
  private final ApproveSellerUseCase approveSellerUseCase;
  private final RegisterSellerUseCase registerSellerUseCase;

  @Transactional
  public void approveSeller(Long sellerId) {
    approveSellerUseCase.execute(sellerId);
  }

  @Transactional
  public void registerSeller(Long memberId, SellerRegisterRequest request, MultipartFile licenseImage) {
    registerSellerUseCase.execute(memberId, request, licenseImage);
  }
}
