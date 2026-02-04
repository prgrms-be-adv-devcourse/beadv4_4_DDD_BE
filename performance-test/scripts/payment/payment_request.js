import http from 'k6/http';
import { check, sleep } from 'k6';

// 환경변수로 URL 받기, 없으면 기본값
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '4';

export let options = {
  vus: 10,          // 가상 사용자 수
  duration: '30s',  // 테스트 지속 시간
};

// 테스트 시작 전에 한 번만 실행: 로그인해서 accessToken 공유
export function setup() {
  const loginUrl = `${BASE_URL}/api/v1/auths/dev/login?memberId=${MEMBER_ID}`;

  const res = http.post(loginUrl);

  // 상태/바디 로그로 찍어서 실제 응답 확인
  console.log(`Dev login status=${res.status}`);
  console.log(`Dev login body=${res.body}`);

  // 1) 상태 코드 체크
  if (res.status !== 200) {
    throw new Error(`Dev login failed: status=${res.status}, body=${res.body}`);
  }

  // 2) body null 방어
  if (!res.body) {
    throw new Error('Dev login failed: response body is empty');
  }

  // 3) JSON 파싱
  let json;
  try {
    json = res.json();
  } catch (e) {
    throw new Error(`Dev login JSON parse error: ${e} body=${res.body}`);
  }

  const accessToken = json.accessToken;
  if (!accessToken) {
    throw new Error(`Dev login success but accessToken is missing. body=${JSON.stringify(json)}`);
  }

  return { accessToken };
}

// 각 VU가 반복해서 실행할 시나리오
export default function (data) {
  const accessToken = data.accessToken;

  // 간단한 주문/결제 요청 페이로드 생성
  const orderId = Math.floor(Math.random() * 1e9); // 임의 orderId
  const orderNo = `ORDER-${__VU}-${Date.now()}`;

  // LocalDateTime용 문자열 (yyyy-MM-dd'T'HH:mm:ss)
  const now = new Date();
  const deadline = new Date(now.getTime() + 30 * 60 * 1000) // +30분
  .toISOString()
  .substring(0, 19); // "2026-02-04T05:21:30" 형식

  const payload = JSON.stringify({
    orderId: orderId,
    orderNo: orderNo,
    totalAmount: 5000,                 // BigDecimal 필드
    paymentDeadlineAt: deadline,       // LocalDateTime
    providerType: 'TOSS_PAYMENTS',     // ProviderType enum
    paymentPurpose: 'PRODUCT_PURCHASE' // PaymentPurpose enum
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
  };

  const res = http.post(`${BASE_URL}/api/v1/payments`, payload, params);

  check(res, {
    'payment status is 200': (r) => r.status === 200,
  });

  sleep(1); // 1초 대기
}
