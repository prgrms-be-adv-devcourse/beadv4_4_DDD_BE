package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class OrderDataInit {

  private final OrderDataInit self;
  private final OrderFacade orderFacade;

  // ★ 리포지토리 직접 사용 (데이터 셋업용)
  private final OrderMemberRepository orderMemberRepository;
  private final OrderProductRepository orderProductRepository;

  public OrderDataInit(
      @Lazy OrderDataInit self,
      OrderFacade orderFacade,
      OrderMemberRepository orderMemberRepository,
      OrderProductRepository orderProductRepository) {
    this.self = self;
    this.orderFacade = orderFacade;
    this.orderMemberRepository = orderMemberRepository;
    this.orderProductRepository = orderProductRepository;
  }

  @Bean
  @org.springframework.core.annotation.Order(3)
  public ApplicationRunner orderDataInitApplicationRunner() {
    return args -> {
      self.makeBaseMembers(); // 1. 회원 먼저 생성
      self.makeBaseProducts(); // 2. 상품 생성 (회원 필요)
      self.makeBaseCartItems(); // 3. 장바구니 담기 테스트 (회원+상품 필요)
      self.makeBaseOrders(); // 4. 주문
    };
  }

  // 1. 회원 생성
  @Transactional
  public void makeBaseMembers() {
    if (orderFacade.countMember() > 0) {
      return;
    }

    // 비밀번호는 테스트용이라 {noop} 붙임 (Spring Security 설정에 따라 다름)
    saveMember("user1", "user1@example.com");
    saveMember("user2", "user2@example.com");
    saveMember("user3", "user3@example.com");

    log.info("Test Members Created: user1, user2, user3");
  }

  private void saveMember(String memberName, String memberPhone) {
    OrderMember member =
        OrderMember.builder().memberName(memberName).memberPhone(memberPhone).build();
    orderMemberRepository.save(member);
  }

  // 2. 상품 생성
  @Transactional
  public void makeBaseProducts() {
    if (orderFacade.countProduct() > 0) {
      return;
    }

    OrderMember user1 = orderFacade.findByMemberId(1L);
    saveProduct(user1.getId(), 10_000, "장갑");
    saveProduct(user1.getId(), 15_000, "모자");
    saveProduct(user1.getId(), 20_000, "목도리");

    OrderMember user2 = orderFacade.findByMemberId(2L);
    saveProduct(user2.getId(), 25_000, "니트");
    saveProduct(user2.getId(), 30_000, "셔츠");

    OrderMember user3 = orderFacade.findByMemberId(3L);
    saveProduct(user3.getId(), 35_000, "바지");

    log.info("Test Products Created");
  }

  private void saveProduct(Long sellerId, int price, String name) {
    OrderProduct product =
        OrderProduct.builder()
            .sellerId(sellerId)
            .name(name)
            .price(BigDecimal.valueOf(price))
            .salePrice(BigDecimal.valueOf(price * 1.2))
            .qty(100)
            .build();
    orderProductRepository.save(product);
  }

  // 3. 장바구니 테스트 (Facade 로직 검증)
  @Transactional
  public void makeBaseCartItems() {
    OrderMember user1 = orderFacade.findByMemberId(1L);
    OrderMember user2 = orderFacade.findByMemberId(2L);

    OrderProduct product1 = orderFacade.findByProductId(1L);
    OrderProduct product2 = orderFacade.findByProductId(2L);
    OrderProduct product3 = orderFacade.findByProductId(3L);
    OrderProduct product4 = orderFacade.findByProductId(4L);

    // Facade를 통해 비즈니스 로직 실행
    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(product1.getId(), 1));
    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(product2.getId(), 2));
    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(product3.getId(), 1));
    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(product4.getId(), 1));

    orderFacade.createCartItem(user2.getId(), new CreateCartItemRequestDto(product1.getId(), 1));

    log.info("Test CartItems Initialized via Facade");
  }

  // 4. 단건 주문 생성
  @Transactional
  public void makeBaseOrders() {
    if (orderFacade.countOrder() > 0) {
      return;
    }

    OrderMember buyer1 = orderFacade.findByMemberId(1L); // user1이 구매
    OrderProduct product1 = orderFacade.findByProductId(5L); // 셔츠 구매

    OrderMember buyer2 = orderFacade.findByMemberId(2L); // user1이 구매
    OrderProduct product2 = orderFacade.findByProductId(6L); // 바지 구매

    // 단건 주문 생성
    orderFacade.createOrder(
        buyer1.getId(),
        new CreateOrderRequestDto(
            product1.getId(), // productId
            2, // quantity (2개 구매)
            "홍길동", // recipientrName
            "010-1234-5678", // recipientPhone
            "12345", // zipcode
            "서울시 강남구 테헤란로 123", // addresss
            "107동" // addressDetail
            ));

    log.info("Test Single Order Created: user1 bought '셔츠' (qty: 2)");

    orderFacade.createOrder(
        buyer2.getId(),
        new CreateOrderRequestDto(
            product2.getId(), // productId
            7, // quantity (2개 구매)
            "세종대왕", // recipientName
            "010-1234-5678", // recipientPhone
            "12345", // zipcode
            "서울시 강남구 테헤란로 123", // address
            "203동" // addressDetail
            ));

    log.info("Test Single Order Created: user2 bought '셔츠' (qty: 7)");
  }
}
