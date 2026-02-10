import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '2';

export const options = {
  vus: 10,
  duration: '30s',
};

export function setup() {
  const loginUrl = `${BASE_URL}/api/v1/auths/dev/login?memberId=${MEMBER_ID}`;
  const res = http.post(loginUrl, null, {
    headers: { Accept: 'application/json' },
  });

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
    throw new Error(`Dev login: accessToken missing. body=${res.body}`);
  }

  return { accessToken };
}

export default function (data) {
  if (!data || !data.accessToken) {
    console.warn('No accessToken from setup, skipping iteration');
    sleep(1);
    return;
  }

  const res = http.get(`${BASE_URL}/api/v1/payments/members`, {
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${data.accessToken}`,
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'has result': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && body.result != null;
      } catch (e) {
        return false;
      }
    },
  });

  if (res.status !== 200) {
    console.warn(`payment member failed: status=${res.status}, body=${res.body}`);
  }

  sleep(1);
}
