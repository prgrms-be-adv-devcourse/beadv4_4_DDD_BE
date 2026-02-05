'use client'

import {useEffect, useRef} from 'react'
import { useRouter, useParams, useSearchParams } from 'next/navigation'

export default function SocialCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const isProcessing = useRef(false)

  // params.provider 대신 직접 'kakao'를 명시합니다.
  const provider = 'kakao'

  useEffect(() => {
    const code = searchParams.get('code')
    const state = searchParams.get('state')

    if (isProcessing.current) return

    if (code && state) {
      isProcessing.current = true
      completeLink(code, state)
    }
  }, [searchParams])

  const completeLink = async (code: string, state: string) => {
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

      // 백엔드 SuccessStatus.SOCIAL_ACCOUNT_LINK_SUCCESS와 일치 확인
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
  };
  return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <p>소셜 계정 연동을 처리 중입니다...</p>
      </div>
  )
}