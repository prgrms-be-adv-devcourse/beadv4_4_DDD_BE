'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from '@/app/lib/axios'

interface OrderSummary {
  orderId: number
  orderNo: string
  orderedAt: string
  paymentDeadlineAt: string
  repProductName: string
  status: string
  totalCnt : number
  totalAmount: number
}

interface Pagination {
  page: number
  size: number
  hasNext: boolean
  totalElements: number
  totalPages: number
}

interface OrderApiResponse {
  isSuccess: boolean
  code: string
  message: string
  pagination: Pagination
  result: OrderSummary[]
}

const PAGE_SIZE = 10

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'ORDER_RECEIVED': return { label: '주문접수', color: '#666' }
    case 'PENDING_PAYMENT': return { label: '결제대기', color: '#f59e0b' }
    case 'PAYMENT_COMPLETED': return { label: '결제완료', color: '#22c55e' }
    case 'PREPARING_PRODUCT': return { label: '상품준비중', color: '#3b82f6' }
    case 'SHIPPING': return { label: '배송중', color: '#667eea' }
    case 'SHIPPING_COMPLETED': return { label: '배송완료', color: '#22c55e' }
    case 'CANCEL_REQUESTED' : return { label: "취소 요청", color: '#ef7777'}
    case 'CANCELED': return { label: '주문취소', color: '#ef4444' }
    default: return { label: status, color: '#999' }
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  }).format(date)
}
export default function OrdersPage() {
  // 상태 관리
  const [orders, setOrders] = useState<OrderSummary[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [isLoading, setIsLoading] = useState(false)

  // 필터 상태
  const [preset, setPreset] = useState<PresetKey>('month1')
  const [startDate, setStartDate] = useState(() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 1)
    return d.toISOString().slice(0, 10)
  })
  const [endDate, setEndDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [currentPage, setCurrentPage] = useState(1)

  // --- 4. 데이터 조회 API 호출 ---
  const fetchOrders = async () => {
    setIsLoading(true)
    try {
      // API 호출 (Spring Data JPA Pageable은 page가 0부터 시작하므로 -1)
      const res = await api.get<OrderApiResponse>('/api/v1/orders', {
        params: {
          page: currentPage - 1,
          size: PAGE_SIZE,
          startDate: startDate,
          endDate: endDate,
        }
      })

      const data = res.data

      if (data.isSuccess && data.result) {
        setOrders(data.result)
        setTotalPages(data.pagination?.totalPages || 1)
      } else {
        setOrders([])
      }
    } catch (error) {
      console.error('주문 내역 조회 실패:', error)
      // 필요 시 에러 처리 (예: 토스트 메시지)
    } finally {
      setIsLoading(false)
    }
  }
    // 필터나 페이지가 바뀌면 자동으로 조회
    useEffect(() => {
      fetchOrders()
    }, [startDate, endDate, currentPage])

    // 기간 프리셋 핸들러
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

    // 검색 버튼 클릭 핸들러 (사실 useEffect 때문에 자동 조회되지만, 명시적 새로고침 역할)
    const handleSearch = () => {
      setCurrentPage(1)
      fetchOrders()
    }

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
                {isLoading ? (
                    <tr><td colSpan={6} style={{ padding: '48px', textAlign: 'center', color: '#999' }}>로딩 중...</td></tr>
                ) : orders?.length > 0 ? (
                    orders.map((order) => {
                      const statusInfo = getStatusLabel(order.status)
                      return (
                          <tr key={order.orderId} style={{borderBottom: '1px solid #f0f0f0'}}>
                            {/* 1. 주문일시 (orderedAt 사용) */}
                            <td style={{padding: '14px 12px', color: '#666'}}>
                              {formatDate(order.orderedAt)}
                            </td>

                            {/* 2. 주문번호 */}
                            <td style={{padding: '14px 12px', fontWeight: 500}}>
                              {order.orderNo}
                            </td>

                            {/* 3. 상품정보 (repProductName 사용 - '코트' 등) */}
                            <td style={{padding: '14px 12px', color: '#333'}}>
                              {order.repProductName}
                            </td>

                            {/* 수량 */}
                            <td style={{padding: '14px 12px', color: '#333'}}>
                              {order.totalCnt}
                            </td>

                            {/* 4. 결제금액 */}
                            <td style={{
                              padding: '14px 12px',
                              textAlign: 'right',
                              fontWeight: 600,
                              color: '#333'
                            }}>
                              {new Intl.NumberFormat('ko-KR').format(order.totalAmount)}원
                            </td>

                            {/* 5. 배송상태 */}
                            <td style={{padding: '14px 12px', textAlign: 'center'}}>
                          <span style={{color: statusInfo.color, fontWeight: 600}}>
                            {statusInfo.label}
                          </span>
                            </td>

                            {/* 6. 상세 링크 */}
                            <td style={{padding: '14px 12px', textAlign: 'center'}}>
                              <Link href={`/mypage/orders/${order.orderId}`} style={{
                                fontSize: '13px',
                                color: '#667eea',
                                fontWeight: 500,
                                textDecoration: 'none'
                              }}>
                                상세보기
                              </Link>
                            </td>
                          </tr>
                      )
                    })
                ) : (
                    <tr>
                      <td colSpan={6}
                          style={{padding: '48px', textAlign: 'center', color: '#999'}}>해당 기간 주문 내역이
                        없습니다.
                      </td>
                    </tr>
                )}
                </tbody>
              </table>
            </div>

            {/* 페이지네이션 */}
            {!isLoading && orders.length > 0 && (
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

                  {/* 페이지 번호 (간단하게 1~totalPages 표시) */}
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
          </div>
        </div>
      </MypageLayout>
  )
}