'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState, useEffect } from 'react'
import Header from '../../components/Header'

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

type PresignedUrlApiResult = { presignedUrl: string; key: string }
type PublicUrlApiResult = { imageUrl: string; key: string }
interface PresignedUrlApiResponse {
  isSuccess: boolean
  result: PresignedUrlApiResult
}
interface PublicUrlApiResponse {
  isSuccess: boolean
  result: PublicUrlApiResult
}

function getExtFromFile(file: File): string {
  const name = file.name
  const parts = name.split('.')
  if (parts.length > 1) return parts[parts.length - 1].toLowerCase()
  const mime = file.type
  if (mime === 'image/jpeg' || mime === 'image/jpg') return 'jpg'
  if (mime === 'image/png') return 'png'
  if (mime === 'image/gif') return 'gif'
  if (mime === 'image/webp') return 'webp'
  return 'jpg'
}

export default function MagazineWritePage() {
  const router = useRouter()
  const [text, setText] = useState('')
  const [tags, setTags] = useState<string[]>([])
  const [tagInput, setTagInput] = useState('')
  const [imageFiles, setImageFiles] = useState<File[]>([])
  const [imagePreviews, setImagePreviews] = useState<string[]>([])
  const [uploadedImageUrls, setUploadedImageUrls] = useState<string[]>([])
  const [uploadingImageIndex, setUploadingImageIndex] = useState<Set<number>>(new Set())
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isAuthChecked, setIsAuthChecked] = useState(false)

  const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (!token?.trim()) {
      router.replace('/login')
      return
    }
    setIsAuthChecked(true)
  }, [router])

  const uploadImageToS3 = async (file: File): Promise<string> => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL!
    const ext = getExtFromFile(file)

    const presignedRes = await fetch(`${apiUrl}/api/v1/files/presigned-url`, {
      method: 'POST',
      headers: {
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        domainType: 'CONTENT',
        ext,
        contentType: file.type,
      }),
    })
    if (!presignedRes.ok) throw new Error('Presigned URL 발급 실패')
    const presignedData: PresignedUrlApiResponse = await presignedRes.json()

    const uploadRes = await fetch(presignedData.result.presignedUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type },
      body: file,
    })
    if (!uploadRes.ok) throw new Error('S3 업로드 실패')

    const publicRes = await fetch(`${apiUrl}/api/v1/files/public-url`, {
      method: 'POST',
      headers: {
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        rawKey: presignedData.result.key,
        domainType: 'CONTENT',
        contentType: file.type,
      }),
    })
    if (!publicRes.ok) throw new Error('Public URL 발급 실패')
    const publicData: PublicUrlApiResponse = await publicRes.json()
    return publicData.result.imageUrl
  }

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

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files
    if (!files) return

    const newFiles: File[] = []
    Array.from(files).forEach((file) => {
      if (!file.type.startsWith('image/')) {
        alert(`${file.name}은(는) 이미지 파일이 아닙니다.`)
        return
      }
      if (imageFiles.length + newFiles.length >= 5) {
        alert('이미지는 최대 5개까지 추가할 수 있습니다.')
        return
      }
      newFiles.push(file)
      const reader = new FileReader()
      reader.onloadend = () => {
        const result = reader.result as string
        setImagePreviews(prev => [...prev, result])
      }
      reader.readAsDataURL(file)
    })

    if (newFiles.length === 0) return

    const startIndex = imageFiles.length
    setImageFiles(prev => [...prev, ...newFiles])
    setUploadedImageUrls(prev => [...prev, ...newFiles.map(() => '')])
    newFiles.forEach((file, i) => {
      const uploadIndex = startIndex + i
      setUploadingImageIndex(prev => new Set(prev).add(uploadIndex))
      uploadImageToS3(file)
        .then((url) => {
          setUploadedImageUrls(prev => {
            const next = [...prev]
            next[uploadIndex] = url
            return next
          })
        })
        .catch(() => {
          alert(`이미지 업로드 실패: ${file.name}`)
          handleImageRemove(uploadIndex)
        })
        .finally(() => {
          setUploadingImageIndex(prev => {
            const s = new Set(prev)
            s.delete(uploadIndex)
            return s
          })
        })
    })

    e.target.value = ''
  }

  const handleImageRemove = (index: number) => {
    setImageFiles(prev => prev.filter((_, i) => i !== index))
    setImagePreviews(prev => prev.filter((_, i) => i !== index))
    setUploadedImageUrls(prev => prev.filter((_, i) => i !== index))
    setUploadingImageIndex(prev => {
      const next = new Set<number>()
      prev.forEach(i => {
        if (i === index) return
        next.add(i > index ? i - 1 : i)
      })
      return next
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
    if (uploadingImageIndex.size > 0) {
      alert('이미지 업로드가 완료될 때까지 기다려주세요.')
      return
    }
    const allUploaded =
      uploadedImageUrls.length === imageFiles.length &&
      uploadedImageUrls.every((url) => url !== '')
    if (!allUploaded) {
      alert('이미지 업로드가 완료되지 않았습니다. 잠시 후 다시 시도해주세요.')
      return
    }

    setIsSubmitting(true)
    const apiUrl = process.env.NEXT_PUBLIC_API_URL
    if (!apiUrl) {
      alert('API URL이 설정되지 않았습니다.')
      setIsSubmitting(false)
      return
    }

    try {
      const body: ContentRequest = {
        text: text.trim(),
        tags,
        images: uploadedImageUrls.map((imageUrl, i) => ({
          imageUrl,
          isPrimary: i === 0,
          sortOrder: i,
        })),
      }
      const res = await fetch(`${apiUrl}/api/v1/contents`, {
        method: 'POST',
        headers: {
          ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      })

      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        const msg = (err as { message?: string }).message ?? '콘텐츠 작성에 실패했습니다.'
        throw new Error(msg)
      }
      const data: ApiResponse = await res.json()
      if (data.isSuccess && data.result?.contentId) {
        router.push(`/magazine/${data.result.contentId}`)
      } else {
        router.push('/magazine')
      }
    } catch (error) {
      console.error('콘텐츠 생성 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '콘텐츠 생성 중 오류가 발생했습니다.'
      alert(errorMessage)
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!isAuthChecked) {
    return (
      <div className="home-page">
        <Header />
        <div style={{ padding: '40px 20px', textAlign: 'center', minHeight: '60vh' }}>
          로딩 중...
        </div>
      </div>
    )
  }

  return (
    <div className="home-page">
      <Header />

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
                      {uploadingImageIndex.has(index) && (
                        <span className="image-uploading-badge">업로드 중</span>
                      )}
                      <button
                        type="button"
                        className="image-remove-btn"
                        onClick={() => handleImageRemove(index)}
                        disabled={uploadingImageIndex.has(index)}
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
                  disabled={
                    isSubmitting ||
                    !text.trim() ||
                    tags.length === 0 ||
                    imageFiles.length === 0 ||
                    uploadingImageIndex.size > 0 ||
                    uploadedImageUrls.length !== imageFiles.length ||
                    uploadedImageUrls.some((u) => !u)
                  }
                >
                  {isSubmitting ? '작성 중...' : uploadingImageIndex.size > 0 ? '이미지 업로드 중...' : '작성하기'}
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
