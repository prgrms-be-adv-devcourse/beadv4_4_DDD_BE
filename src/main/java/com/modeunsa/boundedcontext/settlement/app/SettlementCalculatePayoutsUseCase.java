package com.modeunsa.boundedcontext.settlement.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementCalculatePayoutsUseCase {

  public void calculatePayouts() {
    log.info("[UseCase] calculatePayouts 실행");
    // TODO: 추후 추가 예정
  }
}
