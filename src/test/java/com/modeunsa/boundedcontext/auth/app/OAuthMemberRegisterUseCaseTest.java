package com.modeunsa.boundedcontext.auth.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modeunsa.boundedcontext.auth.app.usecase.OAuthMemberRegisterUseCase;
import com.modeunsa.boundedcontext.auth.domain.entity.OAuthAccount;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import com.modeunsa.shared.auth.dto.OAuthUserInfo;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthMemberRegisterUseCaseTest {

  @InjectMocks private OAuthMemberRegisterUseCase oauthMemberRegisterUseCase;

  @Mock private MemberRepository memberRepository;
  @Mock private SpringDomainEventPublisher eventPublisher;

  @Captor private ArgumentCaptor<Member> memberCaptor;
  @Captor private ArgumentCaptor<MemberSignupEvent> eventCaptor;

  private final OAuthProvider provider = OAuthProvider.KAKAO;
  private final String providerId = "kakao_12345";
  private final String email = "test@example.com";
  private final String name = "테스트유저";
  private final String phoneNumber = "010-1234-5678";

  @Test
  @DisplayName("신규 회원 및 소셜 계정 생성 성공")
  void registerNewMember() {
    // given
    OAuthUserInfo userInfo = createUserInfo();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    OAuthAccount result = oauthMemberRegisterUseCase.execute(userInfo);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getOauthProvider()).isEqualTo(provider);
    assertThat(result.getProviderId()).isEqualTo(providerId);

    verify(memberRepository, times(1)).save(memberCaptor.capture());
    Member savedMember = memberCaptor.getValue();
    assertThat(savedMember.getEmail()).isEqualTo(email);
    assertThat(savedMember.getRealName()).isEqualTo(name);
    assertThat(savedMember.getPhoneNumber()).isEqualTo(phoneNumber);
  }

  @Test
  @DisplayName("회원 생성 시 양방향 연관관계 설정 확인")
  void verifyBidirectionalRelationship() {
    // given
    OAuthUserInfo userInfo = createUserInfo();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    OAuthAccount result = oauthMemberRegisterUseCase.execute(userInfo);

    // then
    verify(memberRepository, times(1)).save(memberCaptor.capture());
    Member savedMember = memberCaptor.getValue();

    // 양방향 연관관계 확인: Member의 소셜 계정 목록에 포함되어 있어야 함
    assertThat(savedMember.getOauthAccount()).contains(result);
    // AuthSocialAccount의 Member 참조 확인
    assertThat(result.getMember()).isEqualTo(savedMember);
  }

  @Test
  @DisplayName("회원가입 이벤트 발행 확인")
  void publishMemberSignupEvent() {
    // given
    OAuthUserInfo userInfo = createUserInfo();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    oauthMemberRegisterUseCase.execute(userInfo);

    // then
    verify(eventPublisher, times(1)).publish(eventCaptor.capture());
    MemberSignupEvent event = eventCaptor.getValue();

    assertThat(event.memberId()).isEqualTo(1L);
    assertThat(event.realName()).isEqualTo(name);
    assertThat(event.email()).isEqualTo(email);
    assertThat(event.phoneNumber()).isEqualTo(phoneNumber);
    assertThat(event.role()).isEqualTo(MemberRole.MEMBER);
    assertThat(event.status()).isEqualTo(MemberStatus.ACTIVE);
  }

  @Test
  @DisplayName("전화번호 없이 회원 가입 성공")
  void registerWithoutPhoneNumber() {
    // given
    OAuthUserInfo userInfo =
        OAuthUserInfo.builder()
            .provider(provider)
            .providerId(providerId)
            .email(email)
            .name(name)
            .phoneNumber(null)
            .build();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    OAuthAccount result = oauthMemberRegisterUseCase.execute(userInfo);

    // then
    assertThat(result).isNotNull();
    verify(memberRepository, times(1)).save(memberCaptor.capture());
    Member savedMember = memberCaptor.getValue();
    assertThat(savedMember.getPhoneNumber()).isNull();
  }

  @Test
  @DisplayName("기본 회원 역할(MEMBER)로 생성됨")
  void registerWithDefaultRole() {
    // given
    OAuthUserInfo userInfo = createUserInfo();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    oauthMemberRegisterUseCase.execute(userInfo);

    // then
    verify(memberRepository, times(1)).save(memberCaptor.capture());
    Member savedMember = memberCaptor.getValue();
    assertThat(savedMember.getRole()).isEqualTo(MemberRole.MEMBER);
  }

  @Test
  @DisplayName("기본 회원 상태(ACTIVE)로 생성됨")
  void registerWithDefaultStatus() {
    // given
    OAuthUserInfo userInfo = createUserInfo();

    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              setEntityId(member, 1L);
              return member;
            });

    // when
    oauthMemberRegisterUseCase.execute(userInfo);

    // then
    verify(memberRepository, times(1)).save(memberCaptor.capture());
    Member savedMember = memberCaptor.getValue();
    assertThat(savedMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
  }

  // --- Helper Methods ---

  private OAuthUserInfo createUserInfo() {
    return OAuthUserInfo.builder()
        .provider(provider)
        .providerId(providerId)
        .email(email)
        .name(name)
        .phoneNumber(phoneNumber)
        .build();
  }

  private void setEntityId(Object entity, Long id) {
    try {
      Field idField = findIdField(entity.getClass());
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set entity id", e);
    }
  }

  private Field findIdField(Class<?> clazz) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField("id");
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException("id field not found");
  }
}
