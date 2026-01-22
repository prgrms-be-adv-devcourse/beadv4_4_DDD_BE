'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState, useEffect } from 'react'

interface ContentImageRequest {
  imageUrl: string
  isPrimary: boolean
  sortOrder: number
}

interface ContentRequest {
  text: string
  tags: string[]
  images: ContentImageRequest[]
}

interface ContentResponse {
  contentId: number
  authorMemberId: number
  text: string
  tags: string[]
  images: any[]
  [key: string]: any
}

interface ApiResponse {
  isSuccess: boolean
  code: string
  message: string
  result: ContentResponse
}

export default function MagazineWritePage() {
  const router = useRouter()
  const [text, setText] = useState('')
  const [tags, setTags] = useState<string[]>([])
  const [tagInput, setTagInput] = useState('')
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [imagePreviews, setImagePreviews] = useState<string[]>([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleAddTag = () => {
    if (tagInput.trim() && tags.length < 5 && tagInput.length <= 10) {
      if (!tags.includes(tagInput.trim())) {
        setTags([...tags, tagInput.trim()])
        setTagInput('')
      }
    }
  }

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove))
  }

  const handleTagInputKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddTag()
    }
  }

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files) return

    const newFiles: File[] = []
    const newPreviews: string[] = []

    Array.from(files).forEach((file) => {
      if (file.type.startsWith('image/')) {
        if (imageFiles.length + newFiles.length < 5) {
          newFiles.push(file)
          const reader = new FileReader()
          reader.onloadend = () => {
            const result = reader.result as string
            setImagePreviews(prev => [...prev, result])
          }
          reader.readAsDataURL(file)
        } else {
          alert('이미지는 최대 5개까지 추가할 수 있습니다.')
        }
      } else {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
      }
    })

    setImageFiles(prev => [...prev, ...newFiles])
  }

  const handleImageRemove = (index: number) => {
    setImageFiles(prev => prev.filter((_, i) => i !== index))
    setImagePreviews(prev => prev.filter((_, i) => i !== index))
  }

  const convertFileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onloadend = () => {
        const result = reader.result as string
        resolve(result)
      }
      reader.onerror = reject
      reader.readAsDataURL(file)
    })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!text.trim()) {
      alert('내용을 입력해주세요.')
      return
    }

    if (text.length > 500) {
      alert('내용은 500자 이하로 입력해주세요.')
      return
    }

    if (tags.length === 0) {
      alert('태그를 최소 1개 이상 추가해주세요.')
      return
    }

    if (imageFiles.length === 0) {
      alert('이미지를 최소 1개 이상 추가해주세요.')
      return
    }

    setIsSubmitting(true)

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      
      // 이미지 파일을 base64로 변환
      const imageUrls: string[] = []
      for (const file of imageFiles) {
        const base64 = await convertFileToBase64(file)
        imageUrls.push(base64)
      }
      
      const contentRequest: ContentRequest = {
        text: text.trim(),
        tags: tags,
        images: imageUrls.map((url, index) => ({
          imageUrl: url,
          isPrimary: index === 0,
          sortOrder: index,
        })),
      }

      console.log('콘텐츠 생성 요청:', contentRequest)

      const response = await fetch(`${apiUrl}/api/v1/contents`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(contentRequest),
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('API 응답 에러:', response.status, errorText)
        let errorMessage = `콘텐츠 생성 실패 (${response.status})`
        try {
          const errorResponse = JSON.parse(errorText)
          if (errorResponse.message) {
            errorMessage = errorResponse.message
          }
        } catch (e) {
          // JSON 파싱 실패 시 기본 메시지 사용
        }
        throw new Error(errorMessage)
      }

      const apiResponse: ApiResponse = await response.json()
      console.log('콘텐츠 생성 응답:', apiResponse)

      if (apiResponse.isSuccess && apiResponse.result) {
        alert('글이 성공적으로 작성되었습니다.')
        const contentId = apiResponse.result.contentId || apiResponse.result.id
        if (contentId) {
          router.push(`/magazine/${contentId}`)
        } else {
          router.push('/magazine')
        }
      } else {
        throw new Error(apiResponse.message || '콘텐츠 생성에 실패했습니다.')
      }
    } catch (error) {
      console.error('콘텐츠 생성 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '콘텐츠 생성 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
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

      {/* Write Section */}
      <section className="magazine-write-section">
        <div className="container">
          <div className="magazine-write-container">
            <div className="write-header">
              <h1 className="write-title">새 글 작성</h1>
              <Link href="/magazine" className="write-cancel-btn">
                취소
              </Link>
            </div>

            <form className="write-form" onSubmit={handleSubmit}>
              {/* Content Text */}
              <div className="form-group">
                <label htmlFor="text" className="form-label">내용</label>
                <textarea
                  id="text"
                  className="write-textarea"
                  placeholder="내용을 입력하세요 (최대 500자)"
                  value={text}
                  onChange={(e) => setText(e.target.value)}
                  maxLength={500}
                  rows={8}
                  required
                />
                <div className="char-count">{text.length} / 500</div>
              </div>

              {/* Tags */}
              <div className="form-group">
                <label htmlFor="tags" className="form-label">태그 (최대 5개)</label>
                <div className="tag-input-wrapper">
                  <input
                    type="text"
                    id="tags"
                    className="tag-input"
                    placeholder="태그를 입력하고 Enter를 누르세요 (최대 10자)"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyPress={handleTagInputKeyPress}
                    maxLength={10}
                  />
                  <button
                    type="button"
                    className="tag-add-btn"
                    onClick={handleAddTag}
                    disabled={tags.length >= 5 || !tagInput.trim() || tagInput.length > 10}
                  >
                    추가
                  </button>
                </div>
                <div className="tags-list">
                  {tags.map((tag, index) => (
                    <span key={index} className="tag-item">
                      #{tag}
                      <button
                        type="button"
                        className="tag-remove-btn"
                        onClick={() => handleRemoveTag(tag)}
                      >
                        ×
                      </button>
                    </span>
                  ))}
                </div>
              </div>

              {/* Images */}
              <div className="form-group">
                <label className="form-label">이미지 (최대 5개)</label>
                <div className="image-upload-wrapper">
                  <label htmlFor="magazine-image-upload" className="image-upload-label">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    <span>이미지 선택</span>
                  </label>
                  <input
                    type="file"
                    id="magazine-image-upload"
                    className="image-upload-input"
                    accept="image/*"
                    multiple
                    onChange={handleFileSelect}
                    disabled={imageFiles.length >= 5}
                  />
                  {imageFiles.length > 0 && (
                    <span className="image-count">({imageFiles.length} / 5)</span>
                  )}
                </div>
                <div className="images-list">
                  {imagePreviews.map((preview, index) => (
                    <div key={index} className="image-item">
                      <img src={preview} alt={`이미지 ${index + 1}`} className="preview-image" />
                      <button
                        type="button"
                        className="image-remove-btn"
                        onClick={() => handleImageRemove(index)}
                      >
                        삭제
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* Submit Button */}
              <div className="write-actions">
                <button
                  type="submit"
                  className="write-submit-btn"
                  disabled={isSubmitting || !text.trim() || tags.length === 0 || imageFiles.length === 0}
                >
                  {isSubmitting ? '작성 중...' : '작성하기'}
                </button>
              </div>
            </form>
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
