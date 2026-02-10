import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { jwtVerify } from 'jose'

// 1. JWT_SECRET 환경변수 강제 검증
const secretKey = process.env.JWT_SECRET
if (!secretKey || secretKey.length === 0) {
  throw new Error('❌ FATAL ERROR: JWT_SECRET 환경변수가 설정되지 않았습니다.')
}
const JWT_SECRET = new TextEncoder().encode(secretKey)

// 2. 공개 페이지 목록 (비회원 & PRE_ACTIVE 모두 접근 가능)
// 이 경로로 '시작하는' 모든 하위 경로가 허용됩니다.
const PUBLIC_PATH_PREFIXES = [
  '/login',
  '/products',  // 상품 목록, 상세
  '/categories', // 카테고리
  '/contents',  // 콘텐츠
  '/cart',      // 장바구니
  '/auth'       // 인증 관련
]

// 3. 정확히 일치해야 하는 공개 페이지
const PUBLIC_EXACT_PATHS = [
  '/' // 홈 화면
]

// 경로가 공개 페이지인지 확인하는 함수
function isPublicPath(pathname: string) {
  if (PUBLIC_EXACT_PATHS.includes(pathname)) return true
  return PUBLIC_PATH_PREFIXES.some(prefix => pathname.startsWith(prefix))
}

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const token = request.cookies.get('accessToken')?.value

  // ============================================================
  // A. 비회원 처리 (토큰 없음)
  // ============================================================
  if (!token) {
    // 공개 페이지라면 통과
    if (isPublicPath(pathname)) {
      return NextResponse.next()
    }
    // 그 외(마이페이지, 주문 등)는 로그인 페이지로 리다이렉트
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('redirect', pathname) // 로그인 후 돌아올 경로 저장
    return NextResponse.redirect(loginUrl)
  }

  // ============================================================
  // B. 회원 처리 (토큰 검증)
  // ============================================================
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET)
    const status = payload.status as string

    // --------------------------------------------------------
    // Case 1: PRE_ACTIVE 상태 (가입 미완료)
    // --------------------------------------------------------
    if (status === 'PRE_ACTIVE') {
      // 1. 가입 완료 페이지는 통과 (무한 루프 방지)
      if (pathname.startsWith('/signup/complete')) {
        return NextResponse.next()
      }

      // 2. 공개 페이지(홈, 상품 등)는 통과
      if (isPublicPath(pathname)) {
        return NextResponse.next()
      }

      // 3. 그 외 모든 페이지 -> 가입 완료 페이지로 강제 리다이렉트
      return NextResponse.redirect(new URL('/signup/complete', request.url))
    }

    // --------------------------------------------------------
    // Case 2: ACTIVE 상태 (정회원)
    // --------------------------------------------------------
    if (status === 'ACTIVE') {
      // 이미 가입된 회원이 가입 완료 페이지 접근 시 -> 홈으로
      if (pathname.startsWith('/signup/complete')) {
        return NextResponse.redirect(new URL('/', request.url))
      }
      // 나머지는 통과
      return NextResponse.next()
    }

    return NextResponse.next()

  } catch (error) {
    // ============================================================
    // C. 토큰 에러 처리 (만료, 변조 등)
    // ============================================================
    console.error('Middleware Token Error:', error)

    // 공개 페이지가 아니라면 로그인 페이지로 리다이렉트
    const response = isPublicPath(pathname)
        ? NextResponse.next()
        : NextResponse.redirect(new URL('/login', request.url))

    // 깨진 쿠키 삭제
    response.cookies.delete('accessToken')
    response.cookies.delete('refreshToken')

    return response
  }
}

// 미들웨어 적용 경로 설정 (API, 정적 파일 제외)
export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}