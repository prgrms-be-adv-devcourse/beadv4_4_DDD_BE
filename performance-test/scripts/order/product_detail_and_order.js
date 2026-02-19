import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '2';
const PRODUCT_ID = Number(__ENV.PRODUCT_ID || '1');

// 점차 동시 주문 수를 늘리는 부하 (ramping-vus)
const PEAK_VUS = Number(__ENV.PEAK_VUS || '200');

// 현재 VU 수 추적 (stage 변경 감지용 - 각 VU마다 독립적)
let lastVUCount = 0;

export const options = {
  scenarios: {
    ramping_orders: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '10s', target: 10 },
        { duration: '10s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 150 },
        { duration: '30s', target: PEAK_VUS },
        { duration: '2m', target: PEAK_VUS },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '10s',
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
  // 현재 VU 수 확인 및 stage 변경 감지
  const currentVUs = exec.scenario?.currentVUs || exec.scenarioInTest?.currentVUs || 0;
  
  // stage 변경 감지: VU 수가 변경되었을 때 로그 (각 VU의 첫 iteration에서만)
  if (currentVUs !== lastVUCount && currentVUs > 0) {
    if (exec.scenario.iterationInInstance === 0) {
      console.log(`[STAGE CHANGE] 현재 동시 VU 수: ${currentVUs}`);
      // 각 stage 시작 시 10초 대기 (첫 번째 VU만)
      sleep(10);
    }
    lastVUCount = currentVUs;
  }

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

  sleep(1);
}
