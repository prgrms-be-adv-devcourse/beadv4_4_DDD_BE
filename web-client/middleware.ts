import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { jwtVerify } from 'jose'

const JWT_SECRET = new TextEncoder().encode(process.env.JWT_SECRET || 'your_jwt_secret_key_must_be_long_enough')

// 1. 로그인이 필요 없는 공개 경로
const PUBLIC_PATHS = [
  '/login',
  '/login/oauth2',
  '/login/oauth2/code/kakao',
  '/login/oauth2/code/naver',
  '/_next',
  '/favicon.ico',
  '/public'
]

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const token = request.cookies.get('accessToken')?.value

  // 공개 경로는 검사 없이 통과
  if (PUBLIC_PATHS.some(path => pathname.startsWith(path))) {
    return NextResponse.next()
  }

  // 2. 토큰이 없으면 로그인 페이지로
  if (!token) {
    return NextResponse.redirect(new URL('/login', request.url))
  }

  try {
    // 3. 토큰 검증 및 디코딩 (jose 사용)
    const { payload } = await jwtVerify(token, JWT_SECRET)
    const status = payload.status as string // 토큰에 status 필드가 있어야 함

    // 4. PRE_ACTIVE 상태 처리
    if (status === 'PRE_ACTIVE') {
      // 이미 가입 완료 페이지에 있다면 통과
      if (pathname.startsWith('/signup/complete')) {
        return NextResponse.next()
      }
      // 다른 페이지 접근 시 가입 완료 페이지로 강제 이동
      return NextResponse.redirect(new URL('/signup/complete', request.url))
    }

    // 5. ACTIVE 상태 처리
    if (status === 'ACTIVE') {
      // 이미 가입된 회원이 가입 완료 페이지 접근 시 홈으로
      if (pathname.startsWith('/signup/complete')) {
        return NextResponse.redirect(new URL('/', request.url))
      }
    }

    return NextResponse.next()

  } catch (error) {
    // 토큰이 유효하지 않으면 로그인 페이지로 (만료 등)
    console.error('Middleware Token Error:', error)
    const response = NextResponse.redirect(new URL('/login', request.url))

    // 유효하지 않은 쿠키 삭제
    response.cookies.delete('accessToken')
    response.cookies.delete('refreshToken')
    return response
  }
}

// 미들웨어 적용 경로 설정
export const config = {
  matcher: [
    /*
     * 아래 경로를 제외한 모든 경로에 미들웨어 적용
     * - api (API 라우트) -> 백엔드 API는 별도 처리하므로 제외하거나 포함 가능 (여기선 프론트 라우팅 보호 목적이므로 제외 권장)
     * - _next/static (정적 파일)
     * - _next/image (이미지 최적화 파일)
     * - favicon.ico (파비콘)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}