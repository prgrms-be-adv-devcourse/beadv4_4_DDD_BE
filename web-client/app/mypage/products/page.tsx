'use client'

import Link from 'next/link'
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

const STATUS_OPTIONS = [
  { value: '', label: '전체 상태' },
  { value: 'ON_SALE', label: '판매중' },
  { value: 'SOLD_OUT', label: '품절' },
  { value: 'HIDDEN', label: '숨김' },
]

const mockProducts = [
  {
    id: 1,
    name: '데일리 베이직 티셔츠',
    category: '상의',
    categoryValue: 'UPPER',
    price: '29,000원',
    stock: 50,
    status: '판매중',
    statusValue: 'ON_SALE',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
    registeredAt: '2024.01.15',
  },
  {
    id: 2,
    name: '루즈핏 오버코트',
    category: '아우터',
    categoryValue: 'OUTER',
    price: '89,000원',
    stock: 12,
    status: '판매중',
    statusValue: 'ON_SALE',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
    registeredAt: '2024.01.12',
  },
  {
    id: 3,
    name: '니트 풀오버 세트',
    category: '상의',
    categoryValue: 'UPPER',
    price: '44,000원',
    stock: 0,
    status: '품절',
    statusValue: 'SOLD_OUT',
    statusStyle: { color: '#f59e0b', fontWeight: 600 },
    registeredAt: '2024.01.08',
  },
  {
    id: 4,
    name: '미니 크로스백',
    category: '가방',
    categoryValue: 'BAG',
    price: '35,000원',
    stock: 8,
    status: '판매중',
    statusValue: 'ON_SALE',
    statusStyle: { color: '#22c55e', fontWeight: 600 },
    registeredAt: '2024.01.05',
  },
]

const PAGE_SIZE = 10

export default function ProductsPage() {
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState('')
  const [currentPage, setCurrentPage] = useState(1)

  const handleSearch = () => {
    setCurrentPage(1)
    alert(`상품 검색: ${keyword || '(전체)'} / ${category || '전체'} / ${status || '전체'}\n(데모 화면입니다.)`)
  }

  const filteredProducts = mockProducts.filter((p) => {
    const matchKeyword = !keyword || p.name.toLowerCase().includes(keyword.toLowerCase())
    const matchCategory = !category || p.categoryValue === category
    const matchStatus = !status || p.statusValue === status
    return matchKeyword && matchCategory && matchStatus
  })

  const totalPages = Math.max(1, Math.ceil(filteredProducts.length / PAGE_SIZE))
  const paginatedProducts = filteredProducts.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  )

  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>상품 관리</h1>
            <p style={{ color: '#666', fontSize: '14px' }}>
              등록한 상품을 검색하고 관리할 수 있어요.
            </p>
          </div>
          <Link
            href="/products/create"
            style={{
              display: 'inline-block',
              padding: '10px 20px',
              borderRadius: '8px',
              border: 'none',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              fontSize: '14px',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            상품 등록
          </Link>
        </div>

        {/* 상품 검색 */}
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
            상품 검색
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
            <select
              value={status}
              onChange={(e) => {
                setStatus(e.target.value)
                setCurrentPage(1)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                background: 'white',
                minWidth: '100px',
              }}
            >
              {STATUS_OPTIONS.map((opt) => (
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
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    상품명
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    카테고리
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    판매가
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    재고
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    상태
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    등록일
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    관리
                  </th>
                </tr>
              </thead>
              <tbody>
                {paginatedProducts.map((product) => (
                  <tr key={product.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '14px 12px', color: '#333', fontWeight: 500 }}>{product.name}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#666' }}>
                      {product.category}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                      {product.price}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#333' }}>
                      {product.stock}개
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <span style={product.statusStyle}>{product.status}</span>
                    </td>
                    <td style={{ padding: '14px 12px', color: '#666' }}>{product.registeredAt}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <Link
                        href={`/products/${product.id}`}
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

          {filteredProducts.length > 0 && totalPages > 0 && (
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

          {filteredProducts.length === 0 && (
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
