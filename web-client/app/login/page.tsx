'use client'

import Link from 'next/link'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Header from '../components/Header'

export default function LoginPage() {
  const router = useRouter()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    // Mock 로그인: 이메일 test@example.com, 비밀번호 1234
    if (email === 'test@example.com' && password === '1234') {
      // Mock 토큰 및 이메일 저장
      localStorage.setItem('accessToken', 'mock-access-token-' + Date.now())
      localStorage.setItem('refreshToken', 'mock-refresh-token-' + Date.now())
      localStorage.setItem('accessTokenExpiresIn', '3600')
      localStorage.setItem('refreshTokenExpiresIn', '86400')
      localStorage.setItem('email', email)
      
      // 로그인 상태 변경 이벤트 발생
      window.dispatchEvent(new Event('loginStatusChanged'))
      
      alert('로그인 성공!')
      router.push('/')
    } else {
      alert('이메일 또는 비밀번호가 올바르지 않습니다.\n이메일: test@example.com\n비밀번호: 1234')
    }
  }

  const handleSocialLogin = async (provider: 'kakao' | 'naver') => {
    // API 통신 제거됨
    alert('소셜 로그인 기능이 비활성화되었습니다.')
  }

  return (
    <div className="home-page">
      {/* Header */}
      <Header />

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
