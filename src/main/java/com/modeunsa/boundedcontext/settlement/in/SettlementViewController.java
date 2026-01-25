package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/settlement")
@RequiredArgsConstructor
public class SettlementViewController {

  private final SettlementFacade settlementFacade;

  @GetMapping
  public String settlementPage(
      @RequestParam(required = false, defaultValue = "0") int step, Model model) {
    model.addAttribute("currentStep", Math.max(0, Math.min(5, step)));

    // 기본값으로 이전 달의 년도와 월 설정
    LocalDate lastMonth = LocalDate.now().minusMonths(1);
    model.addAttribute("selectedYear", lastMonth.getYear());
    model.addAttribute("selectedMonth", lastMonth.getMonthValue());

    // hasSettlement 초기화 (null 방지)
    model.addAttribute("hasSettlement", false);

    return "settlement/index";
  }

  @GetMapping("/query")
  public String querySettlement(
      @RequestParam int year,
      @RequestParam int month,
      @RequestParam(required = false) Long memberId,
      Model model) {

    try {
      // memberId가 없으면 기본값 사용 (실제로는 인증된 사용자 ID를 사용해야 함)
      Long sellerMemberId = memberId != null ? memberId : 7L; // 테스트용 SELLER_MEMBER_ID

      SettlementResponseDto settlement =
          settlementFacade.getSettlement(sellerMemberId, year, month);
      model.addAttribute("settlement", settlement);
      model.addAttribute("hasSettlement", true);
    } catch (Exception e) {
      model.addAttribute("hasSettlement", false);
      model.addAttribute("errorMessage", e.getMessage());
    }

    model.addAttribute("selectedYear", year);
    model.addAttribute("selectedMonth", month);
    model.addAttribute("currentStep", 5);

    return "settlement/index";
  }
}
