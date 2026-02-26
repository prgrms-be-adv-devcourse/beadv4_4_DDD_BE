'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import Header from '../components/Header'
import api from '@/app/lib/axios'

interface CartItemDto {
  id: number         // 장바구니 아이템 고유 ID
  productId: number  // 상품 ID
  name: string
  primaryImageUrl : string
  quantity: number
  salePrice: number
  isAvailable: boolean
}

interface CartResponseResult {
  memberId: number
  totalQuantity: number
  totalAmount: number
  cartItems: CartItemDto[]
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: CartResponseResult
}

// 상품 상세 API 응답 DTO
interface ProductImageDto {
  id: number
  imageUrl: string
  isPrimary: boolean
  sortOrder: number
}

interface ProductDto {
  id: number
  name: string
  price: number
  salePrice: number
  productStatus: string
  saleStatus: string // "SALE", "SOLD_OUT" 등
  images: ProductImageDto[]
}

interface MergedCartItem {
  id: number          // cartItemId
  productId: number
  quantity: number
  // --- ProductDto에서 덮어씌울 최신 데이터 ---
  name: string
  salePrice: number
  primaryImageUrl: string
  saleStatus: string
  soldOut: boolean
}

interface InventoryDto {
  productId: number;
  sellerId: number;
  quantity: number;
  initialized: boolean;
}

