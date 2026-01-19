package com.modeunsa.global.event;

/**
 * 이벤트에 traceId를 포함하여 로깅 추적을 가능하게 하는 인터페이스.
 *
 * <p>사용 예시:
 *
 * <pre>{@code
 * public record PaymentMemberCreatedEvent(Long memberId, String traceId)
 *     implements TraceableEvent {
 *
 *   public PaymentMemberCreatedEvent(Long memberId) {
 *     this(memberId, EventUtils.extractTraceId());
 *   }
 * }
 * }</pre>
 */
public interface TraceableEvent {
  String traceId();
}
