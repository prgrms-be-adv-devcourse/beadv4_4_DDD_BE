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
    const response = await fetch('http://localhost:8080/api/v1/auths/social-accounts/status', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      cache: 'no-store' // 캐시 방지
    });

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('백엔드 통신 에러:', error);
    return NextResponse.json({ isSuccess: false, message: '서버 통신 실패' }, { status: 500 });
  }
}