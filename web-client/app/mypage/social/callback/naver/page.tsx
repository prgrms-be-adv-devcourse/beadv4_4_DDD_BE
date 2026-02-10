'use client'

import { Suspense, useEffect, useRef, useCallback } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'

export default function SocialCallbackPage() {
  return (
    <Suspense fallback={<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><p>로딩 중...</p></div>}>
      <NaverCallbackContent />
    </Suspense>
  )
}

function NaverCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const isProcessing = useRef(false)

  // 고정된 값은 컴포넌트 외부로 빼거나 memoization 없이 사용 가능합니다.
  const provider = 'naver'

  // 1. useCallback을 사용하여 함수를 메모이제이션합니다.
  const completeLink = useCallback(async (code: string, state: string) => {
    try {
      // 주소 보정: 백엔드 redirectUri와 정확히 일치해야 함
      const redirectUri = window.location.origin + `/mypage/social/callback/${provider}`

      const response = await fetch('/mypage/social/link', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          provider,
          code,
          state,
          redirectUri,
        }),
      });

      const data = await response.json();

      if (data.status === 'SOCIAL_ACCOUNT_LINK_SUCCESS') {
        alert('성공적으로 연동되었습니다.');
      } else {
        alert(data.message || '연동에 실패했습니다.');
      }
    } catch (error) {
      console.error('연동 완료 처리 에러:', error);
      alert('연동 처리 중 오류가 발생했습니다.');
    } finally {
      router.push('/mypage/social');
    }
  }, [router, provider]); // 의존성 배열에 router와 provider 추가

  // 2. useEffect의 의존성 배열에 completeLink를 포함합니다.
  useEffect(() => {
    const code = searchParams.get('code')
    const state = searchParams.get('state')

    if (isProcessing.current) return

    if (code && state) {
      isProcessing.current = true
      completeLink(code, state)
    }
  }, [searchParams, completeLink]); // exhaustive-deps 준수

  return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <p>소셜 계정 연동을 처리 중입니다...</p>
      </div>
  )
}