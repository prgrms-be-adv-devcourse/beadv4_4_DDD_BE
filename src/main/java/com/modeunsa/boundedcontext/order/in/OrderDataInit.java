// package com.modeunsa.boundedcontext.order.in;
//
// import com.modeunsa.boundedcontext.order.app.OrderFacade;
// import com.modeunsa.boundedcontext.order.app.OrderSupport;
// import com.modeunsa.boundedcontext.order.domain.Order;
// import com.modeunsa.boundedcontext.order.domain.OrderMember;
// import com.modeunsa.boundedcontext.order.domain.OrderProduct;
// import com.modeunsa.shared.order.dto.CreateCartItemRequestDto;
// import com.modeunsa.shared.order.dto.CreateOrderRequestDto;
// import com.modeunsa.shared.payment.dto.PaymentDto;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Lazy;
// import org.springframework.context.annotation.Profile;
// import org.springframework.transaction.annotation.Transactional;
//
// @Configuration
// @Profile({"local", "dev"})
// @Slf4j
// public class OrderDataInit {
//
//  private final OrderDataInit self;
//  private final OrderFacade orderFacade;
//  private final OrderSupport orderSupport;
//
//  public OrderDataInit(
//      @Lazy OrderDataInit self, OrderFacade orderFacade, OrderSupport orderSupport) {
//    this.self = self;
//    this.orderFacade = orderFacade;
//    this.orderSupport = orderSupport;
//  }
//
//  @Bean
//  @org.springframework.core.annotation.Order(3)
//  public ApplicationRunner orderDataInitApplicationRunner() {
//    return args -> {
//      self.makeBaseCartItems(); // 장바구니 담기 테스트
//      self.makeBaseOrders(); // 단건 주문
//    };
//  }
//
//  // 장바구니 담기
//  @Transactional
//  public void makeBaseCartItems() {
//
//    OrderMember user1 = orderFacade.findByMemberId(4L);
//    OrderMember user2 = orderFacade.findByMemberId(5L);
//
//    // Facade를 통해 비즈니스 로직 실행
//    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(1L, 1));
//    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(2L, 2));
//    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(3L, 1));
//    orderFacade.createCartItem(user1.getId(), new CreateCartItemRequestDto(4L, 1));
//
//    orderFacade.createCartItem(user2.getId(), new CreateCartItemRequestDto(1L, 1));
//
//    log.info("Test CartItems Initialized via Facade");
//  }
//
//  // 4. 단건 주문 생성
//  @Transactional
//  public void makeBaseOrders() {
//    if (orderFacade.countOrder() > 0) {
//      return;
//    }
//
//    OrderMember buyer1 = orderFacade.findByMemberId(4L); // user1이 구매
//    OrderProduct product1 = orderFacade.findByProductId(5L); // 셔츠 구매
//
//    OrderMember buyer2 = orderFacade.findByMemberId(5L); // user1이 구매
//    OrderProduct product2 = orderFacade.findByProductId(6L); // 바지 구매
//
//    // 단건 주문 생성
//    orderFacade.createOrder(
//        buyer1.getId(),
//        new CreateOrderRequestDto(
//            product1.getId(), // productId
//            2, // quantity (2개 구매)
//            "홍길동", // recipientrName
//            "010-1234-5678", // recipientPhone
//            "12345", // zipcode
//            "서울시 강남구 테헤란로 123", // addresss
//            "107동" // addressDetail
//            ));
//
//    log.info("Test Single Order Created: user1 bought '셔츠' (qty: 2)");
//
//    orderFacade.createOrder(
//        buyer2.getId(),
//        new CreateOrderRequestDto(
//            product2.getId(), // productId
//            3, // quantity (3개 구매)
//            "세종대왕", // recipientName
//            "010-1234-5678", // recipientPhone
//            "12345", // zipcode
//            "서울시 강남구 테헤란로 123", // address
//            "203동" // addressDetail
//            ));
//
//    log.info("Test Single Order Created: user2 bought '맨투맨' (qty: 7)");
//
//    // 결제 성공 처리
//    Order order1 = orderSupport.findTopByOrderMemberIdByOrderByIdDesc(buyer1.getId());
//
//    // PaymentDto 생성 (성공용)
//    PaymentDto paymentSuccess =
//        new PaymentDto(
//            order1.getId(),
//            order1.getOrderNo(), // 주문번호 (UUID)
//            buyer1.getId(),
//            order1.getTotalAmount() // 총 금액
//            );
//    orderFacade.approveOrder(paymentSuccess);
//    log.info("주문[{}] 결제 완료 처리됨", order1.getId());
//
//    // 결제 실패 처리
//    Order order2 = orderSupport.findTopByOrderMemberIdByOrderByIdDesc(buyer2.getId());
//
//    // PaymentDto 생성 (실패용)
//    PaymentDto paymentFail =
//        new PaymentDto(
//            order2.getId(),
//            order2.getOrderNo(), // 주문번호 (UUID)
//            buyer2.getId(),
//            order2.getTotalAmount() // 총 금액
//            );
//
//    orderFacade.rejectOrder(paymentFail);
//    log.info("주문[{}] 생성 및 결제 실패 처리됨", order2.getId());
//  }
// }
