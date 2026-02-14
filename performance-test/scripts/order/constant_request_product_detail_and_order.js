import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '2';
const PRODUCT_ID = Number(__ENV.PRODUCT_ID || '1');

// 100명이 1초 간격으로 1명씩 시작, 각 1번만 주문 (총 100건)
const TOTAL_USERS = Number(__ENV.TOTAL_USERS || '100');
const INTERVAL_SEC = __ENV.INTERVAL_SEC || '1s';

export const options = {
  scenarios: {
    one_order_per_user: {
      executor: 'constant-arrival-rate',
      rate: 1,
      timeUnit: INTERVAL_SEC,
      duration: `${TOTAL_USERS}s`,
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
  },
  thresholds: {
    http_reqs: ['rate>1'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.90'],
  },
};

export function setup() {
  const loginUrl = `${BASE_URL}/api/v1/auths/dev/login?memberId=${MEMBER_ID}`;
  const loginRes = http.post(loginUrl, null, {
    headers: { Accept: 'application/json' },
  });

  if (loginRes.status !== 200) {
    throw new Error(`Dev login failed: status=${loginRes.status}, body=${loginRes.body}`);
  }
  if (!loginRes.body) {
    throw new Error('Dev login failed: response body is empty');
  }

  let loginJson;
  try {
    loginJson = JSON.parse(loginRes.body);
  } catch (e) {
    throw new Error(`Dev login JSON parse error: ${e.message}, body=${loginRes.body}`);
  }

  const accessToken = loginJson.accessToken;
  if (!accessToken) {
    throw new Error(`Dev login: accessToken missing. body=${loginRes.body}`);
  }

  return { accessToken };
}

export default function (data) {
  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.accessToken}`,
  };

  // 상품 조회
  const detailRes = http.get(`${BASE_URL}/api/v1/products/${PRODUCT_ID}`, { headers });

  check(detailRes, {
    'product detail status is 200': (r) => r.status === 200,
  });

  // 주문 생성
  const orderRes = http.post(
      `${BASE_URL}/api/v1/orders`,
      JSON.stringify({ productId: PRODUCT_ID, quantity: 1 }),
      { headers }
  );

  check(orderRes, {
    'order create status is 200': (r) => r.status === 200,
  });
}
