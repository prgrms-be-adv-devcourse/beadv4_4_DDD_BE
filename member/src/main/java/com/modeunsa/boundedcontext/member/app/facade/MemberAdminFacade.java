package com.modeunsa.boundedcontext.member.app.facade;

import com.modeunsa.boundedcontext.member.app.usecase.AdminApproveSellerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAdminFacade {
  private final AdminApproveSellerUseCase adminApproveSellerUseCase;

  /** 관리자 관련 */
  @Transactional
  public void approveSeller(Long sellerId) {
    adminApproveSellerUseCase.execute(sellerId);
  }
}
