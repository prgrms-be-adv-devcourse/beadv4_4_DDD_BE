'use client'

import Link from 'next/link'
import { useState } from 'react'
import { useRouter } from 'next/navigation'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // 실제 로그인 로직은 여기에 구현
    console.log('Login:', { email, password, rememberMe })
    // 로그인 성공 후 홈으로 이동
    // router.push('/')
  }

  const handleSocialLogin = async (provider: 'kakao' | 'naver') => {
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      // redirectUri는 백엔드 설정에 맞춰서 프론트엔드 도메인만 전달 (쿼리 파라미터 제거)
      const redirectUri = `${typeof window !== 'undefined' ? window.location.origin : 'http://localhost:3000'}/login/callback`
      
      console.log('소셜 로그인 시작:', { provider, redirectUri, apiUrl })
      
      // OAuth 로그인 URL 조회
      const url = `${apiUrl}/api/v1/auths/oauth/${provider}/url?redirectUri=${encodeURIComponent(redirectUri)}`
      console.log('요청 URL:', url)
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })
      
      console.log('응답 상태:', response.status, response.statusText)
      
      if (!response.ok) {
        let errorText = ''
        try {
          errorText = await response.text()
          console.error('OAuth URL 조회 실패 - 응답 본문:', errorText)
          
          // JSON 형식인 경우 파싱 시도
          try {
            const errorJson = JSON.parse(errorText)
            alert(errorJson.message || `소셜 로그인 URL을 가져오는데 실패했습니다. (${response.status})`)
          } catch {
            alert(`소셜 로그인 URL을 가져오는데 실패했습니다. (${response.status}: ${errorText})`)
          }
        } catch (e) {
          console.error('에러 응답 파싱 실패:', e)
          alert(`소셜 로그인 URL을 가져오는데 실패했습니다. (${response.status})`)
        }
        return
      }

      const apiResponse = await response.json()
      console.log('API 응답:', apiResponse)
      
      if (apiResponse.isSuccess) {
        if (apiResponse.result && typeof apiResponse.result === 'string') {
          console.log('OAuth URL 획득 성공:', apiResponse.result)
          // provider를 sessionStorage에 저장하여 콜백에서 사용
          if (typeof window !== 'undefined') {
            sessionStorage.setItem('oauth_provider', provider)
          }
          // OAuth 인증 페이지로 리다이렉트
          window.location.href = apiResponse.result
        } else {
          console.error('응답 result가 문자열이 아님:', apiResponse.result)
          alert('소셜 로그인 URL 형식이 올바르지 않습니다.')
        }
      } else {
        console.error('API 응답 실패:', apiResponse)
        alert(apiResponse.message || '소셜 로그인 URL을 가져오는데 실패했습니다.')
      }
    } catch (error) {
      console.error('소셜 로그인 실패:', error)
      if (error instanceof Error) {
        alert(`소셜 로그인 중 오류가 발생했습니다: ${error.message}`)
      } else {
        alert('소셜 로그인 중 오류가 발생했습니다.')
      }
    }
  }

  return (
    <div className="home-page">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="logo">
            <Link href="/">뭐든사</Link>
          </div>
          <nav className="nav">
            <Link href="/fashion">패션</Link>
            <Link href="/beauty">뷰티</Link>
            <Link href="/sale">세일</Link>
            <Link href="/magazine">매거진</Link>
          </nav>
          <div className="header-actions">
            <Link href="/search" className="search-btn">검색</Link>
            <Link href="/cart" className="cart-btn">장바구니</Link>
            <button className="user-btn">로그인</button>
          </div>
        </div>
      </header>

      {/* Login Section */}
      <section className="login-section">
        <div className="container">
          <div className="login-container">
            <div className="login-card">
              <h1 className="login-title">로그인</h1>
              <p className="login-subtitle">뭐든사에 오신 것을 환영합니다</p>

              <form className="login-form" onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="email" className="form-label">이메일</label>
                  <input
                    type="email"
                    id="email"
                    className="form-input"
                    placeholder="이메일을 입력하세요"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="password" className="form-label">비밀번호</label>
                  <div className="password-input-wrapper">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      id="password"
                      className="form-input"
                      placeholder="비밀번호를 입력하세요"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                    />
                    <button
                      type="button"
                      className="password-toggle"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? '👁️' : '👁️‍🗨️'}
                    </button>
                  </div>
                </div>

                <div className="form-options">
                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={rememberMe}
                      onChange={(e) => setRememberMe(e.target.checked)}
                      className="checkbox-input"
                    />
                    <span>로그인 상태 유지</span>
                  </label>
                  <Link href="/forgot-password" className="forgot-link">
                    비밀번호 찾기
                  </Link>
                </div>

                <button type="submit" className="login-button">
                  로그인
                </button>
              </form>

              <div className="login-divider">
                <span>또는</span>
              </div>

              <div className="social-login">
                <button 
                  type="button"
                  className="social-button naver-button"
                  onClick={() => handleSocialLogin('naver')}
                >
                  <span>N</span>
                  Naver로 로그인
                </button>
                <button 
                  type="button"
                  className="social-button kakao-button"
                  onClick={() => handleSocialLogin('kakao')}
                >
                  <span>K</span>
                  Kakao로 로그인
                </button>
              </div>

              <div className="signup-link">
                계정이 없으신가요?{' '}
                <Link href="/signup" className="signup-link-text">
                  회원가입
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <div className="footer-section">
              <h3>고객센터</h3>
              <p>1588-0000</p>
              <p>평일 09:00 - 18:00</p>
            </div>
            <div className="footer-section">
              <h3>회사정보</h3>
              <p>주소: 서울시 강남구</p>
              <p>사업자등록번호: 000-00-00000</p>
            </div>
            <div className="footer-section">
              <h3>이용안내</h3>
              <Link href="/terms">이용약관</Link>
              <Link href="/privacy">개인정보처리방침</Link>
            </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 뭐든사. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  )
}
