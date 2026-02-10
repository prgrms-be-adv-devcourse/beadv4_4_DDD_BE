'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../../components/MypageLayout'
import api from '@/app/lib/axios'

interface PaymentMemberResponse {
  customerKey: string
  customerName: string
  customerEmail: string
  balance: number
}

interface PaymentAccountApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: PaymentMemberResponse
}

// /api/v1/payments/accounts/logs 응답 DTO (PaymentAccountLedgerPageResponse)
interface PaymentAccountLedgerItem {
  isDeposit: boolean
  content: string
  amount: number
  balance: number
  createdAt: string
}

// 백엔드 PageInfo (ApiResponse.pageInfo)
interface ApiPageInfo {
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
  pageInfo?: ApiPageInfo
  result: T[] // Page<?> 일 때 ApiResponse.result 는 content 배열만 내려옴
}

const PAGE_SIZE = 10

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

export default function MoneyHistoryPage() {
  // 기본 조회 기간: 최근 1주일
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
  const [currentPage, setCurrentPage] = useState(1)
  const [balance, setBalance] = useState<number | null>(null)
  const [balanceLoading, setBalanceLoading] = useState(true)
  const [balanceError, setBalanceError] = useState<string | null>(null)
  const [history, setHistory] = useState<PaymentAccountLedgerItem[]>([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState<string | null>(null)
  const [totalPages, setTotalPages] = useState(1)

  useEffect(() => {
    const fetchBalance = async () => {
      try {
        const response = await api.get<PaymentAccountApiResponse>('/api/v1/payments/members')
        const data = response.data
        if (data.isSuccess && data.result != null) {
          setBalance(Number(data.result.balance))
          setBalanceError(null)
        } else {
          setBalanceError(data.message || '잔액을 불러오지 못했습니다.')
        }
      } catch {
        setBalanceError('잔액을 불러오지 못했습니다.')
      } finally {
        setBalanceLoading(false)
      }
    }
    fetchBalance()
  }, [])

  const fetchHistory = async (page: number, from: string, to: string) => {
    setHistoryLoading(true)
    setHistoryError(null)

    const params = new URLSearchParams()
    params.set('page', String(page))
    params.set('size', String(PAGE_SIZE))
    if (from) params.set('from', `${from}T00:00:00`)
    if (to) params.set('to', `${to}T23:59:59`)

    try {
      const res = await api.get<ApiResponsePage<PaymentAccountLedgerItem>>(
        `/api/v1/payments/accounts/logs?${params.toString()}`
      )
      const data = res.data

      if (!data.isSuccess || !Array.isArray(data.result)) {
        setHistoryError(data.message || '사용 내역을 불러오지 못했습니다.')
        setHistory([])
        setTotalPages(1)
        return
      }

      setHistory(data.result || [])
      const totalPagesFromServer = data.pageInfo?.totalPages ?? 1
      setTotalPages(Math.max(1, totalPagesFromServer))
    } catch {
      setHistoryError('사용 내역을 불러오지 못했습니다.')
      setHistory([])
      setTotalPages(1)
    } finally {
      setHistoryLoading(false)
    }
  }

  // 최초 로딩 시 기본 기간으로 조회
  useEffect(() => {
    fetchHistory(0, startDate, endDate)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const balanceDisplay =
    balanceLoading && balance === null
      ? '조회 중...'
      : balanceError
        ? balanceError
        : balance != null
          ? `${new Intl.NumberFormat('ko-KR').format(balance)}원`
          : '-'

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
      const from = start.toISOString().slice(0, 10)
      const to = end.toISOString().slice(0, 10)
      setStartDate(from)
      setEndDate(to)
      fetchHistory(0, from, to)
    }
  }

  const handleSearch = () => {
    setCurrentPage(1)
    fetchHistory(0, startDate, endDate)
  }

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    // backend page 인덱스는 0부터 시작
    fetchHistory(page - 1, startDate, endDate)
  }

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>뭐든사 머니 사용 내역</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          충전·사용 내역을 기간별로 확인할 수 있어요.
        </p>

        {/* 보유 머니 */}
        <div
          style={{
            background: 'linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%)',
            borderRadius: '12px',
            padding: '16px 20px',
            marginBottom: '24px',
            border: '1px solid #e8ecff',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <span style={{ fontSize: '14px', color: '#666', fontWeight: 500 }}>현재 보유 머니</span>
          <span
            style={{
              fontSize: '22px',
              fontWeight: 700,
              color: balanceError ? '#dc3545' : '#333',
            }}
          >
            {balanceDisplay}
          </span>
        </div>

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
                    사용일시
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    구분
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    내용
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    금액
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    잔액
                  </th>
                </tr>
              </thead>
              <tbody>
                {historyLoading && (
                  <tr>
                    <td colSpan={5} style={{ padding: '24px', textAlign: 'center', color: '#999' }}>
                      조회 중입니다...
                    </td>
                  </tr>
                )}
                {!historyLoading && historyError && (
                  <tr>
                    <td colSpan={5} style={{ padding: '24px', textAlign: 'center', color: '#dc3545' }}>
                      {historyError}
                    </td>
                  </tr>
                )}
                {!historyLoading && !historyError && history.length === 0 && (
                  <tr>
                    <td colSpan={5} style={{ padding: '24px', textAlign: 'center', color: '#999' }}>
                      해당 기간 사용 내역이 없습니다.
                    </td>
                  </tr>
                )}
                {!historyLoading &&
                  !historyError &&
                  history.map((item, idx) => {
                    const dateObj = new Date(item.createdAt)
                    const dateDisplay = dateObj.toLocaleString('ko-KR', {
                      year: 'numeric',
                      month: '2-digit',
                      day: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                    })

                    const typeLabel = item.isDeposit ? '충전' : '사용'
                    const typeStyle = item.isDeposit
                      ? { color: '#22c55e', fontWeight: 600 }
                      : { color: '#666', fontWeight: 600 }

                    const amountNumber = Number(item.amount || 0)
                    const amountFormatted =
                      (item.isDeposit ? '+' : '-') +
                      new Intl.NumberFormat('ko-KR').format(Math.abs(amountNumber))
                    const amountStyle = item.isDeposit
                      ? { color: '#22c55e', fontWeight: 600 }
                      : { color: '#333', fontWeight: 600 }

                    const balanceFormatted = new Intl.NumberFormat('ko-KR').format(
                      Number(item.balance || 0),
                    )

                    return (
                      <tr key={`${item.createdAt}-${idx}`} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '14px 12px', color: '#666' }}>{dateDisplay}</td>
                        <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                          <span style={typeStyle}>{typeLabel}</span>
                        </td>
                        <td style={{ padding: '14px 12px', color: '#333' }}>{item.content}</td>
                        <td style={{ padding: '14px 12px', textAlign: 'right' }}>
                          <span style={amountStyle}>{amountFormatted}</span>
                        </td>
                        <td
                          style={{
                            padding: '14px 12px',
                            textAlign: 'right',
                            fontWeight: 600,
                            color: '#333',
                          }}
                        >
                          {`${balanceFormatted}원`}
                        </td>
                      </tr>
                    )
                  })}
              </tbody>
            </table>
          </div>

          {history.length > 0 && totalPages > 0 && (
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
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                <button
                  key={page}
                  type="button"
                  onClick={() => handlePageChange(page)}
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
                onClick={() => handlePageChange(Math.min(totalPages, currentPage + 1))}
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
