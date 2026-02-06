import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function GET() {
  const cookieStore = cookies();
  // HttpOnly 쿠키에서 accessToken 추출
  const accessToken = cookieStore.get('accessToken')?.value;

  if (!accessToken) {
    return NextResponse.json({ isSuccess: false, message: '인증 토큰이 없습니다.' }, { status: 401 });
  }

  // 환경 변수에서 API URL 가져오기 (설정되지 않았을 경우를 대비한 기본값 설정은 선택 사항)
  const baseUrl = process.env.NEXT_PUBLIC_API_URL;

  if (!baseUrl) {
    console.error('API URL 환경 변수가 설정되지 않았습니다.');
    return NextResponse.json({ isSuccess: false, message: '서버 설정 오류' }, { status: 500 });
  }

  try {
    // 환경 변수를 사용하여 백엔드 요청 URL 구성
    const response = await fetch(`${baseUrl}/api/v1/auths/social-accounts/status`, {
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
      const errorData = await response.json().catch(() => ({
        isSuccess: false,
        message: `백엔드 요청 실패: ${response.statusText}`
      }));

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