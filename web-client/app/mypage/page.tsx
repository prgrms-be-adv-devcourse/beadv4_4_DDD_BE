'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import Header from '../components/Header'

export default function MyPage() {
  const router = useRouter()

  return (
    <div className="home-page">
      <Header />
      
      <div style={{ padding: '40px 20px', minHeight: '60vh' }}>
        <div className="container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
          <h1 style={{ fontSize: '32px', fontWeight: '700', marginBottom: '32px' }}>마이페이지</h1>

          <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
            {/* Left Navigation */}
            <aside
              style={{
                width: '220px',
                background: 'white',
                borderRadius: '12px',
                padding: '20px 16px',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
              }}
            >
              <div style={{ marginBottom: '16px' }}>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    marginBottom: '8px',
                  }}
                >
                  <div
                    style={{
                      width: '40px',
                      height: '40px',
                      borderRadius: '50%',
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'white',
                      fontWeight: 600,
                      fontSize: '18px',
                    }}
                  >
                    T
                  </div>
                  <div>
                    <div style={{ fontSize: '13px', fontWeight: 600 }}>test@example.com</div>
                    <div style={{ fontSize: '12px', color: '#777' }}>뭐든사 회원</div>
                  </div>
                </div>
              </div>

              <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '14px' }}>
                <div style={{ fontSize: '12px', color: '#999', margin: '8px 0 4px' }}>내 정보</div>
                <Link
                  href="/mypage"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  마이페이지 홈
                </Link>
                <Link
                  href="/mypage/profile"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  기본 정보 수정
                </Link>
                <Link
                  href="/mypage/profile/edit"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  프로필 수정
                </Link>
                <Link
                  href="/mypage/address"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  배송지
                </Link>

                <div style={{ fontSize: '12px', color: '#999', margin: '12px 0 4px' }}>주문 · 혜택</div>
                <Link
                  href="/mypage/orders"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  주문 내역
                </Link>
                <Link
                  href="/mypage/cancel"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  취소/반품 내역
                </Link>
                <Link
                  href="/mypage/favorites"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  좋아요
                </Link>

                <div style={{ fontSize: '12px', color: '#999', margin: '12px 0 4px' }}>판매</div>
                <Link
                  href="/mypage/seller-request"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  판매자 전환
                </Link>
                <Link
                  href="/products/create"
                  style={{
                    padding: '8px 10px',
                    borderRadius: '8px',
                    textDecoration: 'none',
                    color: '#333',
                    fontSize: '13px',
                  }}
                >
                  상품 등록
                </Link>
              </nav>
            </aside>

            {/* Right Content */}
            <div style={{ flex: 1, minWidth: 0 }}>
              <div
                style={{
                  background: 'white',
                  borderRadius: '12px',
                  padding: '24px 24px 20px',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                  marginBottom: '24px',
                }}
              >
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: '20px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div
                      style={{
                        width: '64px',
                        height: '64px',
                        borderRadius: '50%',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white',
                        fontWeight: '600',
                        fontSize: '26px',
                      }}
                    >
                      T
                    </div>
                    <div>
                      <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '4px' }}>test@example.com</h2>
                      <p style={{ color: '#666', fontSize: '13px' }}>뭐든사 회원</p>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <Link
                      href="/mypage/profile"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        fontSize: '13px',
                        color: '#667eea',
                        textDecoration: 'none',
                        padding: '6px 10px',
                        borderRadius: '999px',
                        border: '1px solid #e0e0ff',
                        background: '#f8f8ff',
                      }}
                    >
                      기본 정보 수정
                    </Link>
                    <Link
                      href="/mypage/profile/edit"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        fontSize: '13px',
                        color: '#667eea',
                        textDecoration: 'none',
                        padding: '6px 10px',
                        borderRadius: '999px',
                        border: '1px solid #e0e0ff',
                        background: '#f8f8ff',
                      }}
                    >
                      프로필 수정
                    </Link>
                    <Link
                      href="/mypage/address"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        fontSize: '13px',
                        color: '#667eea',
                        textDecoration: 'none',
                        padding: '6px 10px',
                        borderRadius: '999px',
                        border: '1px solid #e0e0ff',
                        background: '#f8f8ff',
                      }}
                    >
                      배송지
                    </Link>
                    <Link
                      href="/mypage/seller-request"
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        fontSize: '13px',
                        color: '#667eea',
                        textDecoration: 'none',
                        padding: '6px 10px',
                        borderRadius: '999px',
                        border: '1px solid #e0e0ff',
                        background: '#f8f8ff',
                      }}
                    >
                      판매자 전환
                    </Link>
                  </div>
                </div>
              </div>

              {/* 뭐든사 머니 충전하기 */}
              <div
                style={{
                  background: 'white',
                  borderRadius: '12px',
                  padding: '32px',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                  marginBottom: '24px',
                }}
              >
                <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '16px' }}>뭐든사 머니</h3>
                <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                  결제 시 사용할 수 있는 뭐든사 전용 머니입니다.
                </p>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '24px',
                    padding: '16px 20px',
                    borderRadius: '8px',
                    background: '#f8f9ff',
                  }}
                >
                  <div>
                    <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>보유 머니</div>
                    <div style={{ fontSize: '20px', fontWeight: '700', color: '#333' }}>50,000원</div>
                  </div>
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <button
                    type="button"
                    onClick={() => router.push('/mypage/money/charge')}
                    style={{
                      padding: '10px 24px',
                      borderRadius: '8px',
                      border: 'none',
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      color: 'white',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 600,
                      whiteSpace: 'nowrap',
                    }}
                  >
                    충전하기
                  </button>
                </div>
              </div>

              <div
                style={{
                  background: 'white',
                  borderRadius: '12px',
                  padding: '24px 32px 32px',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <Link
                    href="/mypage/orders"
                    style={{
                      padding: '16px',
                      background: '#f5f5f5',
                      borderRadius: '8px',
                      textDecoration: 'none',
                      color: '#333',
                      fontSize: '14px',
                      fontWeight: '500',
                      transition: 'background-color 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#e0e0e0')}
                    onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#f5f5f5')}
                  >
                    주문 내역
                  </Link>
                  <Link
                    href="/mypage/cancel"
                    style={{
                      padding: '16px',
                      background: '#f5f5f5',
                      borderRadius: '8px',
                      textDecoration: 'none',
                      color: '#333',
                      fontSize: '14px',
                      fontWeight: '500',
                      transition: 'background-color 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#e0e0e0')}
                    onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#f5f5f5')}
                  >
                    취소/반품 내역
                  </Link>
                  <Link
                    href="/mypage/favorites"
                    style={{
                      padding: '16px',
                      background: '#f5f5f5',
                      borderRadius: '8px',
                      textDecoration: 'none',
                      color: '#333',
                      fontSize: '14px',
                      fontWeight: '500',
                      transition: 'background-color 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#e0e0e0')}
                    onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#f5f5f5')}
                  >
                    좋아요
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

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
