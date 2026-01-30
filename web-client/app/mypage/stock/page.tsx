'use client'

import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'

const CATEGORY_OPTIONS = [
  { value: '', label: '전체 카테고리' },
  { value: 'OUTER', label: '아우터' },
  { value: 'UPPER', label: '상의' },
  { value: 'LOWER', label: '하의' },
  { value: 'CAP', label: '모자' },
  { value: 'SHOES', label: '신발' },
  { value: 'BAG', label: '가방' },
  { value: 'BEAUTY', label: '뷰티' },
]

const mockStockList = [
  {
    id: 1,
    orderNo: 'ORD-2024-001',
    name: '데일리 베이직 티셔츠',
    category: '상의',
    categoryValue: 'UPPER',
    totalStock: 50,
    reservedStock: 5,
    stockStatus: '충분',
    stockStatusStyle: { color: '#22c55e', fontWeight: 600 },
  },
  {
    id: 2,
    orderNo: 'ORD-2024-002',
    name: '루즈핏 오버코트',
    category: '아우터',
    categoryValue: 'OUTER',
    totalStock: 12,
    reservedStock: 2,
    stockStatus: '주의',
    stockStatusStyle: { color: '#f59e0b', fontWeight: 600 },
  },
  {
    id: 3,
    orderNo: 'ORD-2024-003',
    name: '니트 풀오버 세트',
    category: '상의',
    categoryValue: 'UPPER',
    totalStock: 0,
    reservedStock: 0,
    stockStatus: '품절',
    stockStatusStyle: { color: '#ef4444', fontWeight: 600 },
  },
  {
    id: 4,
    orderNo: 'ORD-2024-004',
    name: '미니 크로스백',
    category: '가방',
    categoryValue: 'BAG',
    totalStock: 8,
    reservedStock: 3,
    stockStatus: '주의',
    stockStatusStyle: { color: '#f59e0b', fontWeight: 600 },
  },
]

const PAGE_SIZE = 10

export default function StockPage() {
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [currentPage, setCurrentPage] = useState(1)

  const handleSearch = () => {
    setCurrentPage(1)
    alert(`재고 검색: ${keyword || '(전체)'} / ${category || '전체'}\n(데모 화면입니다.)`)
  }

  const filteredList = mockStockList.filter((p) => {
    const matchKeyword = !keyword || p.name.toLowerCase().includes(keyword.toLowerCase())
    const matchCategory = !category || p.categoryValue === category
    return matchKeyword && matchCategory
  })

  const totalPages = Math.max(1, Math.ceil(filteredList.length / PAGE_SIZE))
  const paginatedList = filteredList.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  )

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>재고 관리</h1>
        <p style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          등록한 상품의 재고를 확인하고 조정할 수 있어요.
        </p>

        {/* 검색 */}
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
            재고 검색
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => {
                setKeyword(e.target.value)
                setCurrentPage(1)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                width: '180px',
                minWidth: '120px',
              }}
            />
            <select
              value={category}
              onChange={(e) => {
                setCategory(e.target.value)
                setCurrentPage(1)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                background: 'white',
                minWidth: '120px',
              }}
            >
              {CATEGORY_OPTIONS.map((opt) => (
                <option key={opt.value || 'all'} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
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
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    카테고리
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    주문번호
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    상품명
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    총재고
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    예약재고
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    재고 상태
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    재고 조정
                  </th>
                </tr>
              </thead>
              <tbody>
                {paginatedList.map((item) => (
                  <tr key={item.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#666' }}>
                      {item.category}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#666', fontSize: '13px' }}>
                      {item.orderNo}
                    </td>
                    <td style={{ padding: '14px 12px', color: '#333', fontWeight: 500 }}>{item.name}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                      {item.totalStock}개
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#666' }}>
                      {item.reservedStock}개
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <span style={item.stockStatusStyle}>{item.stockStatus}</span>
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <button
                        type="button"
                        onClick={() => alert(`재고 조정: ${item.name}\n(데모 화면입니다.)`)}
                        style={{
                          fontSize: '13px',
                          color: '#667eea',
                          fontWeight: 500,
                          background: 'none',
                          border: 'none',
                          cursor: 'pointer',
                          textDecoration: 'underline',
                        }}
                      >
                        조정하기
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {filteredList.length > 0 && totalPages > 0 && (
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

          {filteredList.length === 0 && (
            <div
              style={{
                padding: '48px 24px',
                textAlign: 'center',
                color: '#999',
                fontSize: '14px',
              }}
            >
              검색 조건에 맞는 상품이 없습니다.
            </div>
          )}
        </div>
      </div>
    </MypageLayout>
  )
}
