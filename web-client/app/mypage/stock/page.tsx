'use client'
'use client'

import { useState } from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";


interface InventoryDto {
  productId: number
  sellerId: number
  quantity: number
  isInitialized: boolean
}
// DTO 구조에 맞춘 Mock 데이터
// 실제 API 연동 시 이 부분을 삭제하고 받아온 데이터를 사용하세요.
const mockStockList = [
  {
    productId: 1,
    name: '데일리 베이직 티셔츠',
    category: '상의',
    quantity: 50,
    isInitialized: true, // 초기화 됨 (실재고 노출)
  },
  {
    productId: 2,
    name: '루즈핏 오버코트',
    category: '아우터',
    quantity: 0,
    isInitialized: false, // 초기화 안 됨 (입력창 + 등록 버튼 노출)
  },
  {
    productId: 3,
    name: '니트 풀오버 세트',
    category: '상의',
    quantity: 0,
    isInitialized: false,
  },
  {
    productId: 4,
    name: '미니 크로스백',
    category: '가방',
    quantity: 8,
    isInitialized: true,
  },
]

const PAGE_SIZE = 10

// 입력한 재고값을 관리하기 위한 헬퍼 함수
function getQuantityEdit(edits: Record<number, string>, productId: number, currentQuantity: number): string {
  return edits[productId] ?? String(currentQuantity)
}

export default function StockPage() {
  const [searchProductId, setSearchProductId] = useState('')
  const [searchProductName, setSearchProductName] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [stockEdits, setStockEdits] = useState<Record<number, string>>({})

  const handleSearch = () => {
    setCurrentPage(1)
  }

  // 재고 입력값 변경
  const handleQuantityChange = (productId: number, value: string) => {
    setStockEdits((prev) => ({ ...prev, [productId]: value }))
  }

  // 등록 버튼 (isInitialized: false 일 때)
  const handleRegister = async (item: typeof mockStockList[0]) => {
    const inputStock = getQuantityEdit(stockEdits, item.productId, item.quantity)

    // TODO: 실제 API 연동 시 아래 코드를 사용하세요.
    try {
      await api.post(`/api/v2/inventories/${item.productId}`, { quantity: Number(inputStock) });
      alert('재고가 등록되었습니다.');
    // 데이터 재조회 로직 추가 (fetchInventory 등)
    } catch (error) {
       console.error(error);
    }

    alert(`[API 호출] POST /${item.productId}\n상품명: ${item.name}\n등록할 재고: ${inputStock}개`)
  }

  // 수정 버튼 (isInitialized: true 일 때 - 필요 시 사용)
  const handleEdit = (item: typeof mockStockList[0]) => {
    alert(`수정 기능은 별도로 구현이 필요합니다. (상품 ID: ${item.productId})`)
  }

  // 필터링 로직
  const filteredList = mockStockList.filter((p) => {
    const matchId = !searchProductId.trim() || p.productId.toString().includes(searchProductId.trim())
    const matchName = !searchProductName.trim() || p.name.toLowerCase().includes(searchProductName.trim().toLowerCase())
    return matchId && matchName
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
            등록한 상품의 재고를 확인하고 초기화할 수 있어요.
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
                  placeholder="상품 ID 검색"
                  value={searchProductId}
                  onChange={(e) => {
                    setSearchProductId(e.target.value)
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
              <input
                  type="text"
                  placeholder="상품명 검색"
                  value={searchProductName}
                  onChange={(e) => {
                    setSearchProductName(e.target.value)
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
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333', width: '15%' }}>
                    카테고리
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333', width: '15%' }}>
                    상품아이디
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333', width: '30%' }}>
                    상품명
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333', width: '20%' }}>
                    재고
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333', width: '20%' }}>
                    관리
                  </th>
                </tr>
                </thead>
                <tbody>
                {paginatedList.map((item) => {
                  const quantityValue = getQuantityEdit(stockEdits, item.productId, item.quantity)
                  return (
                      <tr key={item.productId} style={{borderBottom: '1px solid #f0f0f0'}}>
                        <td style={{padding: '14px 12px', textAlign: 'center', color: '#666'}}>
                          {item.category}
                        </td>
                        <td style={{
                          padding: '14px 12px',
                          textAlign: 'center',
                          color: '#666',
                          fontSize: '13px'
                        }}>
                          {item.productId}
                        </td>
                        <td style={{padding: '14px 12px', color: '#333', fontWeight: 500}}>
                          {item.name}
                        </td>
                        <td style={{padding: '8px 12px', textAlign: 'center'}}>
                          {item.isInitialized ? (
                              <span style={{fontWeight: 600, color: '#333'}}>
                            {item.quantity} 개
                          </span>
                          ) : (
                              <input
                                  type="number"
                                  min={0}
                                  value={quantityValue}
                                  onChange={(e) => handleQuantityChange(item.productId, e.target.value)}
                                  placeholder="초기수량"
                                  style={{
                                    width: '80px',
                                    padding: '6px 8px',
                                    borderRadius: '6px',
                                    border: '1px solid #e0e0e0',
                                    fontSize: '14px',
                                    textAlign: 'center',
                                  }}
                              />
                          )}
                        </td>

                        {/* 🎯 관리 컬럼: 수정 버튼 삭제, 초기화 안 된 경우 '등록' 버튼만 렌더링 */}
                        <td style={{padding: '14px 12px', textAlign: 'center'}}>
                          {!item.isInitialized && (
                              <button
                                  type="button"
                                  onClick={() => handleRegister(item)}
                                  style={{
                                    padding: '6px 12px',
                                    borderRadius: '6px',
                                    border: 'none',
                                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                    color: 'white',
                                    fontSize: '13px',
                                    fontWeight: 500,
                                    cursor: 'pointer',
                                  }}
                              >
                                등록
                              </button>
                          )}
                        </td>
                      </tr>
                  )
                })}
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
                  {Array.from({length: totalPages}, (_, i) => i + 1).map((page) => (
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