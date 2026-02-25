// web-client/app/mypage/social/status/route.ts

import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET() {
  const cookieStore = cookies();
  // HttpOnly 쿠키에서 accessToken 추출
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json({ isSuccess: false, message: '인증 토큰이 없습니다.' }, { status: 401 });
  }

  try {
    // 백엔드 스프링 부트 서버(8080)로 요청 전달
    const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const response = await fetch(`${API_URL}/api/v1/auths/social-accounts/status`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      cache: 'no-store' // 캐시 방지
    });

    // HTTP 응답 상태 확인 (response.ok)
    if (!response.ok) {
      // 비-200 상태 코드일 때도 JSON 에러 응답이 있을 수 있으므로 파싱 시도
      // JSON 파싱 실패 시(HTML 에러 페이지 등)를 대비해 catch 처리
      const errorData = await response.json().catch(() => ({
        isSuccess: false,
        message: `백엔드 요청 실패: ${response.statusText}`
      }));

      // 백엔드의 상태 코드와 에러 데이터를 그대로 클라이언트에 전달
      return NextResponse.json(errorData, { status: response.status });
    }

    // 성공(200 OK)인 경우 정상적으로 데이터 파싱 및 반환
    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('백엔드 통신 에러:', error);
    return NextResponse.json({ isSuccess: false, message: '서버 통신 실패' }, { status: 500 });
  }
}