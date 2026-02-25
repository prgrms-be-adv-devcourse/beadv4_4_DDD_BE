// web-client/app/mypage/social/link-url/route.ts

import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get('type'); // 'kakao' 또는 'naver'
  const redirectUri = searchParams.get('redirectUri');

  // 1. provider(type) 유효성 검증 추가
  if (!type || !['kakao', 'naver'].includes(type.toLowerCase())) {
    return NextResponse.json(
        { isSuccess: false, message: 'Invalid or missing provider type' },
        { status: 400 }
    );
  }
  const provider = type.toUpperCase();

  // 2. accessToken 유효성 검증 추가
  const accessToken = cookies().get('accessToken')?.value;
  if (!accessToken) {
    return NextResponse.json(
        { isSuccess: false, message: 'Unauthorized: Access token is missing' },
        { status: 401 }
    );
  }

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

    if (!response.ok) {
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
    console.error('Social link error:', error);
    return NextResponse.json(
        { isSuccess: false, message: 'Internal Server Error' },
        { status: 500 }
    );
  }
}