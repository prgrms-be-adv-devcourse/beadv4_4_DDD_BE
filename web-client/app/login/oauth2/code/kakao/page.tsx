// app/login/oauth2/code/kakao

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
      if (response.data.isSuccess) {
        setMessage('로그인 성공!')
        window.dispatchEvent(new Event('loginStatusChanged'))
        setTimeout(() => router.push('/'), 1000)
      } else {
        setMessage(`실패: ${response.data.message}`)
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