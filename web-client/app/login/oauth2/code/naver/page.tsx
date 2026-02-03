'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState, Suspense } from 'react'

const API_URL = process.env.NEXT_PUBLIC_API_URL

function LoginCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [message, setMessage] = useState('로그인 처리 중입니다...')

  useEffect(() => {
    // 1. 주소창에서 code와 state를 꺼냅니다.
    const code = searchParams.get('code')
    const state = searchParams.get('state')

    // 2. 현재 주소가 kakao인지 naver인지 확인합니다.
    const provider = window.location.pathname.includes('kakao') ? 'kakao' : 'naver'
    const redirectUri = window.location.href.split('?')[0]

    if (code && state) {
      // 3. 백엔드의 실제 로그인 API(@PostMapping("/login/{provider}"))를 호출합니다.
      fetch(`${API_URL}/api/v1/auths/login/${provider}?code=${code}&state=${state}&redirectUri=${encodeURIComponent(redirectUri)}`, {
        method: 'POST',
        credentials: 'include'
      })
      .then((res) => res.json())
      .then((data) => {
        if (data.isSuccess) {
          // 성공 시 메인 화면으로 이동
          setMessage('로그인 성공! 잠시 후 이동합니다.')
          window.dispatchEvent(new Event('loginStatusChanged'))
          setTimeout(() => router.push('/'), 1000)
        } else {
          setMessage(`로그인 실패: ${data.message}`)
        }
      })
      .catch(() => {
        setMessage('서버 연결에 실패했습니다.')
      })
    }
  }, [searchParams, router])

  return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontSize: '20px' }}>
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