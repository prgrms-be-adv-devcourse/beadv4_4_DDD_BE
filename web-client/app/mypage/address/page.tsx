'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

type Address = {
  id: number
  label: string
  receiver: string
  phone: string
  postalCode: string
  address1: string
  address2: string
  memo: string
  isDefault?: boolean
}

export default function AddressPage() {
  const [email, setEmail] = useState('test@example.com')
  const [addresses] = useState<Address[]>([
    {
      id: 1,
      label: '기본 배송지',
      receiver: '테스트 사용자',
      phone: '010-1234-5678',
      postalCode: '06236',
      address1: '서울특별시 강남구 테헤란로 123',
      address2: '101동 1001호',
      memo: '부재 시 경비실에 맡겨주세요',
      isDefault: true,
    },
    {
      id: 2,
      label: '회사',
      receiver: '테스트 사용자',
      phone: '010-0000-0000',
      postalCode: '04524',
      address1: '서울특별시 중구 세종대로 110',
      address2: '모든사타워 10층',
      memo: '리셉션에 맡겨주세요',
    },
  ])
  const [selectedId, setSelectedId] = useState<number | 'new'>(1)

  const [label, setLabel] = useState('')
  const [receiver, setReceiver] = useState('')
  const [phone, setPhone] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [address1, setAddress1] = useState('')
  const [address2, setAddress2] = useState('')
  const [isDefault, setIsDefault] = useState(false)

  useEffect(() => {
    if (typeof window === 'undefined') return
    const stored = localStorage.getItem('email')
    if (stored) {
      setEmail(stored)
    }
  }, [])

  useEffect(() => {
    if (selectedId === 'new') {
      setLabel('')
      setReceiver('')
      setPhone('')
      setPostalCode('')
      setAddress1('')
      setAddress2('')
      setIsDefault(false)
      return
    }
    const current = addresses.find((a) => a.id === selectedId)
    if (current) {
      setLabel(current.label)
      setReceiver(current.receiver)
      setPhone(current.phone)
      setPostalCode(current.postalCode)
      setAddress1(current.address1)
      setAddress2(current.address2)
      setIsDefault(!!current.isDefault)
    }
  }, [selectedId, addresses])

  return (
    <MypageLayout>
      <div style={{ maxWidth: '600px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>배송지 정보 입력</h1>

          {/* 상단 프로필 카드 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
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
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>테스트 사용자</div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>{email}</div>
              </div>
            </div>
          </div>
          {/* 배송지 목록 카드 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              marginBottom: '24px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 600 }}>배송지 선택</h2>
              <button
                type="button"
                onClick={() => setSelectedId('new')}
                style={{
                  padding: '8px 14px',
                  borderRadius: '999px',
                  border: '1px solid #e0e0ff',
                  background: '#f8f8ff',
                  color: '#667eea',
                  fontSize: '13px',
                  fontWeight: 500,
                  cursor: 'pointer',
                }}
              >
                + 새 배송지 추가
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {addresses.map((addr) => (
                <button
                  key={addr.id}
                  type="button"
                  onClick={() => setSelectedId(addr.id)}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '12px 14px',
                    borderRadius: '10px',
                    border: selectedId === addr.id ? '1px solid #667eea' : '1px solid #e0e0e0',
                    background: selectedId === addr.id ? '#f4f5ff' : '#fafafa',
                    cursor: 'pointer',
                    textAlign: 'left',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div
                      style={{
                        width: '18px',
                        height: '18px',
                        borderRadius: '50%',
                        border: '2px solid',
                        borderColor: selectedId === addr.id ? '#667eea' : '#ccc',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                    >
                      {selectedId === addr.id && (
                        <div
                          style={{
                            width: '10px',
                            height: '10px',
                            borderRadius: '50%',
                            backgroundColor: '#667eea',
                          }}
                        />
                      )}
                    </div>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
                        <span style={{ fontSize: '14px', fontWeight: 600 }}>{addr.label}</span>
                        {addr.isDefault && (
                          <span
                            style={{
                              fontSize: '11px',
                              color: '#667eea',
                              background: '#edf0ff',
                              borderRadius: '999px',
                              padding: '2px 8px',
                              fontWeight: 500,
                            }}
                          >
                            기본
                          </span>
                        )}
                      </div>
                      <div style={{ fontSize: '12px', color: '#666' }}>
                        {addr.receiver} · {addr.phone}
                      </div>
                      <div style={{ fontSize: '12px', color: '#666', marginTop: '2px' }}>
                        ({addr.postalCode}) {addr.address1} {addr.address2}
                      </div>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* 선택한 배송지 / 새 배송지 입력 카드 */}
          <div
            style={{
              background: 'white',
              borderRadius: '12px',
              padding: '24px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
            }}
          >
            <h2 style={{ fontSize: '18px', fontWeight: 600, marginBottom: '8px' }}>
              {selectedId === 'new' ? '새 배송지 입력' : '선택한 배송지 수정'}
            </h2>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '16px',
                gap: '8px',
              }}
            >
              <span style={{ fontSize: '12px', color: '#666' }}>
                {isDefault ? '현재 기본 배송지로 설정되어 있습니다.' : '이 배송지를 기본 배송지로 설정할 수 있어요.'}
              </span>
              <button
                type="button"
                disabled={isDefault}
                onClick={() => {
                  setIsDefault(true)
                  alert('기본 배송지 설정 기능은 Mock 화면입니다.')
                }}
                style={{
                  padding: '6px 12px',
                  borderRadius: '999px',
                  border: '1px solid #e0e0ff',
                  background: isDefault ? '#f1f1f5' : '#f8f8ff',
                  color: isDefault ? '#999' : '#667eea',
                  fontSize: '12px',
                  fontWeight: 500,
                  cursor: isDefault ? 'default' : 'pointer',
                  whiteSpace: 'nowrap',
                }}
              >
                {isDefault ? '기본 배송지' : '기본 배송지 설정'}
              </button>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                배송지 별칭
              </label>
              <input
                type="text"
                placeholder="예: 집, 회사"
                value={label}
                onChange={(e) => setLabel(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                수령인 이름
              </label>
              <input
                type="text"
                placeholder="예: 홍길동"
                value={receiver}
                onChange={(e) => setReceiver(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                연락처
              </label>
              <input
                type="text"
                placeholder="예: 010-1234-5678"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                우편번호
              </label>
              <input
                type="text"
                placeholder="예: 06236"
                value={postalCode}
                onChange={(e) => setPostalCode(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                기본 주소
              </label>
              <input
                type="text"
                placeholder="도로명 주소를 입력하세요"
                value={address1}
                onChange={(e) => setAddress1(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                상세 주소
              </label>
              <input
                type="text"
                placeholder="동/호수, 상세 위치를 입력하세요"
                value={address2}
                onChange={(e) => setAddress2(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                }}
              />
            </div>

            <button
              type="button"
              onClick={() =>
                alert(
                  selectedId === 'new'
                    ? '새 배송지 저장 기능은 Mock 화면입니다.'
                    : '선택한 배송지 수정 기능은 Mock 화면입니다.',
                )
              }
              style={{
                width: '100%',
                marginTop: '8px',
                padding: '10px 0',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 600,
              }}
            >
              저장하기
            </button>
          </div>

          <div style={{ marginTop: '24px', textAlign: 'center' }}>
            <Link
              href="/mypage"
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                borderRadius: '8px',
                border: '1px solid #e0e0ff',
                background: '#f8f8ff',
                color: '#667eea',
                fontSize: '13px',
                fontWeight: 500,
                textDecoration: 'none',
              }}
            >
              마이페이지로 돌아가기
            </Link>
          </div>
      </div>
    </MypageLayout>
  )
}

