import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get('type'); // 'kakao' 또는 'naver'
  const redirectUri = searchParams.get('redirectUri');
  const provider = type?.toUpperCase();
  const accessToken = cookies().get('accessToken')?.value;

  // 1. 백엔드 URL 환경 변수 처리 (기본값 설정)
  const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

  try {
    const response = await fetch(
        `${BACKEND_URL}/api/v1/auths/social-accounts/${provider}/link-url?redirectUri=${encodeURIComponent(redirectUri || '')}`,
        {
          headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
          },
        }
    );

    // 2. HTTP 응답 상태 확인 (response.ok)
    if (!response.ok) {
      // 응답 본문이 JSON일 수도 있으므로 파싱 시도 후 에러 정보 포함하여 반환
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json(
          {
            isSuccess: false,
            message: 'Backend API error',
            details: errorData
          },
          { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);

  } catch (error) {
    // 네트워크 오류나 기타 예외 처리
    console.error('Social link error:', error);
    return NextResponse.json(
        { isSuccess: false, message: 'Internal Server Error' },
        { status: 500 }
    );
  }
}