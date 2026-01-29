'use client'

import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

const mockSettlements = [
  {
    id: 'STL-001',
    date: '2024-01-15',
    depositDate: '2024.01.15',
    depositAccount: '국민 123-456-789012',
    salesAmountDisplay: '450,000원',
    feeDisplay: '22,500원',
    depositAmount: 427500,
    depositAmountDisplay: '427,500원',
    status: '입금완료',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
  },
  {
    id: 'STL-002',
    date: '2023-12-31',
    depositDate: '2023.12.31',
    depositAccount: '국민 123-456-789012',
    salesAmountDisplay: '320,000원',
    feeDisplay: '16,000원',
    depositAmount: 304000,
    depositAmountDisplay: '304,000원',
    status: '입금완료',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
  },
  {
    id: 'STL-003',
    date: '2023-12-15',
    depositDate: '2023.12.15',
    depositAccount: '국민 123-456-789012',
    salesAmountDisplay: '180,000원',
    feeDisplay: '9,000원',
    depositAmount: 171000,
    depositAmountDisplay: '171,000원',
    status: '입금완료',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
  },
  {
    id: 'STL-004',
    date: '2024-01-31',
    depositDate: '-',
    depositAccount: '국민 123-456-789012',
    salesAmountDisplay: '0원',
    feeDisplay: '0원',
    depositAmount: 0,
    depositAmountDisplay: '0원',
    status: '대기중',
    statusStyle: { color: '#f59e0b', fontWeight: 600 },
  },
]

const PAGE_SIZE = 10

type PresetKey = 'week' | 'month1' | 'month3' | 'month6' | 'direct'

export default function SettlementPage() {
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
    alert(`기간 검색: ${startDate} ~ ${endDate}\n(데모 화면입니다.)`)
  }

  const filteredSettlements = mockSettlements.filter((item) => {
    const d = item.date
    return d >= startDate && d <= endDate
  })

  const totalDepositAmount = filteredSettlements.reduce((sum, item) => sum + item.depositAmount, 0)
  const totalDepositDisplay = totalDepositAmount.toLocaleString('ko-KR') + '원'

  const totalPages = Math.max(1, Math.ceil(filteredSettlements.length / PAGE_SIZE))
  const paginatedSettlements = filteredSettlements.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  )

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>정산 내역</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          판매 대금 정산 내역을 기간별로 확인할 수 있어요.
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
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '16px' }}>
            <div style={{ fontSize: '14px', fontWeight: 600, color: '#333' }}>
              조회 기간
            </div>
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
              <span style={{ fontSize: '14px', color: '#666', fontWeight: 500 }}>총 입금 금액</span>
              <span style={{ fontSize: '18px', fontWeight: 700, color: '#333' }}>{totalDepositDisplay}</span>
            </div>
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
                    입금일
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    입금계좌
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    판매금액
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    수수료
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    입금금액
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    상태
                  </th>
                </tr>
              </thead>
              <tbody>
                {paginatedSettlements.map((item) => (
                  <tr key={item.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '14px 12px', color: '#333' }}>{item.depositDate}</td>
                    <td style={{ padding: '14px 12px', color: '#666', fontSize: '13px' }}>
                      {item.depositAccount}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 500, color: '#333' }}>
                      {item.salesAmountDisplay}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', color: '#666' }}>
                      {item.feeDisplay}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                      {item.depositAmountDisplay}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <span style={item.statusStyle}>{item.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredSettlements.length > 0 && totalPages > 0 && (
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

          {filteredSettlements.length === 0 && (
            <div
              style={{
                padding: '48px 24px',
                textAlign: 'center',
                color: '#999',
                fontSize: '14px',
              }}
            >
              해당 기간 정산 내역이 없습니다.
            </div>
          )}
        </div>
      </div>
    </MypageLayout>
  )
}
