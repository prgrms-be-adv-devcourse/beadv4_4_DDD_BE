// app/mypage/seller-info

'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import api from "@/app/lib/axios"
import Header from "@/app/components/Header";
import MypageNav from "@/app/components/MypageNav";

interface SellerInfo {
  businessName: string
  representativeName: string
  settlementBankName: string
  settlementBankAccount: string
  businessLicenseUrl: string
  status: string
}

export default function SellerInfoPage() {
  const router = useRouter()
  const [info, setInfo] = useState<SellerInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const fetchSellerInfo = async () => {
      try {
        const response = await api.get('/api/v2/members/seller')

        if (response.data.isSuccess) {
          setInfo(response.data.result)
        } else {
          setError(response.data.message || '정보를 불러오지 못했습니다.')
        }
      } catch (err: any) {
        console.error(err)
        if (err.response && err.response.status === 403) {
          alert('판매자만 접근할 수 있는 페이지입니다.')
          router.replace('/mypage')
        } else {
          setError('데이터 조회 중 오류가 발생했습니다.')
        }
      } finally {
        setLoading(false)
      }
    }

    fetchSellerInfo()
  }, [router])

  return (
      <div className="home-page">
        <Header />

        <div style={{ padding: '40px 20px', minHeight: '60vh' }}>
          <div className="container" style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <h1 style={{ fontSize: '32px', fontWeight: '700', marginBottom: '32px' }}>마이페이지</h1>

            <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
              <MypageNav />

              {/* Right Content */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '24px' }}>판매자 정보</h2>

                {loading ? (
                    <div>로딩 중...</div>
                ) : error ? (
                    <div style={{ color: 'red' }}>{error}</div>
                ) : info ? (
                    <div style={{
                      background: 'white',
                      padding: '32px',
                      borderRadius: '12px',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
                    }}>
                      {/* 상태 뱃지 */}
                      <div style={{ marginBottom: '24px' }}>
                    <span style={{
                      padding: '6px 12px',
                      borderRadius: '20px',
                      fontSize: '13px',
                      fontWeight: 600,
                      backgroundColor: info.status === 'ACTIVE' ? '#e6f7e6' : '#fff3cd',
                      color: info.status === 'ACTIVE' ? '#2e7d32' : '#856404'
                    }}>
                        {info.status === 'ACTIVE' ? 'ACTIVE' : info.status}
                    </span>
                      </div>

                      <div style={rowStyle}>
                        <label style={labelStyle}>상호명</label>
                        <div style={valueStyle}>{info.businessName}</div>
                      </div>

                      <div style={rowStyle}>
                        <label style={labelStyle}>대표자명</label>
                        <div style={valueStyle}>{info.representativeName}</div>
                      </div>

                      <div style={rowStyle}>
                        <label style={labelStyle}>정산 은행</label>
                        <div style={valueStyle}>{info.settlementBankName}</div>
                      </div>

                      <div style={rowStyle}>
                        <label style={labelStyle}>정산 계좌</label>
                        <div style={valueStyle}>{info.settlementBankAccount}</div>
                      </div>

                      <div style={{ ...rowStyle, borderBottom: 'none' }}>
                        <label style={labelStyle}>사업자 등록증</label>
                        <div style={valueStyle}>
                          {info.businessLicenseUrl ? (
                              <Link
                                  href={info.businessLicenseUrl}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  style={{
                                    color: '#667eea',
                                    textDecoration: 'underline',
                                    fontSize: '14px'
                                  }}
                              >
                                [이미지 보기]
                              </Link>
                          ) : (
                              <span style={{ color: '#999' }}>등록된 이미지 없음</span>
                          )}
                        </div>
                      </div>
                    </div>
                ) : null}

                <div style={{ marginTop: '24px' }}>
                  <Link
                      href="/mypage"
                      style={{
                        padding: '10px 20px',
                        background: '#f1f3ff',
                        color: '#667eea',
                        borderRadius: '8px',
                        textDecoration: 'none',
                        fontWeight: 600
                      }}
                  >
                    목록으로
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  )
}

// 스타일 정의
const rowStyle = {
  display: 'flex',
  padding: '16px 0',
  borderBottom: '1px solid #f0f0f0',
  alignItems: 'center' as const
}

const labelStyle = {
  width: '120px',
  color: '#666',
  fontWeight: 500,
  fontSize: '14px'
}

const valueStyle = {
  flex: 1,
  color: '#333',
  fontWeight: 600,
  fontSize: '15px'
}