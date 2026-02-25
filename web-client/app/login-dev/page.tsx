// web-client/app/login-dev/page.tsx

'use client'

import Link from 'next/link'
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Header from '../components/Header'

export default function LoginDevPage() {
  const router = useRouter()
  const [memberId, setMemberId] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage('')

    const id = memberId.trim()
    if (!id) {
      setErrorMessage('Member ID를 입력해주세요.')
      return
    }

    const numId = Number(id)
    if (!Number.isInteger(numId) || numId <= 0) {
      setErrorMessage('유효한 Member ID(양의 정수)를 입력해주세요.')
      return
    }

    setIsSubmitting(true)

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
      if (!apiUrl) {
        setErrorMessage('API URL이 설정되지 않았습니다. (NEXT_PUBLIC_API_URL)')
        setIsSubmitting(false)
        return
      }

      const url = `${apiUrl}/api/v1/auths/dev/login?memberId=${numId}`
      const res = await fetch(url, { method: 'POST', credentials: 'include' })

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}))
        const message =
          errData?.message || errData?.result?.message || `로그인 실패 (${res.status})`
        setErrorMessage(message)
        setIsSubmitting(false)
        return
      }

      router.push('/')
    } catch (err) {
      console.error('Dev login error:', err)
      setErrorMessage(
        err instanceof Error ? err.message : '네트워크 오류가 발생했습니다.'
      )
      setIsSubmitting(false)
    }
  }

  return (
    <div className="home-page">
      <Header />

      <section className="login-section">
        <div className="container">
          <div className="login-container">
            <div className="login-card">
              <h1 className="login-title">개발용 로그인</h1>
              <p className="login-subtitle">Member ID로 로그인합니다 (dev 전용)</p>

              <form className="login-form" onSubmit={handleSubmit}>
                <div className="form-group">
                  <label htmlFor="memberId" className="form-label">
                    Member ID
                  </label>
                  <input
                    type="text"
                    id="memberId"
                    inputMode="numeric"
                    className="form-input"
                    placeholder="Member ID를 입력하세요 (예: 1)"
                    value={memberId}
                    onChange={(e) => setMemberId(e.target.value)}
                    required
                    disabled={isSubmitting}
                  />
                </div>

                {errorMessage && (
                  <p className="login-error" role="alert">
                    {errorMessage}
                  </p>
                )}

                <button
                  type="submit"
                  className="login-button"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? '로그인 중...' : '로그인'}
                </button>
              </form>

              <div className="signup-link">
                일반 로그인이 필요하신가요?{' '}
                <Link href="/login" className="signup-link-text">
                  로그인 화면으로 이동
                </Link>
              </div>
            </div>
          </div>
        </div>
      </section>

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
