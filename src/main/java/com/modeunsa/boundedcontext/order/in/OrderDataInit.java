package com.modeunsa.boundedcontext.order.in;

import com.modeunsa.boundedcontext.order.app.OrderFacade;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.domain.OrderProduct;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.boundedcontext.order.out.OrderProductRepository;
import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
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
  @org.springframework.core.annotation.Order(1)
  public ApplicationRunner orderDataInitApplicationRunner() {
    return args -> {
      self.makeBaseMembers(); // 1. 회원 먼저 생성 (가장 중요!)
      self.makeBaseProducts(); // 2. 상품 생성 (회원 필요)
      self.makeBaseCartItems(); // 3. 장바구니 담기 테스트 (회원+상품 필요)
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
    saveProduct(user1.getId(), 10_000);
    saveProduct(user1.getId(), 15_000);
    saveProduct(user1.getId(), 20_000);

    OrderMember user2 = orderFacade.findByMemberId(2L);
    saveProduct(user2.getId(), 25_000);
    saveProduct(user2.getId(), 30_000);

    OrderMember user3 = orderFacade.findByMemberId(3L);
    saveProduct(user3.getId(), 35_000);

    log.info("Test Products Created");
  }

  private void saveProduct(Long sellerId, int price) {
    OrderProduct product = OrderProduct.builder().sellerId(sellerId).price(price).qty(100).build();
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
}
