'use client'

import Link from 'next/link'
import { useParams, useRouter } from 'next/navigation'
import { useState } from 'react'

export default function MagazineDetailPage() {
  const params = useParams()
  const router = useRouter()
  const id = parseInt(params.id as string)
  
  const [isLiked, setIsLiked] = useState(false)
  const [comment, setComment] = useState('')
  const [comments, setComments] = useState([
    { id: 1, author: 'user1', text: '정말 예쁘네요!', date: new Date(2024, 0, 14) },
    { id: 2, author: 'user2', text: '어디서 구매하셨나요?', date: new Date(2024, 0, 13) },
  ])

  // Mock data - 실제로는 API에서 가져올 데이터
  const post = {
    id: id,
    image: '이미지',
    likes: Math.floor(Math.random() * 1000) + 100,
    comments: comments.length,
    category: id % 3 === 0 ? '패션' : id % 3 === 1 ? '뷰티' : '라이프',
    title: ['봄 코디', '데일리 룩', '스킨케어', '메이크업', '홈 데코', '트렌드'][id % 6],
    date: new Date(2024, 0, 15 - id),
    author: 'fashionista',
    description: '이번 봄 시즌에 딱 어울리는 코디를 소개해드려요. 부드러운 파스텔 톤과 함께 가볍게 입을 수 있는 아이템들로 구성했습니다. 특히 이 가디건은 다양한 스타일링이 가능해서 추천드려요!',
    tags: ['#봄코디', '#데일리룩', '#패션', '#OOTD'],
  }

  const handleLike = () => {
    setIsLiked(!isLiked)
  }

  const handleCommentSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (comment.trim()) {
      setComments([...comments, {
        id: comments.length + 1,
        author: 'me',
        text: comment,
        date: new Date(),
      }])
      setComment('')
    }
  }

  const formatDate = (date: Date) => {
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    
    if (days === 0) return '오늘'
    if (days === 1) return '어제'
    if (days < 7) return `${days}일 전`
    if (days < 30) return `${Math.floor(days / 7)}주 전`
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
  }

  return (
    <div className="home-page">
      {/* Header */}
      <header className="header">
        <div className="header-container">
          <div className="logo">
            <Link href="/">뭐든사</Link>
          </div>
          <nav className="nav">
            <Link href="/fashion">패션</Link>
            <Link href="/beauty">뷰티</Link>
            <Link href="/sale">세일</Link>
            <Link href="/magazine">매거진</Link>
          </nav>
          <div className="header-actions">
            <Link href="/search" className="search-btn">검색</Link>
            <Link href="/cart" className="cart-btn">장바구니</Link>
            <Link href="/login" className="user-btn">로그인</Link>
          </div>
        </div>
      </header>

      {/* Magazine Detail */}
      <section className="magazine-detail-section">
        <div className="container">
          <div className="magazine-detail-container">
            {/* Back Button */}
            <button 
              className="magazine-back-btn"
              onClick={() => router.back()}
            >
              ← 목록으로
            </button>

            {/* Detail Content */}
            <div className="magazine-detail-content">
              {/* Image Section */}
              <div className="magazine-detail-image">
                <div className="detail-image-placeholder">
                  {post.image}
                </div>
              </div>

              {/* Info Section */}
              <div className="magazine-detail-info">
                {/* Header */}
                <div className="detail-header">
                  <div className="detail-author">
                    <div className="author-avatar">{post.author[0].toUpperCase()}</div>
                    <div>
                      <div className="author-name">{post.author}</div>
                      <div className="post-category">{post.category}</div>
                    </div>
                  </div>
                  <div className="detail-date">{formatDate(post.date)}</div>
                </div>

                {/* Title */}
                <h1 className="detail-title">{post.title}</h1>

                {/* Stats */}
                <div className="detail-stats">
                  <button 
                    className={`stat-btn like-btn ${isLiked ? 'liked' : ''}`}
                    onClick={handleLike}
                  >
                    <span className="stat-icon">❤️</span>
                    <span>{post.likes + (isLiked ? 1 : 0)}</span>
                  </button>
                  <div className="stat-btn">
                    <span className="stat-icon">💬</span>
                    <span>{post.comments + comments.length}</span>
                  </div>
                </div>

                {/* Description */}
                <div className="detail-description">
                  <p>{post.description}</p>
                </div>

                {/* Tags */}
                <div className="detail-tags">
                  {post.tags.map((tag, index) => (
                    <span key={index} className="detail-tag">{tag}</span>
                  ))}
                </div>

                {/* Comments Section */}
                <div className="detail-comments">
                  <h3 className="comments-title">댓글 {comments.length}</h3>
                  <div className="comments-list">
                    {comments.map((comment) => (
                      <div key={comment.id} className="comment-item">
                        <div className="comment-author">{comment.author}</div>
                        <div className="comment-text">{comment.text}</div>
                        <div className="comment-date">{formatDate(comment.date)}</div>
                      </div>
                    ))}
                  </div>

                  {/* Comment Form */}
                  <form className="comment-form" onSubmit={handleCommentSubmit}>
                    <input
                      type="text"
                      className="comment-input"
                      placeholder="댓글을 입력하세요..."
                      value={comment}
                      onChange={(e) => setComment(e.target.value)}
                    />
                    <button 
                      type="submit" 
                      className="comment-submit-btn"
                      disabled={!comment.trim()}
                    >
                      게시
                    </button>
                  </form>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

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
