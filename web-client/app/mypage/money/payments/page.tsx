'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../../components/MypageLayout'
import api from '@/app/lib/axios'

// GET /api/v2/payments 응답 항목
interface PaymentListItem {
  orderNo: string
  orderId: number
  status: string
  totalAmount: number
  productName: string | null
  createdAt: string
}

interface ApiPagination {
  page: number
  size: number
  hasNext: boolean
  totalElements: number
  totalPages: number
}

interface ApiResponsePage<T> {
  isSuccess: boolean
  code: string
  message: string
  pagination?: ApiPagination
  result: T[]
}

const PAGE_SIZE = 10

const PAYMENT_STATUS_LABEL: Record<string, string> = {
  PENDING: '결제 대기',
  IN_PROGRESS: '결제 진행',
  APPROVED: '결제 승인',
  CANCELED: '결제 취소',
  FAILED: '결제 실패',
  FINAL_FAILED: '최종 결제 실패',
  SUCCESS: '결제 완료',
  REFUND_REQUESTED: '환불 요청',
  REFUNDED: '환불 완료',
}

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

export default function MoneyPaymentsPage() {
  const [preset, setPreset] = useState<PresetKey>('week')
  const [startDate, setStartDate] = useState(() => {
    const today = new Date()
    const weekAgo = new Date(today)
    weekAgo.setDate(weekAgo.getDate() - 7)
    return weekAgo.toISOString().slice(0, 10)
  })
  const [endDate, setEndDate] = useState(() => {
    const today = new Date()
    return today.toISOString().slice(0, 10)
  })
  const [status, setStatus] = useState<string>('')
  const [orderNo, setOrderNo] = useState('')
  const [productName, setProductName] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [list, setList] = useState<PaymentListItem[]>([])
  const [listLoading, setListLoading] = useState(false)
  const [listError, setListError] = useState<string | null>(null)
  const [totalPages, setTotalPages] = useState(1)

  const fetchList = async (page: number) => {
    setListLoading(true)
    setListError(null)

    const params = new URLSearchParams()
    params.set('page', String(page))
    params.set('size', String(PAGE_SIZE))
    if (startDate) params.set('from', `${startDate}T00:00:00`)
    if (endDate) params.set('to', `${endDate}T23:59:59`)
    if (status) params.set('status', status)
    if (orderNo.trim()) params.set('orderNo', orderNo.trim())
    if (productName.trim()) params.set('productName', productName.trim())

    try {
      const res = await api.get<ApiResponsePage<PaymentListItem>>(
        `/api/v2/payments?${params.toString()}`
      )
      const data = res.data

      if (!data.isSuccess || !Array.isArray(data.result)) {
        setListError(data.message || '결제 내역을 불러오지 못했습니다.')
        setList([])
        setTotalPages(1)
        return
      }

      setList(data.result || [])
      const totalPagesFromServer = data.pagination?.totalPages ?? 1
      setTotalPages(Math.max(1, totalPagesFromServer))
    } catch {
      setListError('결제 내역을 불러오지 못했습니다.')
      setList([])
      setTotalPages(1)
    } finally {
      setListLoading(false)
    }
  }

  useEffect(() => {
    fetchList(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

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
      fetchList(0)
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    fetchList(0)
  }

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    fetchList(page - 1)
  }

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>
          뭐든사 머니 결제 내역
        </h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          조회 기간, 결제 상태, 주문 번호, 상품 이름으로 결제 내역을 검색할 수 있어요.
        </p>

        {/* 검색 영역 */}
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
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              gap: '12px',
              marginBottom: '16px',
            }}
          >
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value)
                setPreset('direct')
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
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
              }}
            />
          </div>
          <div style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: '#333' }}>
            결제 상태 · 주문 번호 · 상품 이름
          </div>
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              gap: '8px',
              marginBottom: '12px',
            }}
          >
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                minWidth: '140px',
              }}
            >
              <option value="">전체</option>
              {Object.entries(PAYMENT_STATUS_LABEL).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
            <input
              type="text"
              placeholder="주문 번호"
              value={orderNo}
              onChange={(e) => setOrderNo(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                width: '160px',
              }}
            />
            <input
              type="text"
              placeholder="상품 이름"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                width: '180px',
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
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'left',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    결제일시
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'left',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    주문번호
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'left',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    상품명
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'center',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    결제 상태
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'right',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    결제 금액
                  </th>
                </tr>
              </thead>
              <tbody>
                {listLoading && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{ padding: '24px', textAlign: 'center', color: '#999' }}
                    >
                      조회 중입니다...
                    </td>
                  </tr>
                )}
                {!listLoading && listError && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{ padding: '24px', textAlign: 'center', color: '#dc3545' }}
                    >
                      {listError}
                    </td>
                  </tr>
                )}
                {!listLoading && !listError && list.length === 0 && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{ padding: '24px', textAlign: 'center', color: '#999' }}
                    >
                      해당 조건의 결제 내역이 없습니다.
                    </td>
                  </tr>
                )}
                {!listLoading &&
                  !listError &&
                  list.map((item, idx) => {
                    const dateObj = new Date(item.createdAt)
                    const dateDisplay = dateObj.toLocaleString('ko-KR', {
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                    const statusLabel =
                      PAYMENT_STATUS_LABEL[item.status] || item.status || '-'
                    const amountFormatted = new Intl.NumberFormat('ko-KR').format(
                      Number(item.totalAmount ?? 0)
                    )
                    return (
                      <tr
                        key={`${item.orderNo}-${item.createdAt}-${idx}`}
                        style={{ borderBottom: '1px solid #f0f0f0' }}
                      >
                        <td style={{ padding: '14px 12px', color: '#666' }}>
                          {dateDisplay}
                        </td>
                        <td style={{ padding: '14px 12px', color: '#333' }}>
                          {item.orderNo}
                        </td>
                        <td style={{ padding: '14px 12px', color: '#333' }}>
                          {item.productName || '-'}
                        </td>
                        <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                          <span
                            style={{
                              color:
                                item.status === 'SUCCESS'
                                  ? '#22c55e'
                                  : item.status === 'CANCELED' ||
                                      item.status === 'REFUNDED'
                                    ? '#999'
                                    : '#666',
                              fontWeight: 500,
                            }}
                          >
                            {statusLabel}
                          </span>
                        </td>
                        <td
                          style={{
                            padding: '14px 12px',
                            textAlign: 'right',
                            fontWeight: 600,
                            color: '#333',
                          }}
                        >
                          {`${amountFormatted}원`}
                        </td>
                      </tr>
                    )
                  })}
              </tbody>
            </table>
          </div>

          {list.length > 0 && totalPages > 0 && (
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
                onClick={() => handlePageChange(Math.max(1, currentPage - 1))}
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
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(
                (page) => (
                  <button
                    key={page}
                    type="button"
                    onClick={() => handlePageChange(page)}
                    style={{
                      minWidth: '36px',
                      padding: '8px',
                      borderRadius: '8px',
                      border:
                        currentPage === page
                          ? '2px solid #667eea'
                          : '1px solid #e0e0e0',
                      background: currentPage === page ? '#f8f8ff' : '#fff',
                      color: currentPage === page ? '#667eea' : '#333',
                      fontSize: '14px',
                      fontWeight: currentPage === page ? 600 : 400,
                      cursor: 'pointer',
                    }}
                  >
                    {page}
                  </button>
                )
              )}
              <button
                type="button"
                onClick={() =>
                  handlePageChange(Math.min(totalPages, currentPage + 1))
                }
                disabled={currentPage === totalPages}
                style={{
                  padding: '8px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  background: currentPage === totalPages ? '#f5f5f5' : '#fff',
                  color: currentPage === totalPages ? '#999' : '#333',
                  fontSize: '14px',
                  cursor:
                    currentPage === totalPages ? 'not-allowed' : 'pointer',
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
