// web-client/app/mypage/page.tsx

'use client'

import Link from 'next/link'
import { useEffect, useState, useCallback } from 'react' // useCallback 추가
import Header from '../components/Header'
import MypageNav from '../components/MypageNav'
import api from "@/app/lib/axios";

export default function MyPage() {
  // 회원 role (초기값을 'MEMBER'로 안전하게 설정)
  const [role, setRole] = useState('MEMBER')

  // 기본 정보
  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')
  const [basicInfoLoading, setBasicInfoLoading] = useState(true)

  // 프로필
  const [profileImageUrl, setProfileImageUrl] = useState('')

  // 잔액 상태
  const [balance, setBalance] = useState<number | null>(null)
  const [balanceLoading, setBalanceLoading] = useState(true)
  const [balanceError, setBalanceError] = useState<string | null>(null)

  const fetchAllData = useCallback(async () => {
    try {
      setBasicInfoLoading(true)
      const [basicRes, profileRes] = await Promise.allSettled([
        api.get('/api/v1/members/me/basic-info'),
        api.get('/api/v1/members/me/profile')
      ])

      if (basicRes.status === 'fulfilled' && basicRes.value.data.isSuccess) {
        const result = basicRes.value.data.result;
        setRealName(result.realName || '')
        setEmail(result.email || '')
        setRole(result.role || 'MEMBER')
      }

      if (profileRes.status === 'fulfilled' && profileRes.value.data.isSuccess) {
        setProfileImageUrl(profileRes.value.data.result.profileImageUrl || '')
      }
    } catch (e) {
      console.error(e)
    } finally {
      setBasicInfoLoading(false)
    }
  }, [])

  const fetchBalance = useCallback(async () => {
    try {
      setBalanceLoading(true)
      const response = await api.get('/api/v1/payments/members')
      const data = response.data

      if (data.isSuccess && data.result != null) {
        setBalance(Number(data.result.balance))
      } else {
        setBalanceError(data.message || '잔액을 불러오지 못했습니다.')
      }
    } catch (error) {
      // console.error('잔액 조회 실패:', error) // 404 등 로그 너무 많이 찍히면 주석 처리
      setBalanceError('잔액을 불러오지 못했습니다.')
    } finally {
      setBalanceLoading(false)
    }
  }, [])

  useEffect(() => {
    // 1. 초기 로딩
    fetchAllData()
    fetchBalance()

    // 2. 이벤트 핸들러 정의
    const handleLoginStatusChanged = () => {
      console.log('로그인/권한 상태 변경 감지: 데이터 갱신')
      fetchAllData()
      fetchBalance()
    }

    // 3. 리스너 등록
    window.addEventListener('loginStatusChanged', handleLoginStatusChanged)

    // 4. 클린업 (언마운트 시 리스너 제거)
    return () => {
      window.removeEventListener('loginStatusChanged', handleLoginStatusChanged)
    }
  }, [fetchAllData, fetchBalance])

  const balanceDisplay =
      balanceLoading && balance === null
          ? '조회 중...'
          : balanceError
              ? balanceError
              : balance != null
                  ? `${new Intl.NumberFormat('ko-KR').format(balance)}원`
                  : '-'

  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U'

  // 링크 스타일 공통 상수
  const linkStyle = {
    padding: '8px 14px',
    borderRadius: '8px',
    border: '1px solid #e0e0ff',
    background: '#f8f8ff',
    color: '#667eea',
    fontSize: '13px',
    fontWeight: 500,
    textDecoration: 'none',
  }

  return (
      <div className="home-page">
        <Header />

        <div style={{ padding: '40px 20px', minHeight: '60vh' }}>
          <div className="container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <h1 style={{ fontSize: '32px', fontWeight: '700', marginBottom: '32px' }}>마이페이지</h1>

            <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
              <MypageNav role={role} />

              {/* Right Content */}
              <div style={{ flex: 1, minWidth: 0 }}>
                {/* 상단 프로필 카드 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px 24px 20px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '20px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                      <div
                          style={{
                            width: '64px',
                            height: '64px',
                            borderRadius: '50%',
                            background: profileImageUrl
                                ? `url(${profileImageUrl}) center/cover`
                                : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: 'white',
                            fontWeight: '600',
                            fontSize: '26px',
                            overflow: 'hidden'
                          }}
                      >
                        {basicInfoLoading ? '...' : (!profileImageUrl && avatarLetter)}
                      </div>
                      <div>
                        <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '4px' }}>
                          {basicInfoLoading ? '로딩 중...' : email || '이메일 미등록'}
                        </h2>
                        <p style={{ color: '#666', fontSize: '13px' }}>
                          {basicInfoLoading ? '로딩 중...' : realName ? `${realName} 님` : '이름 미등록'}
                        </p>
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '13px', color: '#666', marginBottom: '2px' }}>보유 머니</div>
                      <div
                          style={{
                            fontSize: '20px',
                            fontWeight: '700',
                            color: balanceError ? '#dc3545' : '#333',
                          }}
                      >
                        {balanceDisplay}
                      </div>
                    </div>
                  </div>
                </div>

                {/* 내 정보 카드 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px' }}>내 정보</h3>
                  <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    프로필, 배송지 등 내 정보를 관리할 수 있어요.
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    <Link href="/mypage/basic-info" style={linkStyle}>기본 정보</Link>
                    <Link href="/mypage/profile" style={linkStyle}>프로필</Link>
                    <Link href="/mypage/address" style={linkStyle}>배송지</Link>
                    <Link href="/mypage/social" style={linkStyle}>소셜 연동</Link>
                  </div>
                </div>

                {/* 주문 카드 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px' }}>주문</h3>
                  <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    주문 내역과 취소·반품을 확인할 수 있어요.
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    <Link href="/mypage/orders" style={linkStyle}>주문 내역</Link>
                    <Link href="/mypage/cancel" style={linkStyle}>취소/반품 내역</Link>
                  </div>
                </div>

                {/* 뭐든사 머니 카드 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px' }}>뭐든사 머니</h3>
                  <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    결제 시 사용할 수 있는 뭐든사 전용 머니예요.
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    <Link href="/mypage/money/charge" style={linkStyle}>충전하기</Link>
                    <Link href="/mypage/money/history" style={linkStyle}>사용 내역</Link>
                  </div>
                </div>

                {/* 저장 카드 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px' }}>저장</h3>
                  <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    좋아요한 상품과 스냅을 한 곳에서 모아볼 수 있어요.
                  </p>
                  <Link href="/mypage/favorites" style={{ display: 'inline-block', ...linkStyle }}>
                    좋아요
                  </Link>
                </div>

                {/* 판매 카드 - 역할(Role)에 따른 분기 */}
                <div
                    style={{
                      background: 'white',
                      borderRadius: '12px',
                      padding: '24px',
                      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                      marginBottom: '24px',
                    }}
                >
                  <h3 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px' }}>판매</h3>
                  <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
                    {role === 'MEMBER'
                        ? '판매자 전환 후 상품을 등록하고 관리할 수 있어요.'
                        : '상품 등록, 재고 관리 및 정산 내역을 확인할 수 있어요.'}
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                    {role === 'MEMBER' ? (
                        // 일반 회원일 때: 전환 신청 버튼
                        <Link href="/mypage/seller-request" style={linkStyle}>
                          판매자 전환
                        </Link>
                    ) : (
                        // MEMBER가 아닐 때 (SELLER 등): 판매자 관리 메뉴 전체 노출
                        <>
                          <Link href="/mypage/seller-info" style={linkStyle}>판매자 정보</Link>
                          <Link href="/mypage/products" style={linkStyle}>상품 관리</Link>
                          <Link href="/mypage/stock" style={linkStyle}>재고 관리</Link>
                          <Link href="/mypage/settlement" style={linkStyle}>정산 내역</Link>
                        </>
                    )}
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