package com.modeunsa.boundedcontext.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
class PaymentMemberTest {

  @ParameterizedTest
  @MethodSource("memberTestDataProvider")
  void registerPaymentMember(Long id, String email, String name, MemberStatus status) {

    PaymentMember member = PaymentMember.create(id, email, name, status);

    assertThat(member.getId()).isEqualTo(id);
    assertThat(member.getEmail()).isEqualTo(email);
    assertThat(member.getName()).isEqualTo(name);
    assertThat(member.getCustomerKey()).isNotNull();
    assertThat(member.getStatus()).isEqualTo(status);
  }

  static Stream<Arguments> memberTestDataProvider() {
    return Stream.of(
        Arguments.of(1L, "user1@example.com", "user1", MemberStatus.ACTIVE),
        Arguments.of(2L, "user2@example.com", "user2", MemberStatus.ACTIVE));
  }
}
