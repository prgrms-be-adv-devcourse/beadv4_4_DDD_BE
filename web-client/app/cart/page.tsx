'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import Header from '../components/Header' // 경로에 맞게 수정해주세요
import api from '@/app/lib/axios'
import {Simulate} from "react-dom/test-utils";
import error = Simulate.error;

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

export default function CartPage() {
  const router = useRouter()

  // 상태 관리
  const [cartData, setCartData] = useState<CartResponseResult | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set()) // 선택된 CartItem ID

// --- 장바구니 데이터 조회 ---
const fetchCart = async () => {
  try {
    // 실제 API 호출
    const res = await api.get<ApiResponse>('/api/v1/orders/cart-items')

    if (res.data.isSuccess) {
      setCartData(res.data.result)

      // 처음 로딩 시 '구매 가능'한 상품은 모두 기본 선택 처리
      if (isLoading) { // 최초 1회만 실행
        const availableItemIds = new Set(
            res.data.result.cartItems
            .filter(item => item.isAvailable)
            .map(item => item.id)
        )
        setSelectedItems(availableItemIds)
      }
    }
  } catch (error) {
    console.error('장바구니 조회 실패:', error)
    // 에러 시 빈 상태로 두거나 에러 UI 처리
  } finally {
    setIsLoading(false)
  }
}

useEffect(() => {
  fetchCart()
}, [])
const updateQuantity = async (productId: number, currentQty: number, delta: number) => {
  const newQty = currentQty + delta
  if (newQty < 1) return

  try {
    await api.post(`/api/v1/orders/cart/item`, { productId: productId, quantity: newQty })

    // 성공 시 데이터 다시 불러오기 (금액 재계산을 위해)
    await fetchCart()
  } catch (error) {
    console.error('수량 변경 실패:', error)
    alert('수량을 변경하지 못했습니다.')
  }
}
// 선택된 아이템 일괄 삭제 (선택 삭제 버튼 클릭)
const handleDeleteSelected = async () => {
  if (selectedItems.size === 0) {
    alert('삭제할 상품을 선택해주세요.')
    return
  }

  if (!confirm(`선택한 ${selectedItems.size}개의 상품을 삭제하시겠습니까?`)) return

  try {
    // Set을 배열로 변환
    const ids = Array.from(selectedItems)

    // DELETE 메서드에 Body를 실어 보낼 때는 { data: ... } 옵션 사용
    await api.delete('/api/v2/orders/cart-items', {
      data: {
        cartItemIds: ids,
      },
    })

    // 선택 목록 초기화 및 재조회
    setSelectedItems(new Set())
    await fetchCart()
  } catch (error) {
    console.error('일괄 삭제 실패:', error)
    alert('선택한 상품을 삭제하지 못했습니다.')
  }
}

// 장바구니 전체 삭제
const handleDeleteAll = async () => {
  if (!cartData || cartData.cartItems.length === 0) return
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

// 전체 선택/해제
const handleSelectAll = (checked: boolean) => {
  if (!cartData) return
  if (checked) {
    const allIds = new Set(cartData.cartItems.map(item => item.id))
    setSelectedItems(allIds)
  } else {
    setSelectedItems(new Set())
  }
}

// 개별 선택
const handleSelectItem = (id: number, checked: boolean) => {
  const newSelected = new Set(selectedItems)
  if (checked) {
    newSelected.add(id)
  } else {
    newSelected.delete(id)
  }
  setSelectedItems(newSelected)
}

// 주문하기 버튼 클릭
const handleOrder = () => {
  if (selectedItems.size === 0) {
    alert('주문할 상품을 선택해주세요.')
    return
  }
  // 선택된 cartItemId들을 쿼리로 넘겨서 주문 페이지에서 조회하도록 함
  const itemIds = Array.from(selectedItems).join(',')
  router.push(`/order?cartItemIds=${itemIds}`)
}
  const items = cartData?.cartItems || []
  const allSelected = items.length > 0 && items.every(item => selectedItems.has(item.id))
  const someSelected = items.some(item => selectedItems.has(item.id))

  const selectedCartItems = items.filter(item => selectedItems.has(item.id))
  const totalPrice = selectedCartItems.reduce((sum, item) => sum + (item.salePrice * item.quantity), 0)
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
            ) : items.length > 0 ? (
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
                      {items.map((item) => (
                          <div key={item.id} className="cart-item">
                            <div className="cart-item-checkbox">
                              {/* item.id 기준 선택 */}
                              <input
                                  type="checkbox"
                                  id={`item-${item.id}`}
                                  checked={selectedItems.has(item.id)}
                                  onChange={(e) => handleSelectItem(item.id, e.target.checked)}
                                  disabled={!item.isAvailable}
                              />
                            </div>
                            <div className="cart-item-image">
                              <div className="image-placeholder-small">이미지</div>
                            </div>
                            <div className="cart-item-info">
                              <div className="cart-item-name">{item.name}</div>
                              <div className="cart-item-price">₩{formatPrice(item.salePrice)}</div>
                              {!item.isAvailable && (
                                  <div style={{ fontSize: '13px', color: '#f44336', marginTop: '4px' }}>
                                    구매 불가
                                  </div>
                              )}
                            </div>
                            <div className="cart-item-actions">
                              <div className="quantity-control">
                                <button
                                    className="quantity-btn"
                                    // 수량 변경 시 item.productId 사용
                                    onClick={() => updateQuantity(item.productId, item.quantity, -1)}
                                    disabled={item.quantity <= 1 || !item.isAvailable}
                                >
                                  -
                                </button>
                                <span className="quantity-value">{item.quantity}</span>
                                <button
                                    className="quantity-btn"
                                    // 수량 변경 시 item.productId 사용
                                    onClick={() => updateQuantity(item.productId, item.quantity, 1)}
                                    disabled={!item.isAvailable}
                                >
                                  +
                                </button>
                              </div>
                              <div className="cart-item-total">
                                ₩{formatPrice(item.salePrice * item.quantity)}
                              </div>
                            </div>
                          </div>
                      ))}
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
                        {/* 주문하기 버튼 */}
                        <button onClick={handleOrder} className="order-button">
                          주문하기
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