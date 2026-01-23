'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'

interface JwtTokenResponse {
  accessToken: string
  refreshToken: string
  accessTokenExpiresIn: number
  refreshTokenExpiresIn: number
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: JwtTokenResponse
}

export default function LoginCallbackPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [errorMessage, setErrorMessage] = useState<string>('')

  useEffect(() => {
    const handleCallback = async () => {
      const code = searchParams.get('code')
      const state = searchParams.get('state')
      // sessionStorage에서 provider 가져오기 (없으면 기본값으로 naver)
      const provider = typeof window !== 'undefined' 
        ? (sessionStorage.getItem('oauth_provider') || 'naver')
        : 'naver'
      
      // 사용 후 sessionStorage에서 제거
      if (typeof window !== 'undefined') {
        sessionStorage.removeItem('oauth_provider')
      }

      if (!code || !state) {
        setStatus('error')
        setErrorMessage('인증 코드를 받지 못했습니다.')
        return
      }

      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
        const redirectUri = `${window.location.origin}/login/callback`
        
        // 소셜 로그인 API 호출
        const response = await fetch(
          `${apiUrl}/api/v1/auths/login/${provider}?code=${encodeURIComponent(code)}&redirectUri=${encodeURIComponent(redirectUri)}&state=${encodeURIComponent(state)}`,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
          }
        )

        if (!response.ok) {
          const errorText = await response.text()
          console.error('소셜 로그인 실패:', response.status, errorText)
          let errorMsg = `로그인 실패 (${response.status})`
          try {
            const errorResponse = JSON.parse(errorText)
            if (errorResponse.message) {
              errorMsg = errorResponse.message
            }
          } catch (e) {
            // JSON 파싱 실패 시 기본 메시지 사용
          }
          throw new Error(errorMsg)
        }

        const apiResponse: ApiResponse = await response.json()

        if (apiResponse.isSuccess && apiResponse.result) {
          // JWT 토큰 저장
          localStorage.setItem('accessToken', apiResponse.result.accessToken)
          localStorage.setItem('refreshToken', apiResponse.result.refreshToken)
          localStorage.setItem('accessTokenExpiresIn', apiResponse.result.accessTokenExpiresIn.toString())
          localStorage.setItem('refreshTokenExpiresIn', apiResponse.result.refreshTokenExpiresIn.toString())

          setStatus('success')
          // 홈으로 리다이렉트
          setTimeout(() => {
            router.push('/')
          }, 1500)
        } else {
          throw new Error(apiResponse.message || '로그인에 실패했습니다.')
        }
      } catch (error) {
        console.error('소셜 로그인 실패:', error)
        const errorMsg = error instanceof Error ? error.message : '로그인 중 오류가 발생했습니다.'
        setStatus('error')
        setErrorMessage(errorMsg)
      }
    }

    handleCallback()
  }, [searchParams, router])

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh',
      flexDirection: 'column',
      gap: '20px'
    }}>
      {status === 'loading' && (
        <>
          <div style={{ 
            width: '48px', 
            height: '48px', 
            border: '4px solid #f3f3f3',
            borderTop: '4px solid #667eea',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }}></div>
          <p>로그인 처리 중...</p>
          <style jsx>{`
            @keyframes spin {
              0% { transform: rotate(0deg); }
              100% { transform: rotate(360deg); }
            }
          `}</style>
        </>
      )}
      
      {status === 'success' && (
        <>
          <div style={{ fontSize: '48px' }}>✓</div>
          <p style={{ color: '#4CAF50', fontSize: '18px', fontWeight: '600' }}>로그인 성공!</p>
          <p style={{ color: '#666' }}>잠시 후 홈으로 이동합니다...</p>
        </>
      )}
      
      {status === 'error' && (
        <>
          <div style={{ fontSize: '48px', color: '#f44336' }}>✕</div>
          <p style={{ color: '#f44336', fontSize: '18px', fontWeight: '600' }}>로그인 실패</p>
          <p style={{ color: '#666' }}>{errorMessage}</p>
          <button
            onClick={() => router.push('/login')}
            style={{
              padding: '12px 24px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '600',
              marginTop: '20px'
            }}
          >
            로그인 페이지로 돌아가기
          </button>
        </>
      )}
    </div>
  )
}
