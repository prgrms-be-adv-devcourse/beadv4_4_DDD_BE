'use client'

import { useRouter } from 'next/navigation'
import Link from 'next/link'
import Header from '../components/Header'

const API_URL = process.env.NEXT_PUBLIC_API_URL

export default function LoginPage() {
  const router = useRouter()

  // 카카오나 네이버 버튼을 눌렀을 때 실행되는 함수입니다.
  const handleSocialLogin = async (provider: 'kakao' | 'naver') => {
    try {
      // 0. API_URL 환경 변수 확인
      if (!API_URL) {
        console.error('NEXT_PUBLIC_API_URL 환경 변수가 설정되지 않았습니다. (.env 확인 필요)')
        alert('서버 설정에 문제가 있습니다. 관리자에게 문의해주세요.')
        return
      }

      // 1. 리다이렉트 주소를 준비합니다.
      const redirectUri = `${window.location.origin}/login/oauth2/code/${provider}`

      // 2. 백엔드에 "로그인 페이지 주소(URL)"를 달라고 요청합니다.
      const response = await fetch(
          `${API_URL}/api/v1/auths/oauth/${provider}/url?redirectUri=${encodeURIComponent(redirectUri)}`,
          { credentials: 'include' }
      )

      // HTTP 레벨 에러 먼저 처리
      if (!response.ok) {
        console.error('HTTP 에러:', response.status, response.statusText)
        alert('로그인 요청 중 서버 오류가 발생했습니다.')
        return
      }

      // 성공 응답일 때만 JSON 파싱
      const data = await response.json()

      if (data.isSuccess) {
        // 3. 성공하면 카카오/네이버 실제 로그인 화면으로 이동합니다.
        window.location.href = data.result
      } else {
        alert('로그인 주소를 가져오지 못했습니다.')
      }
    } catch (error) {
      console.error('에러 발생:', error)
      alert('서버와 통신 중 오류가 발생했습니다.')
    }
  }

  return (
      <div className="home-page">
        <Header />

        <section className="login-section">
          <div className="container">
            <div className="login-container">
              <div className="login-card">
                <h1 className="login-title">로그인</h1>
                <p className="login-subtitle">소셜 계정으로 간편하게 시작하세요</p>

                <div className="social-login" style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '20px' }}>
                  {/* 네이버 로그인 버튼 */}
                  <button
                      type="button"
                      className="social-button naver-button"
                      onClick={() => handleSocialLogin('naver')}
                      style={{ backgroundColor: '#03C75A', color: 'white', padding: '12px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Naver로 로그인
                  </button>

                  {/* 카카오 로그인 버튼 */}
                  <button
                      type="button"
                      className="social-button kakao-button"
                      onClick={() => handleSocialLogin('kakao')}
                      style={{ backgroundColor: '#FEE500', color: '#191919', padding: '12px', borderRadius: '8px', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Kakao로 로그인
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Footer 부분 */}
        <footer className="footer">
          <div className="container">
            <div className="footer-content" style={{ display: 'flex', justifyContent: 'space-between', padding: '40px 0' }}>
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
              <div className="footer-section" style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                <h3>이용안내</h3>
                <Link href="/terms">이용약관</Link>
                <Link href="/privacy">개인정보처리방침</Link>
              </div>
            </div>
            <div className="footer-bottom" style={{ textAlign: 'center', borderTop: '1px solid #eee', padding: '20px 0' }}>
              <p>&copy; 2024 뭐든사. All rights reserved.</p>
            </div>
          </div>
        </footer>
      </div>
  )
}