'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios"

// 📌 중요: 백엔드 응답(DTO)과 여기 변수명이 정확히 일치해야 합니다.
// 콘솔(F12)에 찍히는 로그를 보고 만약 이름이 다르다면 여기서 수정해주세요.
interface DeliveryAddress {
  id: number
  addressName: string
  recipientName: string
  recipientPhone: string // 혹시 백엔드에서 phone, phoneNumber 등으로 보내는지 확인 필요
  zipCode: string        // 혹시 백엔드에서 zipcode, postalCode 등으로 보내는지 확인 필요
  address: string
  addressDetail: string
  isDefault: boolean
}

export default function AddressPage() {
  // 상단 프로필 카드용 상태
  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')
  const [profileImageUrl, setProfileImageUrl] = useState('')

  // 배송지 관련 상태
  const [addresses, setAddresses] = useState<DeliveryAddress[]>([])
  const [loading, setLoading] = useState(true)

  // 선택된 배송지 ID ('new'는 새 배송지 작성 모드)
  const [selectedId, setSelectedId] = useState<number | 'new'>('new')

  // 입력 폼 상태
  const [addressName, setAddressName] = useState('')
  const [recipientName, setRecipientName] = useState('')
  const [recipientPhone, setRecipientPhone] = useState('')
  const [zipCode, setZipCode] = useState('')
  const [address, setAddress] = useState('')
  const [addressDetail, setAddressDetail] = useState('')
  const [isDefault, setIsDefault] = useState(false)

  // 1. 초기 데이터 로드
  useEffect(() => {
    fetchAllData()
  }, [])

  const fetchAllData = async () => {
    try {
      setLoading(true)
      await Promise.allSettled([
        fetchProfileInfo(),
        fetchAddresses()
      ])
    } finally {
      setLoading(false)
    }
  }

  const fetchProfileInfo = async () => {
    try {
      const basicRes = await api.get('/api/v1/members/me/basic-info')
      setRealName(basicRes.data.result.realName || '')
      setEmail(basicRes.data.result.email || '')

      try {
        const profileRes = await api.get('/api/v1/members/me/profile')
        setProfileImageUrl(profileRes.data.result.profileImageUrl || '')
      } catch (e) { /* 프로필 없음 무시 */ }
    } catch (e) {
      console.error('사용자 정보 조회 실패', e)
    }
  }

  const fetchAddresses = async () => {
    try {
      const response = await api.get('/api/v1/members/me/addresses')
      const list: DeliveryAddress[] = response.data.result

      // 🔍 디버깅용: 실제 들어오는 데이터 필드명을 확인해보세요!
      console.log('📌 서버에서 받은 배송지 목록:', list);

      setAddresses(list)

      if (list.length > 0 && selectedId !== 'new') {
        const current = list.find(a => a.id === selectedId)
        if (!current) setSelectedId('new')
      }
    } catch (error) {
      console.error('배송지 목록 조회 실패:', error)
    }
  }

  // 2. 선택된 배송지가 변경될 때 폼 채우기
  useEffect(() => {
    if (selectedId === 'new') {
      resetForm()
    } else {
      const target = addresses.find((a) => a.id === selectedId)
      if (target) {
        setAddressName(target.addressName || '')
        setRecipientName(target.recipientName || '')
        // 혹시 데이터가 없더라도 빈 문자열로 처리
        setRecipientPhone(target.recipientPhone || '')
        setZipCode(target.zipCode || '')
        setAddress(target.address || '')
        setAddressDetail(target.addressDetail || '')
        setIsDefault(target.isDefault)
      }
    }
  }, [selectedId, addresses])

  const resetForm = () => {
    setAddressName('')
    setRecipientName('')
    setRecipientPhone('')
    setZipCode('')
    setAddress('')
    setAddressDetail('')
    setIsDefault(false)
  }

  // 3. 저장 핸들러
  const handleSave = async () => {
    if (!addressName || !recipientName || !recipientPhone || !zipCode || !address || !addressDetail) {
      alert('모든 정보를 입력해주세요.')
      return
    }

    const payload = {
      addressName,
      recipientName,
      recipientPhone,
      zipCode,
      address,
      addressDetail,
      isDefault
    }

    try {
      if (selectedId === 'new') {
        await api.post('/api/v1/members/me/addresses', payload)
        alert('배송지가 추가되었습니다.')
      } else {
        await api.patch(`/api/v1/members/me/addresses/${selectedId}`, payload)
        alert('배송지가 수정되었습니다.')
      }

      await fetchAddresses()
      setSelectedId('new')
    } catch (error: any) {
      console.error('배송지 저장 실패:', error)
      const msg = error.response?.data?.message || '저장에 실패했습니다.'
      alert(msg)
    }
  }

  const handleSetDefault = async () => {
    if (selectedId === 'new') return
    try {
      await api.patch(`/api/v1/members/me/addresses/${selectedId}/default`)
      alert('기본 배송지로 설정되었습니다.')
      await fetchAddresses()
    } catch (error: any) {
      console.error('기본 배송지 설정 실패:', error)
      alert(error.response?.data?.message || '설정에 실패했습니다.')
    }
  }

  const handleDelete = async () => {
    if (selectedId === 'new') return
    if (!confirm('정말 이 배송지를 삭제하시겠습니까?')) return
    try {
      await api.delete(`/api/v1/members/me/addresses/${selectedId}`)
      alert('배송지가 삭제되었습니다.')
      setSelectedId('new')
      await fetchAddresses()
    } catch (error: any) {
      console.error('배송지 삭제 실패:', error)
      alert(error.response?.data?.message || '삭제에 실패했습니다.')
    }
  }

  const avatarLetter = realName ? realName.charAt(0).toUpperCase() : 'U'

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>배송지 관리</h1>

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
                    background: profileImageUrl
                        ? `url(${profileImageUrl}) center/cover`
                        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    fontWeight: '600',
                    fontSize: '26px',
                    overflow: 'hidden',
                  }}
              >
                {!profileImageUrl && avatarLetter}
              </div>
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>{realName || '사용자'}</div>
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
              <h2 style={{ fontSize: '18px', fontWeight: 600 }}>배송지 목록 ({addresses.length}/10)</h2>
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

            {loading ? (
                <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>로딩 중...</div>
            ) : addresses.length === 0 ? (
                <div style={{ padding: '20px', textAlign: 'center', color: '#888' }}>등록된 배송지가 없습니다.</div>
            ) : (
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
                                flexShrink: 0,
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
                              <span style={{ fontSize: '14px', fontWeight: 600 }}>{addr.addressName}</span>
                              {addr.isDefault && (
                                  <span
                                      style={{
                                        fontSize: '11px',
                                        color: '#667eea',
                                        background: '#edf0ff',
                                        borderRadius: '999px',
                                        padding: '2px 8px',
                                        fontWeight: 500,
                                        flexShrink: 0,
                                      }}
                                  >
                            기본
                          </span>
                              )}
                            </div>

                            {/* 이름 · 연락처 (연락처가 있을 때만 점 출력) */}
                            <div style={{ fontSize: '12px', color: '#666' }}>
                              {addr.recipientName}
                              {addr.recipientPhone ? ` · ${addr.recipientPhone}` : ''}
                            </div>

                            {/* (우편번호) 주소 (우편번호가 있을 때만 괄호 출력) */}
                            <div style={{ fontSize: '12px', color: '#666', marginTop: '2px' }}>
                              {addr.zipCode && `(${addr.zipCode}) `}
                              {addr.address} {addr.addressDetail}
                            </div>
                          </div>
                        </div>
                      </button>
                  ))}
                </div>
            )}
          </div>

          {/* 입력 폼 카드 */}
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
                  minHeight: '32px'
                }}
            >
            <span style={{ fontSize: '12px', color: '#666' }}>
              {isDefault
                  ? '현재 기본 배송지로 설정되어 있습니다.'
                  : selectedId === 'new'
                      ? '체크 시 기본 배송지로 등록됩니다.'
                      : '이 배송지를 기본 배송지로 설정할 수 있어요.'}
            </span>

              {selectedId !== 'new' && (
                  <button
                      type="button"
                      disabled={isDefault}
                      onClick={handleSetDefault}
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
              )}
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                배송지 별칭
              </label>
              <input
                  type="text"
                  placeholder="예: 집, 회사"
                  value={addressName}
                  onChange={(e) => setAddressName(e.target.value)}
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
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
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
                  value={recipientPhone}
                  onChange={(e) => setRecipientPhone(e.target.value)}
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
                  value={zipCode}
                  onChange={(e) => setZipCode(e.target.value)}
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
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
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
                  value={addressDetail}
                  onChange={(e) => setAddressDetail(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                  }}
              />
            </div>

            {selectedId === 'new' && (
                <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <input
                      type="checkbox"
                      id="check_default"
                      checked={isDefault}
                      onChange={(e) => setIsDefault(e.target.checked)}
                      style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                  />
                  <label htmlFor="check_default" style={{ fontSize: '14px', cursor: 'pointer' }}>이 주소를 기본 배송지로 설정</label>
                </div>
            )}

            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                  type="button"
                  onClick={handleSave}
                  style={{
                    flex: 1,
                    padding: '12px 0',
                    borderRadius: '8px',
                    border: 'none',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 600,
                  }}
              >
                {selectedId === 'new' ? '등록하기' : '수정하기'}
              </button>

              {selectedId !== 'new' && (
                  <button
                      type="button"
                      onClick={handleDelete}
                      style={{
                        width: '80px',
                        padding: '12px 0',
                        borderRadius: '8px',
                        border: '1px solid #ff4d4f',
                        background: 'white',
                        color: '#ff4d4f',
                        cursor: 'pointer',
                        fontSize: '14px',
                        fontWeight: 600,
                      }}
                  >
                    삭제
                  </button>
              )}
            </div>
          </div>
        </div>
      </MypageLayout>
  )
}