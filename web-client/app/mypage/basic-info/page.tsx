'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";

export default function BasicInfoPage() {
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const [realName, setRealName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')

  // 기본정보 조회
  useEffect(() => {
    fetchBasicInfo()
  }, [])

  const fetchBasicInfo = async () => {
    try {
      setLoading(true)

      const response = await api.get('/api/v1/members/me/basic-info')
      const basicInfo = response.data.result

      setRealName(basicInfo.realName || '')
      setEmail(basicInfo.email || '')
      setPhoneNumber(basicInfo.phoneNumber || '')
    } catch (error) {
      console.error('기본정보 조회 실패:', error)
      alert('기본정보를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  // 기본정보 수정
  const handleSave = async () => {
    try {
      setSaving(true)

      await api.patch('/api/v1/members/me/basic-info', {
        realName,
        email,
        phoneNumber,
      })

      alert('기본정보가 저장되었습니다.')
    } catch (error) {
      console.error('기본정보 저장 실패:', error)
      alert('저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
        <MypageLayout>
          <div style={{ maxWidth: '600px', textAlign: 'center', padding: '40px' }}>
            로딩 중...
          </div>
        </MypageLayout>
    )
  }

  return (
      <MypageLayout>
        <div style={{ maxWidth: '600px' }}>
          <h1 style={{ fontSize: '28px', fontWeight: 700, marginBottom: '24px' }}>기본 정보</h1>

          <div
              style={{
                background: 'white',
                borderRadius: '12px',
                padding: '24px',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                marginBottom: '24px',
              }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
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
                {realName ? realName.charAt(0).toUpperCase() : 'T'}
              </div>
              <div>
                <div style={{ fontSize: '14px', color: '#666', marginBottom: '4px' }}>이름</div>
                <div style={{ fontSize: '16px', fontWeight: 600 }}>{realName || '테스트 사용자'}</div>
                <div style={{ fontSize: '13px', color: '#666', marginTop: '2px' }}>{email}</div>
              </div>
            </div>
          </div>

          <div
              style={{
                background: 'white',
                borderRadius: '12px',
                padding: '24px',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                marginBottom: '24px',
              }}
          >
            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                이름
              </label>
              <input
                  type="text"
                  value={realName}
                  onChange={(e) => setRealName(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                    backgroundColor: 'white',
                  }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                이메일
              </label>
              <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="hello@example.com"
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                    backgroundColor: 'white',
                  }}
              />
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '14px', fontWeight: 500, marginBottom: '6px' }}>
                연락처
              </label>
              <input
                  type="text"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="010-1234-5678"
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: '1px solid #e0e0e0',
                    fontSize: '14px',
                    backgroundColor: 'white',
                  }}
              />
            </div>

            <button
                type="button"
                onClick={handleSave}
                disabled={saving}
                style={{
                  width: '100%',
                  marginTop: '8px',
                  padding: '10px 0',
                  borderRadius: '8px',
                  border: 'none',
                  background: saving ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  cursor: saving ? 'not-allowed' : 'pointer',
                  fontSize: '14px',
                  fontWeight: 600,
                }}
            >
              {saving ? '저장 중...' : '저장하기'}
            </button>
          </div>
        </div>
      </MypageLayout>
  )
}