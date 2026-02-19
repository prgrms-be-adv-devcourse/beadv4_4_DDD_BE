'use client'

import { useEffect, useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from '@/app/lib/axios'

interface SettlementItemResponseDto {
  id: number
  orderItemId: number
  sellerMemberId: number
  totalSalesAmount: number | string
  feeAmount: number | string
  amount: number | string
  purchaseConfirmedAt: string
}

interface SettlementResponseDto {
  id: number
  totalSalesAmount: number | string
  feeAmount: number | string
  amount: number | string
  payoutAt: string | null
  items: SettlementItemResponseDto[]
}

interface ApiResponse<T> {
  isSuccess: boolean
  code: string
  message: string
  result: T
}

const MONTHS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
const toAmount = (value: number | string | null | undefined): number => Number(value ?? 0)
const formatWon = (value: number | string): string =>
  `${toAmount(value).toLocaleString('ko-KR')}원`

export default function SettlementPage() {
  const now = new Date()
  const [year, setYear] = useState(now.getFullYear())
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [settlement, setSettlement] = useState<SettlementResponseDto | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchSettlement = async (y: number, m: number) => {
    setLoading(true)
    setError(null)
    try {
      const response = await api.get<ApiResponse<SettlementResponseDto>>(
        `/api/v1/settlements/${y}/${m}`
      )
      const data = response.data
      if (data.isSuccess && data.result != null) {
        setSettlement(data.result)
      } else {
        setSettlement(null)
        setError(data.message || '정산 내역을 불러올 수 없습니다.')
      }
    } catch (err: unknown) {
      const status = err && typeof err === 'object' && 'response' in err
        ? (err as { response?: { status?: number } }).response?.status
        : undefined
      if (status === 404) {
        setSettlement(null)
        setError(null)
      } else {
        setSettlement(null)
        setError('정산 내역을 불러오는 중 오류가 발생했습니다.')
      }
    } finally {
      setLoading(false)
    }
  }

  const handleSearch = () => {
    fetchSettlement(year, month)
  }

  useEffect(() => {
    fetchSettlement(year, month)
  }, [])

  const years = Array.from({ length: 5 }, (_, i) => now.getFullYear() - i)
  const depositDateDisplay =
    settlement?.payoutAt != null
      ? new Date(settlement.payoutAt).toLocaleDateString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
        }).replace(/\. /g, '.').replace(/\.$/, '')
      : '-'
  const status = settlement?.payoutAt != null ? '입금완료' : '대기중'
  const statusStyle =
    status === '입금완료'
      ? { color: '#22c55e', fontWeight: 600 }
      : { color: '#f59e0b', fontWeight: 600 }
  const settlementItems = settlement?.items ?? []
  const maxSalesAmount = Math.max(
    ...settlementItems.map((item) => toAmount(item.totalSalesAmount)),
    1
  )

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>정산 내역</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          판매 대금 정산 내역을 년·월 기준으로 확인할 수 있어요.
        </p>

        {/* 년/월 선택 */}
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
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '16px',
              marginBottom: '16px',
            }}
          >
            <div style={{ fontSize: '14px', fontWeight: 600, color: '#333' }}>조회 월</div>
            {settlement != null && (
              <div
                style={{
                  background: 'linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%)',
                  borderRadius: '8px',
                  padding: '10px 16px',
                  border: '1px solid #e8ecff',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <span style={{ fontSize: '14px', color: '#666', fontWeight: 500 }}>
                  총 입금 금액
                </span>
                <span style={{ fontSize: '18px', fontWeight: 700, color: '#333' }}>
                  {settlement.amount.toLocaleString('ko-KR')}원
                </span>
              </div>
            )}
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
            <select
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                minWidth: '100px',
              }}
            >
              {years.map((y) => (
                <option key={y} value={y}>
                  {y}년
                </option>
              ))}
            </select>
            <select
              value={month}
              onChange={(e) => setMonth(Number(e.target.value))}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                minWidth: '100px',
              }}
            >
              {MONTHS.map((m) => (
                <option key={m} value={m}>
                  {m}월
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={handleSearch}
              disabled={loading}
              style={{
                padding: '8px 20px',
                borderRadius: '8px',
                border: 'none',
                background:
                  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                fontSize: '14px',
                fontWeight: 600,
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1,
              }}
            >
              {loading ? '조회 중...' : '조회'}
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
                    입금일
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'right',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    판매금액
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'right',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    수수료
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'right',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    입금금액
                  </th>
                  <th
                    style={{
                      padding: '14px 12px',
                      textAlign: 'center',
                      fontWeight: 600,
                      color: '#333',
                    }}
                  >
                    상태
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{
                        padding: '48px 24px',
                        textAlign: 'center',
                        color: '#999',
                        fontSize: '14px',
                      }}
                    >
                      조회 중입니다...
                    </td>
                  </tr>
                )}
                {!loading && error != null && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{
                        padding: '48px 24px',
                        textAlign: 'center',
                        color: '#999',
                        fontSize: '14px',
                      }}
                    >
                      {error}
                    </td>
                  </tr>
                )}
                {!loading && error == null && settlement == null && (
                  <tr>
                    <td
                      colSpan={5}
                      style={{
                        padding: '48px 24px',
                        textAlign: 'center',
                        color: '#999',
                        fontSize: '14px',
                      }}
                    >
                      해당 월 정산 내역이 없습니다.
                    </td>
                  </tr>
                )}
                {!loading && settlement != null && (
                  <tr style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '14px 12px', color: '#333' }}>
                      {depositDateDisplay}
                    </td>
                    <td
                      style={{
                        padding: '14px 12px',
                        textAlign: 'right',
                        fontWeight: 500,
                        color: '#333',
                      }}
                    >
                      {formatWon(settlement.totalSalesAmount)}
                    </td>
                    <td
                      style={{
                        padding: '14px 12px',
                        textAlign: 'right',
                        color: '#666',
                      }}
                    >
                      {formatWon(settlement.feeAmount)}
                    </td>
                    <td
                      style={{
                        padding: '14px 12px',
                        textAlign: 'right',
                        fontWeight: 600,
                        color: '#333',
                      }}
                    >
                      {formatWon(settlement.amount)}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <span style={statusStyle}>{status}</span>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {!loading && settlement != null && (
          <div
            style={{
              marginTop: '24px',
              background: 'white',
              borderRadius: '12px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
              border: '1px solid #f0f0f0',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                padding: '18px 20px',
                borderBottom: '1px solid #f0f0f0',
                background: '#fafbff',
              }}
            >
              <h2 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: '#333' }}>
                정산 아이템 상세
              </h2>
              <p style={{ margin: '6px 0 0', fontSize: '13px', color: '#666' }}>
                아이템별 판매금액/수수료/정산금 내역
              </p>
            </div>

            <div style={{ padding: '16px 20px', borderBottom: '1px solid #f5f5f5' }}>
              {settlementItems.length === 0 ? (
                <div style={{ color: '#999', fontSize: '14px', padding: '8px 0' }}>
                  정산 아이템이 없습니다.
                </div>
              ) : (
                settlementItems.map((item) => {
                  const salesAmount = toAmount(item.totalSalesAmount)
                  const ratio = Math.max(8, Math.round((salesAmount / maxSalesAmount) * 100))

                  return (
                    <div key={item.id} style={{ marginBottom: '10px' }}>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          fontSize: '12px',
                          marginBottom: '4px',
                          color: '#555',
                        }}
                      >
                        <span>주문상품 #{item.orderItemId}</span>
                        <span>{formatWon(item.totalSalesAmount)}</span>
                      </div>
                      <div
                        style={{
                          width: '100%',
                          height: '8px',
                          background: '#eef1ff',
                          borderRadius: '999px',
                        }}
                      >
                        <div
                          style={{
                            width: `${ratio}%`,
                            height: '8px',
                            background: 'linear-gradient(90deg, #667eea 0%, #8b5cf6 100%)',
                            borderRadius: '999px',
                          }}
                        />
                      </div>
                    </div>
                  )
                })
              )}
            </div>

            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                <thead>
                  <tr style={{ background: '#fcfcfd', borderBottom: '1px solid #eee' }}>
                    <th style={{ padding: '12px', textAlign: 'left', color: '#444' }}>주문상품ID</th>
                    <th style={{ padding: '12px', textAlign: 'right', color: '#444' }}>판매금액</th>
                    <th style={{ padding: '12px', textAlign: 'right', color: '#444' }}>수수료</th>
                    <th style={{ padding: '12px', textAlign: 'right', color: '#444' }}>정산금</th>
                    <th style={{ padding: '12px', textAlign: 'center', color: '#444' }}>구매확정일</th>
                  </tr>
                </thead>
                <tbody>
                  {settlementItems.length === 0 ? (
                    <tr>
                      <td
                        colSpan={5}
                        style={{ padding: '20px 12px', textAlign: 'center', color: '#999' }}
                      >
                        표시할 상세 항목이 없습니다.
                      </td>
                    </tr>
                  ) : (
                    settlementItems.map((item) => (
                      <tr key={`row-${item.id}`} style={{ borderBottom: '1px solid #f5f5f5' }}>
                        <td style={{ padding: '12px', color: '#333' }}>#{item.orderItemId}</td>
                        <td style={{ padding: '12px', textAlign: 'right', color: '#333' }}>
                          {formatWon(item.totalSalesAmount)}
                        </td>
                        <td style={{ padding: '12px', textAlign: 'right', color: '#666' }}>
                          {formatWon(item.feeAmount)}
                        </td>
                        <td style={{ padding: '12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                          {formatWon(item.amount)}
                        </td>
                        <td style={{ padding: '12px', textAlign: 'center', color: '#666' }}>
                          {new Date(item.purchaseConfirmedAt)
                            .toLocaleDateString('ko-KR', {
                              year: 'numeric',
                              month: '2-digit',
                              day: '2-digit',
                            })
                            .replace(/\. /g, '.')
                            .replace(/\.$/, '')}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </MypageLayout>
  )
}
