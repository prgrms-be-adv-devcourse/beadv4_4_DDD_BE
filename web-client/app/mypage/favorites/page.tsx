'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '../../components/Header'

type TabKey = 'product' | 'snap'

export default function FavoritesPage() {
  const [activeTab, setActiveTab] = useState<TabKey>('product')

  return (
    <div className="home-page">
      <Header />

      {/* Page Header */}
      <div className="page-header">
        <div className="container">
          <h1 className="page-title">좋아요 내역</h1>
          <p className="page-subtitle">내가 좋아요한 상품과 스냅을 한 곳에서 모아볼 수 있어요.</p>
        </div>
      </div>

      {/* Favorites Content */}
      <section className="products-section">
        <div className="container">
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
      </section>
    </div>
  )
}

function ProductFavorites() {
  return (
    <div className="products-grid">
      {[1, 2, 3, 4, 5, 6].map((item) => (
        <Link key={item} href={`/products/${item}`} className="product-card">
          <div className="product-image">
            <div className="image-placeholder">이미지</div>
          </div>
          <div className="product-info">
            <div className="product-brand">브랜드명</div>
            <div className="product-name">좋아요한 상품 {item}</div>
            <div className="product-price">₩{((item * 10000) + 9000).toLocaleString()}</div>
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

