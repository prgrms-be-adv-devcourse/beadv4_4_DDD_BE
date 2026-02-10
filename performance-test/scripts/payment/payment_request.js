import http from 'k6/http';
import { check, sleep } from 'k6';

// 환경변수로 URL 받기, 없으면 기본값
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '2';

export let options = {
  vus: 10,          // 가상 사용자 수
  duration: '30s',  // 테스트 지속 시간
};

// 테스트 시작 전에 한 번만 실행: 로그인해서 accessToken 공유
export function setup() {
  const loginUrl = `${BASE_URL}/api/v1/auths/dev/login?memberId=${MEMBER_ID}`;
  const loginParams = {
    headers: { Accept: 'application/json' },
  };

  const res = http.post(loginUrl, null, loginParams);

  if (res.status !== 200) {
    throw new Error(`Dev login failed: status=${res.status}, body=${res.body}`);
  }
  if (!res.body) {
    throw new Error('Dev login failed: response body is empty');
  }

  let json;
  try {
    json = JSON.parse(res.body);
  } catch (e) {
    throw new Error(`Dev login JSON parse error: ${e.message}, body=${res.body}`);
  }

  const accessToken = json.accessToken;
  if (!accessToken) {
    throw new Error(`Dev login: accessToken missing in response. body=${res.body}`);
  }

  return { accessToken };
}

// 각 VU가 반복해서 실행할 시나리오 (setup()에서 받은 data 사용)
export default function (data) {
  if (!data || !data.accessToken) {
    console.warn('No accessToken from setup, skipping iteration');
    sleep(1);
    return;
  }

  const accessToken = data.accessToken;

  const orderId = Math.floor(Math.random() * 1e9);
  const orderNo = `ORDER-${__VU}-${Date.now()}`;

  // PaymentRequest.paymentDeadlineAt: 미래 시점 (서버 LocalDateTime 파싱 시 타임존 이슈 방지를 위해 7일 후 사용)
  const deadline = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
    .toISOString()
    .slice(0, 19);

  const payload = JSON.stringify({
    orderId,
    orderNo,
    totalAmount: 5000,
    paymentDeadlineAt: deadline,
    providerType: 'TOSS_PAYMENTS',
    paymentPurpose: 'PRODUCT_PURCHASE',
  });

  const res = http.post(`${BASE_URL}/api/v1/payments`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
  });

  const ok = check(res, {
    'payment status is 200': (r) => r.status === 200,
  });
  if (!ok) {
    console.warn(`payment failed: status=${res.status}, body=${res.body}`);
  }

  sleep(1);
}
