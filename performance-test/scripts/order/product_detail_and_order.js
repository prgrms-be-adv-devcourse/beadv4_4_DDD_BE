import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '3';
const PRODUCT_ID = Number(__ENV.PRODUCT_ID || '1');

// 정확히 10명이 1번씩 주문
const CONCURRENT_VUS = Number(__ENV.CONCURRENT_VUS || '100');

export const options = {
  scenarios: {
    concurrent_orders: {
      executor: 'per-vu-iterations',
      vus: CONCURRENT_VUS,   // 10명
      iterations: 1,         // 각 VU당 1회 실행
      maxDuration: '10s',    // 안전 타임아웃
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
    throw new Error(`Dev login failed: status=${loginRes.status}`);
  }

  const loginJson = JSON.parse(loginRes.body);
  return { accessToken: loginJson.accessToken };
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
