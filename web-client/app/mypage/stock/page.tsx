'use client'

import {useEffect, useState} from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";

// --- DTO 인터페이스 정의 ---
interface ProductDto {
  id: number
  sellerId: number
  sellerBusinessName: string
  name: string
  category: string
  description: string
  price: number
  salePrice: number
  currency: string
  productStatus: string
  saleStatus: string
  favoriteCount: number
  primaryImageUrl: string | null
  createdAt: string
  updatedAt: string
  createdBy: string | null
  updatedBy: string | null
}

interface PaginationDto {
  page: number
  size: number
  hasNext: boolean
  totalElements: number
  totalPages: number
}

interface ProductListResponse {
  isSuccess: boolean
  code: string
  message: string
  pagination: PaginationDto
  result: ProductDto[]
}

interface InventoryDto {
  productId: number
  sellerId: number
  quantity: number
  initialized: boolean
}

interface InventoryListResponse {
  isSuccess: boolean
  code: string
  message: string
  result: {
    productIds: InventoryDto[]
  }
}

interface MergedStockItem {
  productId: number
  name: string
  category: string
  quantity: number
  initialized: boolean
}

const PAGE_SIZE = 10

function getQuantityEdit(edits: Record<number, string>, productId: number, currentQuantity: number): string {
  return edits[productId] ?? String(currentQuantity)
}

