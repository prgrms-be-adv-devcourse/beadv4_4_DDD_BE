import { NextRequest, NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const type = searchParams.get('type'); // 'kakao' 또는 'naver'
  const redirectUri = searchParams.get('redirectUri');
  const provider = type?.toUpperCase();
  const accessToken = cookies().get('accessToken')?.value;

  try {
    const response = await fetch(
        `http://localhost:8080/api/v1/auths/social-accounts/${provider}/link-url?redirectUri=${encodeURIComponent(redirectUri || '')}`,
        { headers: { 'Authorization': `Bearer ${accessToken}` } }
    );
    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ isSuccess: false }, { status: 500 });
  }
}