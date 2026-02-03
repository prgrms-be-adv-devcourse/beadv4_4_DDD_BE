'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState, Suspense } from 'react'

const API_URL = process.env.NEXT_PUBLIC_API_URL

function LoginCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [message, setMessage] = useState('로그인 처리 중입니다...')

  useEffect(() => {
    const code = searchParams.get('code')
    const state = searchParams.get('state')

    const provider = window.location.pathname.includes('kakao') ? 'kakao' : 'naver'
    const redirectUri = window.location.href.split('?')[0]

    if (!code || !state) {
      setMessage('잘못된 로그인 요청입니다. 다시 시도해주세요.')
      setTimeout(() => router.replace('/login'), 1500)
      return
    }

    fetch(
        `${API_URL}/api/v1/auths/login/${provider}` +
        `?code=${encodeURIComponent(code)}` +
        `&state=${encodeURIComponent(state)}` +
        `&redirectUri=${encodeURIComponent(redirectUri)}`,
        {
          method: 'POST',
          credentials: 'include'
        }
    )
    .then((res) => {
      if (!res.ok) throw new Error('HTTP error')
      return res.json()
    })
    .then((data) => {
      if (data.isSuccess) {
        setMessage('로그인 성공! 잠시 후 이동합니다.')
        window.dispatchEvent(new Event('loginStatusChanged'))
        setTimeout(() => router.push('/'), 1000)
      } else {
        setMessage(`로그인 실패: ${data.message}`)
        setTimeout(() => router.replace('/login'), 1500)
      }
    })
    .catch(() => {
      setMessage('서버 연결에 실패했습니다.')
      setTimeout(() => router.replace('/login'), 1500)
    })
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