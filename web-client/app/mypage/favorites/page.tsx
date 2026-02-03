'use client'

import {useEffect, useState} from 'react'
import Link from 'next/link'
import MypageLayout from '../../components/MypageLayout'
import {useSearchParams} from "next/navigation";

type TabKey = 'product' | 'snap'

interface FavoriteProduct {
  productId: number
  productName: string
  sellerBusinessName: string
  salePrice: number
  primaryImageUrl?: string
}

const PAGE_SIZE = 12

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
        {activeTab === 'product' ? <ProductFavorites /> : <SnapFavorites />}
      </div>
    </MypageLayout>
  )
}

function ProductFavorites() {
  const [products, setProducts] = useState<FavoriteProduct[]>([])
  const [isLoading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const searchParams = useSearchParams()
  const pageParam = searchParams.get('page')
  const currentPage = Math.max(0, parseInt(pageParam ?? '0', 10) || 0)


  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        const accessToken = localStorage.getItem('accessToken')
        if (!accessToken?.trim()) {
          setLoading(false)
          return
        }
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || ''
        if (!apiUrl) return

        const res = await fetch(`${apiUrl}/api/v1/products/favorites?page=${currentPage}&size=${PAGE_SIZE}`, {
          headers: { Authorization: `Bearer ${accessToken}` },
        })

        const data = await res.json()

        if (!res.ok || !data.isSuccess) {
          setError(data.message || '관심상품을 불러오지 못했습니다.')
          return
        }

        setProducts(data.result ?? [])
      } catch (e) {
        setError(e instanceof Error ? e.message : '관심상품을 불러오지 못했습니다.')
      } finally {
        setLoading(false)
      }
    }

    fetchFavorites()
  }, [])

  return (
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

