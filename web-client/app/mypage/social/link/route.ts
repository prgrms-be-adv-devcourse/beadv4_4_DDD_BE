// app/mypage/social/link/route.ts
import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { provider, code, state, redirectUri } = body;

    if (!provider) {
      return NextResponse.json({ isSuccess: false, message: '제공자 정보가 없습니다.' }, { status: 400 });
    }

    const accessToken = cookies().get('accessToken')?.value;
    const formattedProvider = provider.toUpperCase(); // KAKAO 또는 NAVER

    const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

    const response = await fetch(
        `${baseUrl}/api/v1/auths/social-accounts/${formattedProvider}/link?code=${code}&state=${state}&redirectUri=${encodeURIComponent(redirectUri)}`,
        {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${accessToken}` }
        }
    );
    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ isSuccess: false, message: '서버 에러가 발생했습니다.' }, { status: 500 });
  }
}