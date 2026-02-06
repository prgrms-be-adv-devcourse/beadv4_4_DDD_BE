import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';

// GET: 소셜 계정 연동 상태 조회
export async function GET() {
  const cookieStore = cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json({ isSuccess: false, message: '인증 토큰이 없습니다.' }, { status: 401 });
  }

  const baseUrl = process.env.NEXT_PUBLIC_API_URL;

  if (!baseUrl) {
    console.error('API URL 환경 변수가 설정되지 않았습니다.');
    return NextResponse.json({ isSuccess: false, message: '서버 설정 오류' }, { status: 500 });
  }

  try {
    const response = await fetch(`${baseUrl}/api/v1/auths/social-accounts/status`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      cache: 'no-store'
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({
        isSuccess: false,
        message: `백엔드 요청 실패: ${response.statusText}`
      }));

      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('백엔드 통신 에러:', error);
    return NextResponse.json({ isSuccess: false, message: '서버 통신 실패' }, { status: 500 });
  }
}

// POST: 소셜 계정 연동 처리
export async function POST(request: Request) {
  const cookieStore = cookies();
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json({ isSuccess: false, message: '인증 토큰이 없습니다.' }, { status: 401 });
  }

  const baseUrl = process.env.NEXT_PUBLIC_API_URL;

  if (!baseUrl) {
    console.error('API URL 환경 변수가 설정되지 않았습니다.');
    return NextResponse.json({ isSuccess: false, message: '서버 설정 오류' }, { status: 500 });
  }

  try {
    const body = await request.json();
    const { provider, code, state, redirectUri } = body;

    // 백엔드 API 스펙에 맞게 쿼리 파라미터로 전달
    const response = await fetch(
        `${baseUrl}/api/v1/auths/social-accounts/${provider}/link?code=${encodeURIComponent(code)}&state=${encodeURIComponent(state)}&redirectUri=${encodeURIComponent(redirectUri)}`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
        }
    );

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({
        isSuccess: false,
        message: `백엔드 요청 실패: ${response.statusText}`
      }));

      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('소셜 연동 에러:', error);
    return NextResponse.json({ isSuccess: false, message: '서버 통신 실패' }, { status: 500 });
  }
}