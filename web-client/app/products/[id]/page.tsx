'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import {useCallback, useEffect, useState} from 'react'
import Header from '../../components/Header'
import api from '@/app/lib/axios'

const FASHION_CATEGORIES = [
  { label: '아우터', value: 'outer' },
  { label: '상의', value: 'upper' },
  { label: '하의', value: 'lower' },
  { label: '모자', value: 'cap' },
  { label: '가방', value: 'bag' },
  { label: '신발', value: 'shoes' },
  { label: '뷰티', value: 'beauty' },
] as const


interface ProductImageDto {
  id: number
  imageUrl: string
  isPrimary: boolean
  sortOrder: number
}

interface ProductDetailResponse {
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
  quantity: number
  isFavorite: boolean
  favoriteCount: number
  images: ProductImageDto[]
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: ProductDetailResponse
}

interface InventoryDto {
  productId: number;
  sellerId: number;
  quantity: number;
  initialized: boolean;
}

export default function ProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const productId = params.id as string

  const [product, setProduct] = useState<ProductDetailResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedImageUrl, setSelectedImageUrl] = useState<string | null>(null);

  const fetchProduct = useCallback(async () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
    if (!apiUrl || !productId) {
      setProduct(null)
      return
    }

    try {
      const url = `${apiUrl}/api/v1/products/${productId}`
      const res = await api.get<ApiResponse>(url)

      const data: ApiResponse = res.data

      if (data.isSuccess && data.result) {
        setProduct(data.result)
      } else {
        setError(data.message || '상품 정보를 불러오지 못했습니다.')
        setProduct(null)
      }
    } catch (e: any) {
      console.error(e)
      setError('상품 정보를 불러오는 중 오류가 발생했습니다.')
      setProduct(null)
    } finally {
      setIsLoading(false)
    }
  }, [productId])

  const fetchInventory = async (productId: number): Promise<number | null> => {
    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await api.get(`${apiUrl}/api/v2/inventories/${productId}`)

      if (res.data) {
        return res.data.quantity
      }

      return null
    } catch (error) {
      console.error('재고 조회 실패:', error)
      return null
    }
  }

  useEffect(() => {
    fetchProduct()
  }, [fetchProduct])

  useEffect(() => {
    if (!selectedImageUrl && product?.images && product.images.length > 0) {
      setSelectedImageUrl(product.images[0].imageUrl);
    }
  }, [product, selectedImageUrl])

  useEffect(() => {
    const syncInventory = async () => {
      if (!product) return

      const quantity = await fetchInventory(product.id)
      if (typeof quantity === 'number' && Number.isFinite(quantity)) {
        setProduct(prev => (prev ? { ...prev, quantity } : prev))
      }
    }

    syncInventory()
  }, [product?.id])

  const [quantity, setQuantity] = useState(1)
  const [isCreatingOrder, setIsCreatingOrder] = useState(false)
  const [isAddingToCart, setIsAddingToCart] = useState(false)
  const [isTogglingFavorite, setIsTogglingFavorite] = useState(false)

  const getSafeStock = () => {
    if (!product || typeof product.quantity !== 'number' || !Number.isFinite(product.quantity)) {
      return 0
    }
    return product.quantity
  }


  const getCategoryLabel = (category: string): string => {
    const found = FASHION_CATEGORIES.find(
        (item) => item.value.toUpperCase() === category
    );

    return found?.label ?? category;
  };

  const handleOrder = async () => {
    if (!product) return

    setIsCreatingOrder(true)

    try {
      // 1. 재고 조회 API 호출
      const currentQuantity = await fetchInventory(product.id)

      if (currentQuantity === null) {
        alert('재고 조회 중 오류가 발생했습니다.')
        return
      }

      if (currentQuantity <= 0) {
        alert('품절된 상품입니다.')
        return
      }

      if (quantity > currentQuantity) {
        alert(`재고가 부족합니다. (현재 재고: ${currentQuantity}개)`)
        return
      }

      // 2. 주문 생성 API 호출
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
      const res = await api.post(`${apiUrl}/api/v1/orders`, {
        productId: product.id,
        quantity: quantity,
      })

      const data = res.data

      console.log('주문 생성 API 응답:', data)

      if (res.status === 401 || data.code === 'AUTH_401_002') {
        alert('로그인 시간이 만료되었습니다. 다시 로그인해주세요.')
        router.replace('/login')
        return
      }

      if (data.isSuccess && data.result) {
        // 성공 시 이동
        const orderId = data.result.orderId
        router.push(`/order?orderId=${orderId}`)
      } else {
        alert(data.message || '주문 생성 실패')
      }

    } catch (error: any) {
      // 401, 400 에러
      const status = error.response?.status
      const errorMsg = error.response?.data?.message || '주문 중 오류가 발생했습니다.'

      if (status === 401) {
        alert('로그인이 필요하거나 세션이 만료되었습니다.')
        router.push('/login')
      } else {
        alert(errorMsg)
      }
    } finally {
      setIsCreatingOrder(false)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price)
  }

  const formatCount = (count: number): string => {
    const format = (value: number, unit: string) =>
        `${Number(value.toFixed(1))}${unit}`;

    if (count < 1000) return count.toString();
    if (count < 10_000) return format(count / 1000, '천');
    if (count < 100_000_000) return format(count / 10_000, '만');
    return format(count / 100_000_000, '억');
  };

  const HeartIcon = ({active}: { active: boolean }) => (
      <svg
          width="24"
          height="24"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
      >
        <path
            d="M12 21s-6.716-4.514-9.428-7.226C.78 11.98.78 8.993 2.69 7.083c1.91-1.91 4.897-1.91 6.807 0L12 9.586l2.503-2.503c1.91-1.91 4.897-1.91 6.807 0 1.91 1.91 1.91 4.897 0 6.807C18.716 16.486 12 21 12 21z"
            fill={active ? '#e60023' : 'none'}
            stroke={active ? 'none' : '#999'}
            strokeWidth="1.6"
            style={{
              fillOpacity: active ? 1 : 0,
            }}
        />
      </svg>
  );

  const handleToggleFavorite = async () => {
    // API 호출 or optimistic update
    if (!product || isTogglingFavorite) return
    const res = await api.get('/api/v1/auths/me', {
      withCredentials: true
    })

    if (!res.data.result.isAuthenticated) {
      alert('로그인이 필요한 서비스입니다.')
      router.push('/login')
      return
    }

    const apiUrl = process.env.NEXT_PUBLIC_API_URL || '';
    if (!apiUrl) return

    const prevIsFavorite = product.isFavorite
    const prevCount = product.favoriteCount

    // ✅ optimistic update
    setProduct({
      ...product,
      isFavorite: !prevIsFavorite,
      favoriteCount: prevIsFavorite
          ? prevCount - 1
          : prevCount + 1,
    })

    setIsTogglingFavorite(true)

    try {
      let res;
      if (prevIsFavorite) {
        res = await api.delete(
            `${apiUrl}/api/v1/products/favorites/${product.id}`
        )
      } else {
        res = await api.post(
            `${apiUrl}/api/v1/products/favorites/${product.id}`
        );
      }

      if (!res.data?.isSuccess) {
        throw new Error('관심상품 처리 실패')
      }
    } catch (e) {
      // ❌ rollback
      setProduct({
        ...product,
        isFavorite: prevIsFavorite,
        favoriteCount: prevCount,
      })

      alert('관심상품 처리 중 오류가 발생했습니다.')
    } finally {
      setIsTogglingFavorite(false)
    }

    console.log('관심상품 토글');
  };

  const handleAddToCart = async () => {
    if (!product) return

    setIsAddingToCart(true)

    try {
      // 1. 재고 조회
      const currentQuantity = await fetchInventory(product.id)

      if (currentQuantity === null) {
        alert('재고 조회 중 오류가 발생했습니다.')
        return
      }

      if (currentQuantity <= 0) {
        alert('품절된 상품입니다.')
        return
      }

      if (quantity > currentQuantity) {
        alert(`재고가 부족합니다. (현재 재고: ${currentQuantity}개)`)
        return
      }

      const res = await api.post('/api/v1/orders/cart/item', {
        productId: product.id, // 현재 페이지의 상품 ID
        quantity: quantity // 사용자가 선택한 수량
      })
      alert('장바구니에 상품이 추가되었습니다.')
      router.push('/cart')
    } catch (error) {
      console.error('장바구니 추가 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '장바구니 추가 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsAddingToCart(false)
    }
  }

  if (isLoading) {
    return (
        <div className="home-page">
          <Header/>
          <div className="product-detail-container">
            <div className="container">
              <div style={{textAlign: 'center', padding: '80px 20px'}}>
                <p>상품 정보를 불러오는 중...</p>
              </div>
            </div>
          </div>
        </div>
    )
  }

  if (error || !product) {
    return (
        <div className="home-page">
          <Header/>
          <div className="product-detail-container">
            <div className="container">
              <div style={{textAlign: 'center', padding: '80px 20px'}}>
                <p style={{color: '#f44336', marginBottom: '20px'}}>{error || '상품을 찾을 수 없습니다.'}</p>
                <Link href="/" style={{color: '#667eea', textDecoration: 'underline'}}>
                  홈으로 돌아가기
                </Link>
              </div>
            </div>
          </div>
        </div>
    )
  }

  return (
      <div className="home-page">
        {/* Header */}
        <Header/>

        {/* Product Detail Section */}
        <div className="product-detail-container">
          <div className="container">
            <div className="product-detail-content">
              {/* Product Images */}
              <div className="product-images">
                {/* Main Image */}
                <div className="main-image">
                  {selectedImageUrl ? (
                      <img
                          src={selectedImageUrl ?? product.images[0]?.imageUrl}
                          alt="상품 이미지"
                          className="image-placeholder-large"
                      />
                  ) : (
                      <div className="image-placeholder-large">상품 이미지</div>
                  )}
                </div>

                {/* Thumbnail Images */}
                <div className="thumbnail-images">
                  {product.images.map((image) => (
                      <div
                          key={image.id}
                          className="thumbnail"
                          onClick={() => setSelectedImageUrl(image.imageUrl)}
                      >
                        <img
                            src={image.imageUrl}
                            alt=""
                            className="image-placeholder-small"
                        />
                      </div>
                  ))}
                </div>
              </div>

              {/* Product Info */}
              <div className="product-info-section">

                {/* Brand + Favorite */}
                <div className="product-header">
                  <div className="product-brand-name">{product.sellerBusinessName}</div>
                  {/*<div className="product-favorite">*/}
                  {/*  ♡ <span>{formatCount(product.favoriteCount)}</span>*/}
                  {/*</div>*/}
                </div>

                {/* Category */}
                <div className="product-category">{getCategoryLabel(product.category)}</div>
                <h1 className="product-title">{product.name}</h1>
                <div className="product-price-section">
                  {product.salePrice < product.price ? (
                      <>
                        <span
                            className="product-price original-price">₩{formatPrice(product.price)}</span>
                        <span
                            className="product-price sale-price">₩{formatPrice(product.salePrice)}</span>
                      </>
                  ) : (
                      <span className="product-price">₩{formatPrice(product.salePrice)}</span>
                  )}
                </div>

                <div className="product-divider"></div>

                {/* Delivery Info */}
                <div className="delivery-info">
                  <div className="info-row">
                    <span className="info-label">배송비</span>
                    <span className="info-value">무료배송</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">배송예정</span>
                    <span className="info-value">01.14(수) 도착 예정</span>
                  </div>
                </div>

                <div className="product-divider"></div>

                {/* 상품명 왼쪽 위, 수량·구매금액 같은 row (구매하기 버튼 위) */}
                <div className="product-options">
                  <div className="option-group quantity-with-amount">
                    <span className="option-label product-name-row">{product.name}</span>
                    <div className="quantity-amount-row">
                      <div className="quantity-selector">
                        <button
                            className="quantity-btn minus"
                            onClick={() => setQuantity(Math.max(1, quantity - 1))}
                            disabled={quantity <= 1}
                        >
                          -
                        </button>
                        <span className="quantity-value">{quantity}</span>
                        <button
                            className="quantity-btn plus"
                            onClick={() => setQuantity(prev => Math.min(getSafeStock(), prev + 1))}
                            disabled={getSafeStock() <= 0 || quantity >= getSafeStock()}
                        >
                          +
                        </button>
                      </div>
                      <span
                          className="quantity-amount">₩{formatPrice(product.salePrice * quantity)}</span>
                    </div>
                  </div>
                </div>

                <div className="product-divider"></div>

                {/* Action Buttons */}
                <div className="action-buttons">
                  <button
                      className={`favorite-button ${product.isFavorite ? 'active' : ''}`}
                      aria-label="관심상품"
                  >
                    {/* 클릭 영역 */}
                    <span
                        className="favorite-icon"
                        onClick={(e) => {
                          e.stopPropagation(); // 버튼 이벤트 차단
                          handleToggleFavorite();
                        }}
                    >
    <HeartIcon active={product.isFavorite}/>
  </span>

                    {/* 숫자는 클릭 안 됨 */}
                    <span className="favorite-count">
    {formatCount(product.favoriteCount)}
  </span>
                  </button>
                  <button
                      className="cart-button"
                      onClick={handleAddToCart}
                      disabled={isAddingToCart || product.quantity <= 0}
                  >
                    {isAddingToCart ? '추가 중...' : product.quantity <= 0 ? '품절' : '장바구니'}
                  </button>
                  <button
                      className="buy-button"
                      onClick={handleOrder}
                      disabled={isCreatingOrder || product.quantity <= 0}
                  >
                    {isCreatingOrder ? '주문 처리 중...' : product.quantity <= 0 ? '재입고 요청' : '구매하기'}
                  </button>
                </div>
              </div>
            </div>

            {/* Product Description */}
            <div className="product-description-section">
              <h2 className="description-title">상품 상세 정보</h2>
              <div className="description-content">
                <p>{product.description || '상품 상세 설명이 없습니다.'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <footer className="footer">
          <div className="container">
            <div className="footer-content">
              <div className="footer-section">
                <h3>고객센터</h3>
                <p>1588-0000</p>
                <p>평일 09:00 - 18:00</p>
              </div>
              <div className="footer-section">
                <h3>회사정보</h3>
                <p>주소: 서울시 강남구</p>
                <p>사업자등록번호: 000-00-00000</p>
              </div>
              <div className="footer-section">
                <h3>이용안내</h3>
                <Link href="/terms">이용약관</Link>
                <Link href="/privacy">개인정보처리방침</Link>
              </div>
            </div>
            <div className="footer-bottom">
              <p>&copy; 2024 뭐든사. All rights reserved.</p>
            </div>
          </div>
        </footer>
      </div>
  )
}