// app/login/oauth2/code/naver

'use client'

import api from '../../../../lib/axios';
import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState, useRef, Suspense } from 'react'

function LoginCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [message, setMessage] = useState('로그인 처리 중입니다...')
  const isProcessing = useRef(false)  // 추가

  useEffect(() => {
    const code = searchParams.get('code')
    const state = searchParams.get('state')
    const provider = window.location.pathname.includes('kakao') ? 'kakao' : 'naver'
    const redirectUri = window.location.href.split('?')[0]

    if (!code || !state) {
      setMessage('잘못된 로그인 요청입니다.')
      setTimeout(() => router.replace('/login'), 1500)
      return
    }

    // 이미 처리 중이면 무시
    if (isProcessing.current) return
    isProcessing.current = true  // 처리 시작 표시

    api.post(`/api/v1/auths/login/${provider}`, null, {
      params: { code, state, redirectUri }
    })
    .then((response) => {
      const { isSuccess, result, message } = response.data;
      if (isSuccess) {
        const memberStatus = result.status;
        if (memberStatus === 'PRE_ACTIVE') {
          setMessage('회원가입 마무리를 위해 추가 정보 입력 페이지로 이동합니다...');
          // 잠시 후 회원가입 완료 페이지로 이동
          setTimeout(() => router.replace('/signup/complete'), 1000);
        } else {
          setMessage('로그인 성공! 홈으로 이동합니다.');
          window.dispatchEvent(new Event('loginStatusChanged'));
          setTimeout(() => router.replace('/'), 1000);
        }
      } else {
        setMessage(`로그인 실패: ${message}`)
        setTimeout(() => router.replace('/login'), 1500)
      }
    })
    .catch(() => {
      setMessage('서버 연결 실패')
      setTimeout(() => router.replace('/login'), 1500)
    })
  }, [searchParams, router])

  return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        {message}
      </div>
  )
}

export default function CallbackPage() {
  return (
      <Suspense fallback={<div>로딩 중...</div>}>
        <LoginCallbackContent />
      </Suspense>
  )
}