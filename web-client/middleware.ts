import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'
import { jwtVerify } from 'jose'

const JWT_SECRET = new TextEncoder().encode(process.env.JWT_SECRET)

// 로그인이 꼭 필요한 페이지 (마이페이지, 주문 등)
const PROTECTED_PATHS = ['/mypage', '/orders', '/members']

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const token = request.cookies.get('accessToken')?.value

  // 1. 비회원이 보호된 페이지 접근 시 -> 로그인으로
  if (!token) {
    if (PROTECTED_PATHS.some(path => pathname.startsWith(path))) {
      return NextResponse.redirect(new URL('/login', request.url))
    }
    return NextResponse.next()
  }

  // 2. 회원 상태 체크
  try {
    const { payload } = await jwtVerify(token, JWT_SECRET)
    const status = payload.status as string

    // PRE_ACTIVE 상태인 경우
    if (status === 'PRE_ACTIVE') {
      // 가입 완료 페이지는 통과
      if (pathname.startsWith('/signup/complete')) {
        return NextResponse.next()
      }

      // 보호된 페이지 접근 시 -> 가입 완료 페이지로
      if (PROTECTED_PATHS.some(path => pathname.startsWith(path))) {
        return NextResponse.redirect(new URL('/signup/complete', request.url))
      }

      // **[중요]** 홈(/)이나 상품 목록 등은 통과!
      return NextResponse.next()
    }

    // ACTIVE 상태인데 가입 완료 페이지 접근 시 -> 홈으로
    if (status === 'ACTIVE' && pathname.startsWith('/signup/complete')) {
      return NextResponse.redirect(new URL('/', request.url))
    }

  } catch (error) {
    // 토큰 에러 시 쿠키 삭제 등 처리...
  }

  return NextResponse.next()
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}