export default function CartPage() {
  const router = useRouter()

  // 상태 관리
  // 장바구니 기본 정보 (totalQuantity, totalAmount 등)
  const [cartMeta, setCartMeta] = useState<Omit<CartResponseResult, 'cartItems'> | null>(null)

  const [mergedItems, setMergedItems] = useState<MergedCartItem[]>([])

  const [isLoading, setIsLoading] = useState(true)
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set())

  // --- 데이터 병합 로직 ---
  const fetchCart = async () => {
    try {
      // 장바구니 API 호출
      const res = await api.get<ApiResponse>('/api/v1/orders/cart-items')

      if (res.data.isSuccess) {
        const cartResult = res.data.result
        if (!cartResult?.cartItems) {
          setMergedItems([])
          setIsLoading(false)
          return
        }

        // 메타데이터 저장
        setCartMeta({
          memberId: cartResult.memberId,
          totalQuantity: cartResult.totalQuantity,
          totalAmount: cartResult.totalAmount
        })

        // 상품 API 병합
        const itemsWithProductInfo = await Promise.all(
            cartResult.cartItems.map(async (cartItem): Promise<{
              quantity: number;
              primaryImageUrl: string;
              productId: number;
              salePrice: number;
              name: string;
              id: number;
              saleStatus: string
              soldOut: boolean
            }> => {
              try {
                // 상품 상세 조회 API
                const productRes = await api.get(`/api/v1/products/${cartItem.productId}`)

                if (productRes.data.isSuccess) {
                  const product: ProductDto = productRes.data.result
                  const primaryImg = product.images.find(img => img.isPrimary)

                  const inventoryRes = await api.get<InventoryDto>(`/api/v2/inventories/${cartItem.productId}`)

                  const availableQuantity = inventoryRes.data.quantity

                  // DTO 조합 후 반환
                  return {
                    id: cartItem.id,
                    productId: cartItem.productId,
                    quantity: cartItem.quantity,
                    // Product 최신 데이터로 덮어쓰기
                    name: product.name,
                    salePrice: product.salePrice,
                    primaryImageUrl: primaryImg ? primaryImg.imageUrl : '',
                    saleStatus: product.saleStatus,
                    soldOut : cartItem.quantity > availableQuantity
                  }
                }
              } catch (err) {
                console.error(`상품 상세 조회 실패 (ID: ${cartItem.productId}):`, err)
              }

              // 상품 API 실패 시 기본 데이터
              return {
                id: cartItem.id,
                productId: cartItem.productId,
                quantity: cartItem.quantity,
                name: cartItem.name,             // 장바구니 DB에 있던 과거 이름
                salePrice: cartItem.salePrice,   // 장바구니 DB에 있던 과거 가격
                primaryImageUrl: '',
                saleStatus: 'ERROR',              // 구매 불가 처리
                soldOut : false
              }
            })
        )

        setMergedItems(itemsWithProductInfo)

        // 처음 로딩 시 '판매 중'인 상품만 자동 선택
        if (isLoading) {
          const availableItemIds = new Set(
              itemsWithProductInfo
              .filter(item => item.saleStatus === 'SALE')
              .map(item => item.id)
          )
          setSelectedItems(availableItemIds)
        }
      }
    } catch (error) {
      console.error('장바구니 조회 실패:', error)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchCart()
  }, [])

  // --- 이벤트 핸들러 ---
  const updateQuantity = async (productId: number, currentQty: number, delta: number) => {
    const newQty = currentQty + delta

    // 최소 수량 방어
    if (newQty < 1) return

    try {
      const inventoryRes = await api.get<InventoryDto>(`/api/v2/inventories/${productId}`)

      const availableQuantity = inventoryRes.data.quantity

      // 재고 수량 비교 로직
      if (newQty > availableQuantity) {
        alert(`재고가 부족합니다. (현재 남은 수량: ${availableQuantity}개)`)
        return
      }

      // 기존 장바구니 수량 변경 API 호출
      await api.post(`/api/v1/orders/cart/item`, { productId: productId, quantity: newQty })
      await fetchCart()

    } catch (error) {
      console.error('수량 변경 또는 재고 조회 실패:', error)
      alert('수량을 변경하지 못했습니다.')
    }
  }

  const handleDeleteSelected = async () => {
    if (selectedItems.size === 0) {
      alert('삭제할 상품을 선택해주세요.')
      return
    }

    if (!confirm(`선택한 ${selectedItems.size}개의 상품을 삭제하시겠습니까?`)) return

    try {
      const ids = Array.from(selectedItems)
      await api.delete('/api/v2/orders/cart-items', {
        data: { cartItemIds: ids },
      })
      setSelectedItems(new Set())
      await fetchCart()
    } catch (error) {
      console.error('일괄 삭제 실패:', error)
      alert('선택한 상품을 삭제하지 못했습니다.')
    }
  }

  const handleDeleteAll = async () => {
    if (mergedItems.length === 0) return
    if (!confirm('장바구니를 비우시겠습니까?')) return

    try {
      await api.delete('/api/v2/orders/cart-items/all')
      setSelectedItems(new Set())
      await fetchCart()
    } catch (error) {
      console.error('전체 삭제 실패:', error)
      alert('장바구니 비우기에 실패했습니다.')
    }
  }

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const allIds = new Set(mergedItems.map(item => item.id))
      setSelectedItems(allIds)
    } else {
      setSelectedItems(new Set())
    }
  }

  const handleSelectItem = (id: number, checked: boolean) => {
    const newSelected = new Set(selectedItems)
    if (checked) {
      newSelected.add(id)
    } else {
      newSelected.delete(id)
    }
    setSelectedItems(newSelected)
  }

  // --- 전체 주문하기 로직 ---
  const handleOrder = async () => {
    // saleStatus가 'SALE'인 모든 상품 필터링
    const availableItems = mergedItems.filter(item => item.saleStatus === 'SALE')

    if (availableItems.length === 0) {
      alert('주문 가능한 상품이 없습니다.')
      return
    }

    try {
      // 전체 구매 가능 상품의 cartItemId 배열 추출
      const itemIds = availableItems.map(item => item.id)

      // 장바구니 주문 생성 API 호출
      const res = await api.post('/api/v1/orders/cart-order')

      if (res.data.isSuccess) {
        // 성공 시 생성된 orderId를 받아 주문/결제 페이지로 이동
        const createdOrderId = res.data.result.orderId
        router.push(`/order?orderId=${createdOrderId}`)
      } else {
        alert(res.data.message || '주문 생성에 실패했습니다.')
      }
    } catch (error:any) {
      const errorMessage = error.response?.data?.message || '주문 처리 중 오류가 발생했습니다.'

      alert(errorMessage)
    }
  }

  // --- 계산 로직 (전체 주문 기준) ---
  const allSelected = mergedItems.length > 0 && mergedItems.every(item => selectedItems.has(item.id))
  const someSelected = mergedItems.some(item => selectedItems.has(item.id))

  // 결제 금액은 '판매 중(SALE)'인 전체 상품을 기준으로 계산
  const availableCartItems = mergedItems.filter(item => item.saleStatus === 'SALE')
  const totalPrice = availableCartItems.reduce((sum, item) => sum + (item.salePrice * item.quantity), 0)
  const deliveryFee = 0
  const finalTotal = totalPrice + deliveryFee

  const formatPrice = (price: number) => new Intl.NumberFormat('ko-KR').format(price)

  return (
      <div className="home-page">
        <Header />

        <div className="cart-page-container">
          <div className="container">
            <h1 className="cart-page-title">장바구니</h1>

            {isLoading ? (
                <div style={{ textAlign: 'center', padding: '80px 20px' }}>
                  <p>장바구니 정보를 불러오는 중...</p>
                </div>
            ) : mergedItems.length > 0 ? (
                <>
                  {/* 장바구니 리스트 */}
                  <div className="cart-items-section">
                    <div className="cart-items-header">
                      <div className="select-all">
                        <input
                            type="checkbox"
                            id="select-all"
                            checked={allSelected}
                            onChange={(e) => handleSelectAll(e.target.checked)}
                        />
                        <label htmlFor="select-all">전체 선택</label>
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button className="delete-selected-btn" onClick={handleDeleteAll}>
                          전체 삭제
                        </button>
                        {someSelected && (
                            <button className="delete-selected-btn" onClick={handleDeleteSelected}>
                              선택 삭제 ({selectedItems.size})
                            </button>
                        )}
                      </div>
                    </div>

                    <div className="cart-items-list">
                      {mergedItems.map((item) => {
                        const isDisabled = item.saleStatus !== 'SALE' || item.soldOut;

                        return (
                            <div
                                key={item.id}
                                className="cart-item"
                                style={{
                                  opacity: isDisabled ? 0.5 : 1
                                }}
                            >
                              <div className="cart-item-checkbox">
                                <input
                                    type="checkbox"
                                    id={`item-${item.id}`}
                                    checked={selectedItems.has(item.id)}
                                    onChange={(e) => handleSelectItem(item.id, e.target.checked)}
                                />
                              </div>

                              <div
                                  className="cart-item-image"
                                  style={{ width: '80px', height: '80px', flexShrink: 0 }}
                              >
                                {item.primaryImageUrl ? (
                                    <img
                                        src={item.primaryImageUrl}
                                        alt={item.name}
                                        style={{
                                          width: '100%',
                                          height: '100%',
                                          objectFit: 'cover',
                                          borderRadius: '8px',
                                          filter: isDisabled ? 'grayscale(100%)' : 'none'
                                        }}
                                    />
                                ) : (
                                    <div
                                        style={{
                                          width: '100%',
                                          height: '100%',
                                          background: '#eee',
                                          borderRadius: '8px',
                                          display: 'flex',
                                          alignItems: 'center',
                                          justifyContent: 'center',
                                          fontSize: '12px',
                                          color: '#999'
                                        }}
                                    >
                                      이미지 없음
                                    </div>
                                )}
                              </div>

                              <div className="cart-item-info">
                                <div className="cart-item-name">{item.name}</div>
                                <div className="cart-item-price">₩{formatPrice(item.salePrice)}</div>

                                {item.saleStatus !== 'SALE' && (
                                    <div style={{ fontSize: '13px', color: '#f44336', marginTop: '4px', fontWeight: 'bold' }}>
                                      구매 불가 (판매 종료)
                                    </div>
                                )}

                                {item.saleStatus === 'SALE' && item.soldOut && (
                                    <div style={{ fontSize: '13px', color: '#ff9800', marginTop: '4px', fontWeight: 'bold' }}>
                                      품절 (재고 부족)
                                    </div>
                                )}
                              </div>

                              <div className="cart-item-actions">
                                <div className="quantity-control">
                                  <button
                                      className="quantity-btn"
                                      onClick={() => updateQuantity(item.productId, item.quantity, -1)}
                                      disabled={item.quantity <= 1 || isDisabled}
                                  >
                                    -
                                  </button>

                                  <span className="quantity-value">{item.quantity}</span>

                                  <button
                                      className="quantity-btn"
                                      onClick={() => updateQuantity(item.productId, item.quantity, 1)}
                                      disabled={isDisabled}
                                  >
                                    +
                                  </button>
                                </div>

                                <div className="cart-item-total">
                                  ₩{formatPrice(item.salePrice * item.quantity)}
                                </div>
                              </div>
                            </div>
                        );
                      })}
                    </div>
                  </div>

                  {/* 주문 요약 */}
                  <div className="cart-summary-card">
                    <div className="cart-promo-banner">
                      {/* 프로모션 배너 */}
                    </div>
                    <div className="cart-summary-section">
                      <div className="summary-card">
                        <h2 className="summary-title">주문 요약</h2>
                        <div className="summary-details">
                          <div className="summary-row">
                            <span>상품 금액</span>
                            <span>₩{formatPrice(totalPrice)}</span>
                          </div>
                          <div className="summary-row">
                            <span>배송비</span>
                            <span style={{ color: '#667eea' }}>무료배송</span>
                          </div>
                          <div className="summary-divider"></div>
                          <div className="summary-row total">
                            <span>총 결제 금액</span>
                            <span>₩{formatPrice(finalTotal)}</span>
                          </div>
                        </div>
                        {/* 선택과 무관하게 전체 구매하는 버튼 */}
                        <button onClick={handleOrder} className="order-button">
                          전체 상품 주문하기
                        </button>
                      </div>
                    </div>
                  </div>
                </>
            ) : (
                <div className="empty-cart">
                  <div className="empty-cart-icon">🛒</div>
                  <h2 className="empty-cart-title">장바구니가 비어있습니다</h2>
                  <p className="empty-cart-text">원하는 상품을 담아보세요</p>
                  <Link href="/" className="shopping-button">
                    쇼핑하러 가기
                  </Link>
                </div>
            )}
          </div>
        </div>

        <footer className="footer">
          {/* Footer 내용 */}
        </footer>
      </div>
  )
}