export default function StockPage() {
  // --- 상태 관리 ---
  const [stockList, setStockList] = useState<MergedStockItem[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const [searchProductId, setSearchProductId] = useState('')
  const [searchProductName, setSearchProductName] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [stockEdits, setStockEdits] = useState<Record<number, string>>({})

  // --- 데이터 병합 조회 로직 ---
  const fetchStockData = async () => {
    setIsLoading(true)
    try {
      // 1. 판매자의 상품 목록 조회 (전체 조회를 위해 size를 넉넉하게 설정)
      const productsRes = await api.get<ProductListResponse>('/api/v1/products/sellers', {
        params: {
          page: 0,
          size: 100
        }
      })

      if (productsRes.data.isSuccess) {
        const products = productsRes.data.result

        if (!products || products.length === 0) {
          setStockList([])
          return
        }

        const productIds = products.map(p => p.id)

        // 2. 해당 상품들의 재고 목록 조회
        const inventoryRes = await api.post<InventoryListResponse>('/api/v2/inventories', { productIds })

        if (inventoryRes.data.isSuccess) {
          // 💡 핵심: result.productIds 로 꺼내야 진짜 배열이 나옵니다!
          const inventories = inventoryRes.data.result.productIds

          // 3. 상품 정보 + 재고 정보 병합
          const mergedList: MergedStockItem[] = products.map(product => {
            // 이제 inventories가 정상적인 배열이므로 .find()가 완벽하게 동작합니다!
            const inv = inventories.find(i => i.productId === product.id)
            return {
              productId: product.id,
              name: product.name,
              category: product.category,
              quantity: inv ? inv.quantity : 0,
              initialized: inv ? inv.initialized : false
            }
          })

          setStockList(mergedList)
        }
      }
    } catch (error) {
      console.error('재고 데이터 조회 실패:', error)
      alert('데이터를 불러오는데 실패했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchStockData()
  }, [])

  // --- 이벤트 핸들러 ---
  const handleSearch = () => {
    setCurrentPage(1)
  }

  const handleQuantityChange = (productId: number, value: string) => {
    setStockEdits((prev) => ({ ...prev, [productId]: value }))
  }

  const handleRegister = async (item: MergedStockItem) => {
    const inputStock = getQuantityEdit(stockEdits, item.productId, item.quantity)

    if (!inputStock || Number(inputStock) < 0) {
      alert('올바른 수량을 입력해주세요.')
      return
    }

    try {
      const res = await api.post(`/api/v2/inventories/${item.productId}`, {
        quantity: Number(inputStock)
      })

      if (res.data.isSuccess) {
        alert('재고가 성공적으로 등록되었습니다.')
        setStockEdits((prev) => {
          const newState = { ...prev }
          delete newState[item.productId]
          return newState
        })
        await fetchStockData()
      } else {
        alert(res.data.message || '재고 등록에 실패했습니다.')
      }
    } catch (error) {
      console.error('재고 등록 에러:', error)
      alert('재고 등록 중 오류가 발생했습니다.')
    }
  }

  // --- 필터링 및 페이지네이션 ---
  const filteredList = stockList.filter((p) => {
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

          {/* 검색 컨테이너 */}
          <div style={{ background: 'white', borderRadius: '12px', padding: '20px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)', border: '1px solid #f0f0f0', marginBottom: '24px' }}>
            <div style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px', color: '#333' }}>재고 검색</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
              <input type="text" placeholder="상품 ID 검색" value={searchProductId} onChange={(e) => { setSearchProductId(e.target.value); setCurrentPage(1); }} style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px', width: '180px', minWidth: '120px' }} />
              <input type="text" placeholder="상품명 검색" value={searchProductName} onChange={(e) => { setSearchProductName(e.target.value); setCurrentPage(1); }} style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #e0e0e0', fontSize: '14px', width: '180px', minWidth: '120px' }} />
              <button type="button" onClick={handleSearch} style={{ padding: '8px 20px', borderRadius: '8px', border: 'none', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}>검색</button>
            </div>
          </div>

          {/* 테이블 컨테이너 */}
          <div style={{ background: 'white', borderRadius: '12px', boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)', border: '1px solid #f0f0f0', overflow: 'hidden' }}>

            {isLoading ? (
                <div style={{ padding: '60px 0', textAlign: 'center', color: '#666' }}>
                  데이터를 불러오는 중입니다...
                </div>
            ) : (
                <>
                  <div style={{ overflowX: 'auto' }}>
                    <table style={{width: '100%', borderCollapse: 'collapse', fontSize: '14px'}}>
                      <thead>
                      <tr style={{background: '#f8f9fa', borderBottom: '2px solid #eee'}}>
                        <th style={{
                          padding: '14px 12px',
                          textAlign: 'center',
                          fontWeight: 600,
                          color: '#333',
                          width: '15%'
                        }}>카테고리
                        </th>
                        <th style={{
                          padding: '14px 12px',
                          textAlign: 'center',
                          fontWeight: 600,
                          color: '#333',
                          width: '15%'
                        }}>상품아이디
                        </th>
                        <th style={{
                          padding: '14px 12px',
                          textAlign: 'left',
                          fontWeight: 600,
                          color: '#333',
                          width: '30%'
                        }}>상품명
                        </th>
                        <th style={{
                          padding: '14px 12px',
                          textAlign: 'center',
                          fontWeight: 600,
                          color: '#333',
                          width: '20%'
                        }}>재고
                        </th>
                        <th style={{
                          padding: '14px 12px',
                          textAlign: 'center',
                          fontWeight: 600,
                          color: '#333',
                          width: '20%'
                        }}>관리
                        </th>
                      </tr>
                      </thead>
                      <tbody>
                      {paginatedList.map((item) => {
                        const quantityValue = getQuantityEdit(stockEdits, item.productId, item.quantity)
                        return (
                            <tr key={item.productId} style={{borderBottom: '1px solid #f0f0f0'}}>
                              <td style={{
                                padding: '14px 12px',
                                textAlign: 'center',
                                color: '#666'
                              }}>{item.category}</td>
                              <td style={{
                                padding: '14px 12px',
                                textAlign: 'center',
                                color: '#666',
                                fontSize: '13px'
                              }}>{item.productId}</td>
                              <td style={{
                                padding: '14px 12px',
                                color: '#333',
                                fontWeight: 500
                              }}>{item.name}</td>

                              {/* 🎯 1. 수량 영역: isInitialized가 true면 단순 텍스트, false면 입력창 */}
                              <td style={{padding: '8px 12px', textAlign: 'center'}}>
                                {item.initialized ? (
                                    <span style={{fontWeight: 600, color: '#4CAF50'}}>
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
                                          textAlign: 'center'
                                        }}
                                    />
                                )}
                              </td>

                              {/* 🎯 2. 버튼 영역: isInitialized가 true면 '등록 완료' 텍스트, false면 '등록' 버튼 */}
                              <td style={{padding: '14px 12px', textAlign: 'center'}}>
                                {item.initialized ? (
                                    <span
                                        style={{fontSize: '13px', color: '#999', fontWeight: 500}}>
                                등록 완료
                              </span>
                                ) : (
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
                                          cursor: 'pointer'
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

                  {/* 페이지네이션 */}
                  {filteredList.length > 0 && totalPages > 0 && (
                      <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        gap: '4px',
                        padding: '16px',
                        borderTop: '1px solid #f0f0f0'
                      }}>
                        <button type="button"
                                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                                disabled={currentPage === 1} style={{
                          padding: '8px 12px',
                          borderRadius: '8px',
                          border: '1px solid #e0e0e0',
                          background: currentPage === 1 ? '#f5f5f5' : '#fff',
                          color: currentPage === 1 ? '#999' : '#333',
                          fontSize: '14px',
                          cursor: currentPage === 1 ? 'not-allowed' : 'pointer'
                        }}>이전
                        </button>
                        {Array.from({length: totalPages}, (_, i) => i + 1).map((page) => (
                            <button key={page} type="button" onClick={() => setCurrentPage(page)}
                                    style={{
                                      minWidth: '36px',
                                      padding: '8px',
                                      borderRadius: '8px',
                                      border: currentPage === page ? '2px solid #667eea' : '1px solid #e0e0e0',
                                      background: currentPage === page ? '#f8f8ff' : '#fff',
                                      color: currentPage === page ? '#667eea' : '#333',
                                      fontSize: '14px',
                                      fontWeight: currentPage === page ? 600 : 400,
                                      cursor: 'pointer'
                                    }}>{page}</button>
                        ))}
                        <button type="button"
                                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                                disabled={currentPage === totalPages} style={{
                          padding: '8px 12px',
                          borderRadius: '8px',
                          border: '1px solid #e0e0e0',
                          background: currentPage === totalPages ? '#f5f5f5' : '#fff',
                          color: currentPage === totalPages ? '#999' : '#333',
                          fontSize: '14px',
                          cursor: currentPage === totalPages ? 'not-allowed' : 'pointer'
                        }}>다음
                        </button>
                      </div>
                  )}

                  {filteredList.length === 0 && (
                      <div style={{
                        padding: '48px 24px',
                        textAlign: 'center',
                        color: '#999',
                        fontSize: '14px'
                      }}>
                        검색 조건에 맞는 상품이 없습니다.
                      </div>
                  )}
                </>
            )}
          </div>
        </div>
      </MypageLayout>
  )
}