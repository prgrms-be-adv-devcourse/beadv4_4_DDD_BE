package com.modeunsa.boundedcontext.settlement.in;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.domain.entity.Settlement;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementCandidateItem;
import com.modeunsa.boundedcontext.settlement.domain.entity.SettlementMember;
import com.modeunsa.boundedcontext.settlement.out.SettlementCandidateItemRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementMemberRepository;
import com.modeunsa.boundedcontext.settlement.out.SettlementRepository;
import com.modeunsa.global.config.SettlementConfig;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.settlement.dto.SettlementResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Settlement", description = "정산 도메인 API")
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class ApiV1SettlementController {
  private final SettlementFacade settlementFacade;
  private final SettlementMemberRepository settlementMemberRepository;
  private final SettlementCandidateItemRepository settlementCandidateItemRepository;
  private final SettlementRepository settlementRepository;
  private final SettlementConfig settlementConfig;

  @Operation(summary = "월별 정산서 조회", description = "원하는 월의 정산서를 조회합니다.")
  @GetMapping("/{year}/{month}")
  public ResponseEntity<ApiResponse> getSettlement(
      // @AuthenticationPrincipal Long memberId,
      @PathVariable int year, @PathVariable @Min(1) @Max(12) int month) {
    SettlementResponseDto settlementResponseDto = settlementFacade.getSettlement(7L, year, month);

    return ApiResponse.onSuccess(SuccessStatus.CREATED, settlementResponseDto);
  }

  @Operation(summary = "1단계: 기본 멤버 데이터 조회", description = "초기화된 멤버 데이터를 조회합니다.")
  @GetMapping("/flow/step1")
  public ResponseEntity<ApiResponse> getStep1Members() {
    List<SettlementMember> members = settlementMemberRepository.findAll();
    List<MemberDto> memberDtos =
        members.stream()
            .map(
                m ->
                    new MemberDto(
                        m.getId(),
                        m.getRole(),
                        m.getCreatedAt() != null ? m.getCreatedAt() : LocalDateTime.now()))
            .collect(Collectors.toList());

    return ApiResponse.onSuccess(SuccessStatus.OK, memberDtos);
  }

  @Operation(summary = "2단계: 정산 후보 항목 조회", description = "생성된 정산 후보 항목을 조회합니다.")
  @GetMapping("/flow/step2")
  public ResponseEntity<ApiResponse> getStep2CandidateItems() {
    List<SettlementCandidateItem> items = settlementCandidateItemRepository.findAll();
    List<CandidateItemDto> itemDtos =
        items.stream()
            .map(
                item ->
                    new CandidateItemDto(
                        item.getId(),
                        item.getOrderItemId(),
                        item.getBuyerMemberId(),
                        item.getSellerMemberId(),
                        item.getAmount(),
                        item.getPurchaseConfirmedAt(),
                        item.getCollectedAt()))
            .collect(Collectors.toList());

    BigDecimal totalAmount =
        items.stream()
            .map(SettlementCandidateItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Step2Response response = new Step2Response(itemDtos, totalAmount);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(summary = "3단계: 일별 배치 후 정산서 조회", description = "일별 배치 실행 후 생성된 정산서를 조회합니다.")
  @GetMapping("/flow/step3")
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public ResponseEntity<ApiResponse> getStep3Settlements() {
    // 7번 판매자를 기준으로 정산서 조회
    Long sellerMemberId = 7L;
    Long systemMemberId = settlementConfig.getSystemMemberId();

    // 판매자 정산서와 시스템 정산서(수수료) 모두 조회
    List<Settlement> sellerSettlements =
        settlementRepository.findAllBySellerMemberId(sellerMemberId);
    List<Settlement> systemSettlements =
        settlementRepository.findAllBySellerMemberId(systemMemberId);

    // 두 리스트 합치기
    List<Settlement> settlements = new java.util.ArrayList<>();
    settlements.addAll(sellerSettlements);
    settlements.addAll(systemSettlements);

    // items 컬렉션을 초기화하기 위해 각 Settlement의 items에 접근
    settlements.forEach(
        s -> {
          if (s.getItems() != null) {
            s.getItems().size(); // Lazy 초기화
          }
        });

    // 현재 년도와 월 가져오기
    LocalDate now = LocalDate.now();
    int currentYear = now.getYear();
    int currentMonth = now.getMonthValue();

    // 모든 정산 아이템 수집
    List<SettlementItemDto> allItems = new java.util.ArrayList<>();

    List<SettlementDto> settlementDtos =
        settlements.stream()
            .map(
                s -> {
                  // 각 정산서의 아이템들을 DTO로 변환
                  List<SettlementItemDto> itemDtos =
                      s.getItems().stream()
                          .map(
                              item ->
                                  new SettlementItemDto(
                                      item.getId(),
                                      s.getId(),
                                      item.getOrderItemId(),
                                      item.getBuyerMemberId(),
                                      item.getSellerMemberId(),
                                      item.getAmount(),
                                      item.getEventType().name(),
                                      item.getPurchaseConfirmedAt()))
                          .collect(Collectors.toList());

                  // 전체 아이템 리스트에 추가
                  allItems.addAll(itemDtos);

                  return new SettlementDto(
                      s.getId(),
                      s.getSellerMemberId(),
                      s.getType().name(),
                      s.getAmount(),
                      currentYear, // 현재 년도로 표시
                      currentMonth, // 현재 월로 표시
                      s.getPayoutAt(),
                      s.getItems() != null ? s.getItems().size() : 0,
                      itemDtos);
                })
            .collect(Collectors.toList());

    BigDecimal totalSellerAmount =
        sellerSettlements.stream()
            .map(Settlement::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalFeeAmount =
        systemSettlements.stream()
            .map(Settlement::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Step3Response response =
        new Step3Response(settlementDtos, totalSellerAmount, totalFeeAmount, allItems);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(summary = "4단계: 기간 조정 후 정산서 조회", description = "기간 조정 후 정산서를 조회합니다.")
  @GetMapping("/flow/step4")
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public ResponseEntity<ApiResponse> getStep4Settlements() {
    List<Settlement> settlements = settlementRepository.findAll();
    // items 컬렉션을 초기화하기 위해 각 Settlement의 items에 접근
    settlements.forEach(
        s -> {
          if (s.getItems() != null) {
            s.getItems().size(); // Lazy 초기화
          }
        });

    List<SettlementDto> settlementDtos =
        settlements.stream()
            .map(
                s -> {
                  List<SettlementItemDto> itemDtos =
                      s.getItems() != null
                          ? s.getItems().stream()
                              .map(
                                  item ->
                                      new SettlementItemDto(
                                          item.getId(),
                                          s.getId(),
                                          item.getOrderItemId(),
                                          item.getBuyerMemberId(),
                                          item.getSellerMemberId(),
                                          item.getAmount(),
                                          item.getEventType().name(),
                                          item.getPurchaseConfirmedAt()))
                              .collect(Collectors.toList())
                          : new java.util.ArrayList<>();

                  return new SettlementDto(
                      s.getId(),
                      s.getSellerMemberId(),
                      s.getType().name(),
                      s.getAmount(),
                      s.getSettlementYear(),
                      s.getSettlementMonth(),
                      s.getPayoutAt(),
                      s.getItems() != null ? s.getItems().size() : 0,
                      itemDtos);
                })
            .collect(Collectors.toList());

    return ApiResponse.onSuccess(SuccessStatus.OK, settlementDtos);
  }

  @Operation(summary = "5단계: 월별 배치 후 정산서 조회", description = "월별 배치 실행 후 지급 완료된 정산서를 조회합니다.")
  @GetMapping("/flow/step5")
  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public ResponseEntity<ApiResponse> getStep5Settlements() {
    List<Settlement> settlements = settlementRepository.findAll();
    // items 컬렉션을 초기화하기 위해 각 Settlement의 items에 접근
    settlements.forEach(
        s -> {
          if (s.getItems() != null) {
            s.getItems().size(); // Lazy 초기화
          }
        });

    List<SettlementDto> settlementDtos =
        settlements.stream()
            .map(
                s -> {
                  List<SettlementItemDto> itemDtos =
                      s.getItems() != null
                          ? s.getItems().stream()
                              .map(
                                  item ->
                                      new SettlementItemDto(
                                          item.getId(),
                                          s.getId(),
                                          item.getOrderItemId(),
                                          item.getBuyerMemberId(),
                                          item.getSellerMemberId(),
                                          item.getAmount(),
                                          item.getEventType().name(),
                                          item.getPurchaseConfirmedAt()))
                              .collect(Collectors.toList())
                          : new java.util.ArrayList<>();

                  return new SettlementDto(
                      s.getId(),
                      s.getSellerMemberId(),
                      s.getType().name(),
                      s.getAmount(),
                      s.getSettlementYear(),
                      s.getSettlementMonth(),
                      s.getPayoutAt(),
                      s.getItems() != null ? s.getItems().size() : 0,
                      itemDtos);
                })
            .collect(Collectors.toList());

    long paidCount = settlements.stream().filter(s -> s.getPayoutAt() != null).count();

    Step5Response response = new Step5Response(settlementDtos, paidCount, settlements.size());
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Getter
  @AllArgsConstructor
  public static class MemberDto {
    private Long id;
    private String role;
    private LocalDateTime createdAt;
  }

  @Getter
  @AllArgsConstructor
  public static class CandidateItemDto {
    private Long id;
    private Long orderItemId;
    private Long buyerMemberId;
    private Long sellerMemberId;
    private BigDecimal amount;
    private LocalDateTime purchaseConfirmedAt;
    private LocalDateTime collectedAt;
  }

  @Getter
  @AllArgsConstructor
  public static class Step2Response {
    private List<CandidateItemDto> items;
    private BigDecimal totalAmount;
  }

  @Getter
  @AllArgsConstructor
  public static class SettlementItemDto {
    private Long id;
    private Long settlementId;
    private Long orderItemId;
    private Long buyerMemberId;
    private Long sellerMemberId;
    private BigDecimal amount;
    private String eventType;
    private LocalDateTime purchaseConfirmedAt;
  }

  @Getter
  @AllArgsConstructor
  public static class SettlementDto {
    private Long id;
    private Long sellerMemberId;
    private String type;
    private BigDecimal amount;
    private int settlementYear;
    private int settlementMonth;
    private LocalDateTime payoutAt;
    private int itemCount;
    private List<SettlementItemDto> items;
  }

  @Getter
  @AllArgsConstructor
  public static class Step3Response {
    private List<SettlementDto> settlements;
    private BigDecimal totalSellerAmount;
    private BigDecimal totalFeeAmount;
    private List<SettlementItemDto> allItems;
  }

  @Getter
  @AllArgsConstructor
  public static class Step5Response {
    private List<SettlementDto> settlements;
    private long paidCount;
    private long totalCount;
  }
}
