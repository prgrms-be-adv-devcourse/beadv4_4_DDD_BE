'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from '@/app/lib/axios'

interface OrderSummary {
  orderId: number
  orderNo: string
  orderedAt: string
  repProductName: string
  status: string
  totalAmount: number
}

interface OrderApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: OrderSummary[]
  pagination?: {
    totalPages: number
  }
}

const PAGE_SIZE = 10

const TARGET_STATUSES = [
  'CANCEL_REQUESTED', // 취소요청
  'CANCELLED_BY_USER', // 취소완료
  'REFUND_REQUESTED', // 환불요청
  'REFUNDED', // 환불완료
]

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

const getStatusInfo = (status: string) => {
  if (status.includes('CANCEL')) {
    return status === 'CANCEL_REQUESTED'
        ? { label: '취소요청', color: '#666', type: '취소' }
        : { label: '취소완료', color: '#ef4444', type: '취소' }
  }
  if (status.includes('REFUND') || status.includes('RETURN')) {
    return status.includes('REQUESTED')
        ? { label: '환불 요청', color: '#f59e0b', type: '환불' }
        : { label: '환불 완료', color: '#22c55e', type: '환불' }
  }
  return { label: status, color: '#999', type: '-' }
}

// 날짜 포맷
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(new Date(dateString))
}

export default function CancelPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [isLoading, setIsLoading] = useState(false)

  // 필터 State
  const [preset, setPreset] = useState<PresetKey>('month1')
  const [startDate, setStartDate] = useState(() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 1)
    return d.toISOString().slice(0, 10)
  })
  const [endDate, setEndDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [currentPage, setCurrentPage] = useState(1)

  // --- API 호출 ---
  const fetchOrders = async () => {
    setIsLoading(true)
    try {
      const res = await api.get<OrderApiResponse>('/api/v1/orders', {
        params: {
          page: currentPage - 1,
          size: PAGE_SIZE,
          startDate,
          endDate
        }
      })

      const data = res.data
      if (data.isSuccess && data.result) {
        const filtered = data.result.filter(order =>
            TARGET_STATUSES.includes(order.status)
        )
        setOrders(filtered)
        setTotalPages(data.pagination?.totalPages || 1)
      } else {
        setOrders([])
      }
    } catch (error) {
      console.error('취소 내역 조회 실패:', error)
      setOrders([])
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders()
  }, [startDate, endDate, currentPage])

  // --- 이벤트 핸들러 ---
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
    fetchOrders()
  }

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>취소/반품 내역</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          취소·반품 신청 내역과 처리 상태를 확인할 수 있어요.
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
        <div style={{ background: 'white', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', border: '1px solid #f0f0f0', overflow: 'hidden' }}>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
              <thead>
              <tr style={{ background: '#f8f9fa', borderBottom: '2px solid #eee' }}>
                <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>주문일시</th>
                <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>주문번호</th>
                <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>구분</th>
                <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>상품정보</th>
                <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>금액</th>
                <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>처리상태</th>
              </tr>
              </thead>
              <tbody>
              {isLoading ? (
                  <tr><td colSpan={6} style={{ padding: '48px', textAlign: 'center', color: '#999' }}>로딩 중...</td></tr>
              ) : orders?.length > 0 ? (
                  orders.map((item) => {
                    const info = getStatusInfo(item.status)
                    // 만약 필터링된 결과가 아니면(일반 주문이면) 렌더링 안 함
                    // (위 fetchOrders에서 이미 걸러내지만, 안전장치)
                    if (info.type === '-') return null

                    return (
                        <tr key={item.orderId} style={{ borderBottom: '1px solid #f0f0f0' }}>
                          {/* 1. 신청일 (주문일시 or 업데이트일시) */}
                          <td style={{ padding: '14px 12px', color: '#666' }}>
                            {formatDate(item.orderedAt)}
                          </td>

                          {/* 2. 주문번호 */}
                          <td style={{ padding: '14px 12px', fontWeight: 500 }}>
                            {item.orderNo}
                          </td>

                          {/* 3. 구분 (취소 vs 반품) */}
                          <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                          <span style={{
                            color: info.type === '취소' ? '#666' : '#667eea',
                            fontWeight: 600
                          }}>
                            {info.type}
                          </span>
                          </td>

                          {/* 4. 상품정보 */}
                          <td style={{ padding: '14px 12px', color: '#333' }}>
                            {item.repProductName}
                          </td>

                          {/* 5. 금액 */}
                          <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                            {new Intl.NumberFormat('ko-KR').format(item.totalAmount)}원
                          </td>

                          {/* 6. 처리상태 */}
                          <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                          <span style={{ color: info.color, fontWeight: 600 }}>
                            {info.label}
                          </span>
                          </td>
                        </tr>
                    )
                  })
              ) : (
                  <tr>
                    <td colSpan={6} style={{ padding: '48px', textAlign: 'center', color: '#999' }}>
                      해당 기간 내역이 없습니다.
                    </td>
                  </tr>
              )}
              </tbody>
            </table>
          </div>

          {/* 페이지네이션 (필요시 활성화) */}
          {!isLoading && orders?.length > 0 && (
              <div style={{ display: 'flex', justifyContent: 'center', padding: '16px' }}>
                {/* 페이지네이션 버튼들... (주문내역과 동일) */}
              </div>
          )}
        </div>
      </div>
    </MypageLayout>
  )
}