'use client'

import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import {Suspense, useCallback, useEffect, useState} from 'react'
import Header from '../components/Header'

const BEAUTY_CATEGORIES = [
  { label: '전체', href: '/beauty', value: 'beauty' },
  { label: '스킨케어', href: '/beauty?category=skincare', value: 'skincare' },
  { label: '메이크업', href: '/beauty?category=makeup', value: 'makeup' },
  { label: '헤어', href: '/beauty?category=hair', value: 'hair' },
  { label: '바디', href: '/beauty?category=body', value: 'body' },
  { label: '네일', href: '/beauty?category=nail', value: 'nail' },
  { label: '향수', href: '/beauty?category=fragrance', value: 'fragrance' },
] as const

const PAGE_SIZE = 12

interface ProductResponse {
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
  primaryImageUrl: string
  createdAt: string
  updatedAt: string
  createdBy: number
  updatedBy: number
}

interface Pagination {
  page: number
  size: number
  hasNext: boolean
  totalElements: number
  totalPages: number
}

interface ProductsApiResponse {
  isSuccess: boolean
  code: string
  message: string
  pagination: Pagination | null
  result: ProductResponse[] | null
}

function formatPrice(price: number): string {
  return new Intl.NumberFormat('ko-KR').format(price)
}

function BeautyContent() {
  const searchParams = useSearchParams()
  const currentCategory = searchParams.get('category')
  const pageParam = searchParams.get('page')
  const currentPage = Math.max(0, parseInt(pageParam ?? '0', 10) || 0)

  const [products, setProducts] = useState<ProductResponse[]>([])
  const [pagination, setPagination] = useState<Pagination | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const HeartFilledIcon = () => (
      <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="#e60023"
          xmlns="http://www.w3.org/2000/svg"
      >
        <path d="M12 21s-6.716-4.514-9.428-7.226C.78 11.98.78 8.993 2.69 7.083c1.91-1.91 4.897-1.91 6.807 0L12 9.586l2.503-2.503c1.91-1.91 4.897-1.91 6.807 0 1.91 1.91 1.91 4.897 0 6.807C18.716 16.486 12 21 12 21z" />
      </svg>
  )

  const formatCount = (count: number): string => {
    const format = (value: number, unit: string) =>
        `${Number(value.toFixed(1))}${unit}`

    if (count < 1000) return count.toString()
    if (count < 10_000) return format(count / 1000, '천')
    if (count < 100_000_000) return format(count / 10_000, '만')
    return format(count / 100_000_000, '억')
  }

  const fetchProducts = useCallback(async () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
    if (!apiUrl) {
      setProducts([])
      setPagination(null)
      setIsLoading(false)
      return
    }
    setIsLoading(true)
    setError(null)
    try {
      const category = 'BEAUTY'
      const url = `${apiUrl}/api/v1/products?category=${encodeURIComponent(category)}&page=${currentPage}&size=${PAGE_SIZE}`
      const res = await fetch(url, {
        credentials: 'include' // 브라우저에 저장된 쿠키(accessToken)를 함께 전송
      })
      const data: ProductsApiResponse = await res.json()
      if (!res.ok) {
        setError(data.message || '상품 목록을 불러오지 못했습니다.')
        setProducts([])
        setPagination(null)
        return
      }
      if (data.isSuccess && data.result) {
        setProducts(data.result)
        setPagination(data.pagination ?? null)
      } else {
        setProducts([])
        setPagination(null)
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : '상품 목록을 불러오지 못했습니다.')
      setProducts([])
      setPagination(null)
    } finally {
      setIsLoading(false)
    }
  }, [currentCategory, currentPage])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  const totalPages = pagination?.totalPages ?? 0
  const hasNext = pagination?.hasNext ?? false

  function buildPageUrl(page: number): string {
    const params = new URLSearchParams()
    if (currentCategory) params.set('category', currentCategory)
    params.set('page', String(page))
    return `/beauty?${params.toString()}`
  }

  return (
      <div className="home-page">
        <Header />

        <div className="page-header">
          <div className="container">
            <h1 className="page-title">뷰티</h1>
            <p className="page-subtitle">프리미엄 뷰티 제품을 만나보세요</p>
          </div>
        </div>

        {/* Category Section */}
        <section className="category-section beauty-category-section">
          <div className="container">
            <div className="category-filters">
              {BEAUTY_CATEGORIES.map(({ label, href, value }) => {
                const isActive = value === null ? !currentCategory : currentCategory === value
                return (
                    <Link
                        key={value ?? 'all'}
                        href={href}
                        className={`category-filter-btn${isActive ? ' active' : ''}`}
                    >
                      {label}
                    </Link>
                )
              })}
            </div>
          </div>
        </section>

        {/* Products Section */}
        <section className="products-section">
          <div className="container">
            <div className="products-header">
              <h2 className="section-title">전체 상품</h2>
              {/*<div className="filter-options">*/}
              {/*  <select className="filter-select">*/}
              {/*    <option>정렬순</option>*/}
              {/*    <option>인기순</option>*/}
              {/*    <option>최신순</option>*/}
              {/*    <option>가격 낮은순</option>*/}
              {/*    <option>가격 높은순</option>*/}
              {/*  </select>*/}
              {/*</div>*/}
            </div>

            {isLoading ? (
                <div style={{ textAlign: 'center', padding: '48px 20px', color: '#666' }}>
                  상품 목록을 불러오는 중...
                </div>
            ) : error ? (
                <div style={{ textAlign: 'center', padding: '48px 20px', color: '#f44336' }}>
                  {error}
                </div>
            ) : products.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '48px 20px', color: '#666' }}>
                  등록된 상품이 없습니다.
                </div>
            ) : (
                <>
                  <div className="products-grid">
                    {products.map((product) => (
                        <Link key={product.id} href={`/products/${product.id}`} className="product-card">
                          <div className="product-image">
                            <img src={product.primaryImageUrl} alt={product.name} />
                            <div className="favorite-overlay">
                              <HeartFilledIcon />
                              <span className="favorite-count">{formatCount(product.favoriteCount)}</span>
                            </div>
                          </div>
                          <div className="product-info">
                            <div className="product-brand">{product.sellerBusinessName || '브랜드'}</div>
                            <div className="product-name">{product.name}</div>
                            <div className="product-price">
                              ₩{formatPrice(Number(product.salePrice))}
                              {product.salePrice < product.price && (
                                  <span style={{ marginLeft: '8px', fontSize: '13px', color: '#999', textDecoration: 'line-through' }}>
                            ₩{formatPrice(Number(product.price))}
                          </span>
                              )}
                            </div>
                          </div>
                        </Link>
                    ))}
                  </div>

                  {totalPages > 1 && (
                      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '32px' }}>
                        <Link
                            href={currentPage <= 0 ? '#' : buildPageUrl(currentPage - 1)}
                            style={{
                              padding: '8px 16px',
                              borderRadius: '8px',
                              border: '1px solid #e0e0e0',
                              background: currentPage <= 0 ? '#f5f5f5' : '#fff',
                              color: currentPage <= 0 ? '#999' : '#333',
                              pointerEvents: currentPage <= 0 ? 'none' : 'auto',
                              textDecoration: 'none',
                            }}
                        >
                          이전
                        </Link>
                        <span style={{ fontSize: '14px', color: '#666' }}>
                    {currentPage + 1} / {totalPages}
                  </span>
                        <Link
                            href={!hasNext ? '#' : buildPageUrl(currentPage + 1)}
                            style={{
                              padding: '8px 16px',
                              borderRadius: '8px',
                              border: '1px solid #e0e0e0',
                              background: !hasNext ? '#f5f5f5' : '#fff',
                              color: !hasNext ? '#999' : '#333',
                              pointerEvents: !hasNext ? 'none' : 'auto',
                              textDecoration: 'none',
                            }}
                        >
                          다음
                        </Link>
                      </div>
                  )}
                </>
            )}
          </div>
        </section>

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

export default function BeautyPage() {
  return (
      <Suspense fallback={<div>로딩 중...</div>}>
        <BeautyContent />
      </Suspense>
  )
}