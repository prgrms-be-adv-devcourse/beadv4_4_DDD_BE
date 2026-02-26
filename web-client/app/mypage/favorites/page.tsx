'use client'

import {Suspense, useEffect, useState} from 'react'
import Link from 'next/link'
import MypageLayout from '../../components/MypageLayout'
import {useSearchParams} from "next/navigation";
import api from "@/app/lib/axios";

type TabKey = 'product' | 'snap'

interface FavoriteProduct {
  productId: number
  productName: string
  sellerBusinessName: string
  salePrice: number
  primaryImageUrl?: string
}

interface RecommendedProduct {
  id: number
  name: string
  sellerBusinessName: string
  salePrice: number
  primaryImageUrl?: string
}

interface Pagination {
  page: number
  size: number
  hasNext: boolean
  totalElements: number
  totalPages: number
}

const PAGE_SIZE = 9

export default function FavoritesPage() {
  const [activeTab, setActiveTab] = useState<TabKey>('product')

  return (
    <MypageLayout>
      <div>
        <h1 className="page-title" style={{ fontSize: '28px', fontWeight: 700, marginBottom: '8px' }}>좋아요</h1>
        <p className="page-subtitle" style={{ color: '#666', fontSize: '14px', marginBottom: '24px' }}>
          내가 좋아요한 상품과 스냅을 한 곳에서 모아볼 수 있어요.
        </p>

          {/* Tabs */}
          <div
            style={{
              display: 'inline-flex',
              borderRadius: '999px',
              padding: '4px',
              background: '#f1f3ff',
              marginBottom: '24px',
            }}
          >
            <button
              type="button"
              onClick={() => setActiveTab('product')}
              style={{
                minWidth: '100px',
                padding: '8px 16px',
                borderRadius: '999px',
                border: 'none',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 600,
                background: activeTab === 'product' ? '#667eea' : 'transparent',
                color: activeTab === 'product' ? '#fff' : '#555',
                transition: 'all 0.2s',
              }}
            >
              상품
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('snap')}
              style={{
                minWidth: '100px',
                padding: '8px 16px',
                borderRadius: '999px',
                border: 'none',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 600,
                background: activeTab === 'snap' ? '#667eea' : 'transparent',
                color: activeTab === 'snap' ? '#fff' : '#555',
                transition: 'all 0.2s',
              }}
            >
              스냅
            </button>
          </div>

        {/* Tab contents */}
        {activeTab === 'product' ? (
          <Suspense fallback={<div>로딩 중...</div>}>
            <ProductFavorites />
          </Suspense>
        ) : <SnapFavorites />}
      </div>
    </MypageLayout>
  )
}

function ProductFavorites() {
  const [products, setProducts] = useState<FavoriteProduct[]>([])
  const [recommendations, setRecommendations] = useState<RecommendedProduct[]>([])

  const [isLoading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [recLoading, setRecLoading] = useState(true)

  const [pagination, setPagination] = useState<Pagination | null>(null)



  const searchParams = useSearchParams()
  const pageParam = searchParams.get('page')
  const currentPage = Math.max(0, parseInt(pageParam ?? '0', 10) || 0)

  const totalPages = pagination?.totalPages ?? 0
  const hasNext = pagination?.hasNext ?? false

  function buildPageUrl(page: number): string {
    const params = new URLSearchParams()
    params.set('page', String(page))
    return `/mypage/favorites?${params.toString()}`
  }

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
        if (!apiUrl) return

        const res = await api.get(`${apiUrl}/api/v1/products/favorites?page=${currentPage}&size=${PAGE_SIZE}`)

        const data = await res.data

        if (!data.isSuccess) {
          setError(data.message || '관심상품을 불러오지 못했습니다.')
          setProducts([])
          setPagination(null)
          return
        }

        setProducts(data.result ?? [])
        setPagination(data.pagination ?? null)

      } catch (e) {
        setError(e instanceof Error ? e.message : '관심상품을 불러오지 못했습니다.')
        setProducts([])
        setPagination(null)

      } finally {
        setLoading(false)
      }
    }

    const fetchRecommendations = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
        if (!apiUrl) return

        const res = await api.get(
            `${apiUrl}/api/v2/products/recommendations`
        )

        const data = res.data

        if (!data.isSuccess) return

        setRecommendations(data.result ?? [])
      } catch (e) {
        console.error('추천 API 실패:', e)
      } finally {
        setRecLoading(false)
      }
    }

    fetchFavorites()
    fetchRecommendations()
  }, [currentPage])

  return (
      <>
        {/* 기존 관심상품 Grid */}
        <div className="products-grid">
          {products.map((product) => (
              <Link
                  key={product.productId}
                  href={`/products/${product.productId}`}
                  className="product-card"
              >
                <div className="product-image">
                  {product.primaryImageUrl ? (
                      <img
                          src={product.primaryImageUrl}
                          alt={product.productName}
                          className="image-placeholder"
                      />
                  ) : (
                      <div className="image-placeholder">이미지</div>
                  )}
                </div>

                <div className="product-info">
                  <div className="product-brand">{product.sellerBusinessName}</div>
                  <div className="product-name">{product.productName}</div>
                  <div className="product-price">
                    ₩{product.salePrice.toLocaleString()}
                  </div>
                </div>
              </Link>
          ))}
        </div>

        {/* ===== Pagination ===== */}
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

        {/* ===== AI 추천 영역 (관심상품 아래) ===== */}
        {recommendations.length > 0 && (
            <section className="ai-recommend-section">
              <div className="ai-recommend-header">
                <h3 className="ai-recommend-title">
                  회원님이 좋아할 만한 상품
                </h3>
              </div>

              <div className="ai-recommend-scroll">
                {recommendations.map((item) => (
                    <Link
                        key={item.id}
                        href={`/products/${item.id}`}
                        className="product-card"
                    >
                      <div className="product-image">
                        {item.primaryImageUrl ? (
                            <img src={item.primaryImageUrl} alt={item.name} />
                        ) : (
                            <div className="image-placeholder">이미지</div>
                        )}
                      </div>

                      <div className="product-info">
                        <div className="product-brand">
                          {item.sellerBusinessName}
                        </div>
                        <div className="product-name">
                          {item.name}
                        </div>
                        <div className="product-price">
                          ₩{item.salePrice.toLocaleString()}
                        </div>
                      </div>
                    </Link>
                ))}
              </div>
            </section>
        )}
      </>
  )
}

function SnapFavorites() {
  const posts = Array.from({ length: 9 }, (_, i) => ({
    id: i + 1,
    likes: Math.floor(Math.random() * 1000) + 100,
    comments: Math.floor(Math.random() * 50) + 5,
  }))

  return (
    <div className="magazine-grid">
      {posts.map((post) => (
        <Link key={post.id} href={`/magazine/${post.id}`} className="magazine-post">
          <div className="post-image">
            <div className="image-placeholder">이미지</div>
            <div className="post-overlay">
              <div className="post-stats">
                <span className="stat-item">❤️ {post.likes}</span>
                <span className="stat-item">💬 {post.comments}</span>
              </div>
            </div>
          </div>
        </Link>
      ))}
    </div>
  )
}

