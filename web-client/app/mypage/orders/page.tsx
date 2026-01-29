'use client'

import Link from 'next/link'
import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

const mockOrders = [
  {
    id: 'ORD-2024-001',
    date: '2024-01-15',
    dateDisplay: '2024.01.15 14:30',
    status: '배송완료',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
    productSummary: '데일리 티셔츠 외 1건',
    quantity: 2,
    amount: '89,000원',
  },
  {
    id: 'ORD-2024-002',
    date: '2024-01-10',
    dateDisplay: '2024.01.10 11:20',
    status: '배송중',
    statusStyle: { color: '#667eea', fontWeight: 600 },
    productSummary: '루즈핏 코트',
    quantity: 1,
    amount: '45,000원',
  },
  {
    id: 'ORD-2024-003',
    date: '2024-01-05',
    dateDisplay: '2024.01.05 09:15',
    status: '주문접수',
    statusStyle: { color: '#666', fontWeight: 600 },
    productSummary: '니트 세트 외 2건',
    quantity: 3,
    amount: '132,000원',
  },
]

const PAGE_SIZE = 10

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

export default function OrdersPage() {
  const [preset, setPreset] = useState<PresetKey>('month1')
  const [startDate, setStartDate] = useState('2024-01-01')
  const [endDate, setEndDate] = useState('2024-01-31')
  const [currentPage, setCurrentPage] = useState(1)

  const handlePreset = (key: PresetKey) => {
    setPreset(key)
    setCurrentPage(1)
    const today = new Date()
    const end = new Date(today)
    let start = new Date(today)
    if (key === 'week') start.setDate(start.getDate() - 7)
    else if (key === 'month1') start.setMonth(start.getMonth() - 1)
    else if (key === 'month3') start.setMonth(start.getMonth() - 3)
    else if (key === 'month6') start.setMonth(start.getMonth() - 6)
    if (key !== 'direct') {
      setStartDate(start.toISOString().slice(0, 10))
      setEndDate(end.toISOString().slice(0, 10))
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    // Mock: 실제로는 startDate, endDate로 API 호출
    alert(`기간 검색: ${startDate} ~ ${endDate}\n(데모 화면입니다.)`)
  }

  const filteredOrders = mockOrders.filter((order) => {
    const d = order.date
    return d >= startDate && d <= endDate
  })

  const totalPages = Math.max(1, Math.ceil(filteredOrders.length / PAGE_SIZE))
  const paginatedOrders = filteredOrders.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  )

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>내 주문내역</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          주문한 상품의 배송 상태를 확인할 수 있어요.
        </p>

        {/* 기간 검색 */}
        <div
          style={{
            background: 'white',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            border: '1px solid #f0f0f0',
            marginBottom: '24px',
          }}
        >
          <div style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px', color: '#333' }}>
            조회 기간
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '16px' }}>
            {[
              { key: 'week' as PresetKey, label: '최근 1주일' },
              { key: 'month1' as PresetKey, label: '1개월' },
              { key: 'month3' as PresetKey, label: '3개월' },
              { key: 'month6' as PresetKey, label: '6개월' },
            ].map(({ key, label }) => (
              <button
                key={key}
                type="button"
                onClick={() => handlePreset(key)}
                style={{
                  padding: '8px 14px',
                  borderRadius: '8px',
                  border: preset === key ? '2px solid #667eea' : '1px solid #e0e0e0',
                  background: preset === key ? '#f8f8ff' : '#fff',
                  color: preset === key ? '#667eea' : '#666',
                  fontSize: '13px',
                  fontWeight: 500,
                  cursor: 'pointer',
                }}
              >
                {label}
              </button>
            ))}
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value)
                setPreset('direct')
                setCurrentPage(1)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
              }}
            />
            <span style={{ color: '#999', fontSize: '14px' }}>~</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value)
                setPreset('direct')
                setCurrentPage(1)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
              }}
            />
            <button
              type="button"
              onClick={handleSearch}
              style={{
                padding: '8px 20px',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                fontSize: '14px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              검색
            </button>
          </div>
        </div>

        {/* 테이블 */}
        <div
          style={{
            background: 'white',
            borderRadius: '12px',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            border: '1px solid #f0f0f0',
            overflow: 'hidden',
          }}
        >
          <div style={{ overflowX: 'auto' }}>
            <table
              style={{
                width: '100%',
                borderCollapse: 'collapse',
                fontSize: '14px',
              }}
            >
              <thead>
                <tr style={{ background: '#f8f9fa', borderBottom: '2px solid #eee' }}>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    주문일시
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    주문번호
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    상품정보
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    수량
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    결제금액
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    배송상태
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    주문상세
                  </th>
                </tr>
              </thead>
              <tbody>
                {paginatedOrders.map((order) => (
                  <tr
                    key={order.id}
                    style={{
                      borderBottom: '1px solid #f0f0f0',
                    }}
                  >
                    <td style={{ padding: '14px 12px', color: '#666' }}>{order.dateDisplay}</td>
                    <td style={{ padding: '14px 12px', fontWeight: 500 }}>{order.id}</td>
                    <td style={{ padding: '14px 12px', color: '#333' }}>{order.productSummary}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#333' }}>
                      {order.quantity}건
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                      {order.amount}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <span style={order.statusStyle}>{order.status}</span>
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <Link
                        href={`/mypage/orders/${order.id}`}
                        style={{
                          fontSize: '13px',
                          color: '#667eea',
                          fontWeight: 500,
                          textDecoration: 'none',
                        }}
                      >
                        상세보기
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredOrders.length > 0 && totalPages > 0 && (
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: '4px',
                padding: '16px',
                borderTop: '1px solid #f0f0f0',
              }}
            >
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                style={{
                  padding: '8px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  background: currentPage === 1 ? '#f5f5f5' : '#fff',
                  color: currentPage === 1 ? '#999' : '#333',
                  fontSize: '14px',
                  cursor: currentPage === 1 ? 'not-allowed' : 'pointer',
                }}
              >
                이전
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                <button
                  key={page}
                  type="button"
                  onClick={() => setCurrentPage(page)}
                  style={{
                    minWidth: '36px',
                    padding: '8px',
                    borderRadius: '8px',
                    border: currentPage === page ? '2px solid #667eea' : '1px solid #e0e0e0',
                    background: currentPage === page ? '#f8f8ff' : '#fff',
                    color: currentPage === page ? '#667eea' : '#333',
                    fontSize: '14px',
                    fontWeight: currentPage === page ? 600 : 400,
                    cursor: 'pointer',
                  }}
                >
                  {page}
                </button>
              ))}
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                style={{
                  padding: '8px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  background: currentPage === totalPages ? '#f5f5f5' : '#fff',
                  color: currentPage === totalPages ? '#999' : '#333',
                  fontSize: '14px',
                  cursor: currentPage === totalPages ? 'not-allowed' : 'pointer',
                }}
              >
                다음
              </button>
            </div>
          )}

          {filteredOrders.length === 0 && (
            <div
              style={{
                padding: '48px 24px',
                textAlign: 'center',
                color: '#999',
                fontSize: '14px',
              }}
            >
              해당 기간 주문 내역이 없습니다.
            </div>
          )}
        </div>

      </div>
    </MypageLayout>
  )
}