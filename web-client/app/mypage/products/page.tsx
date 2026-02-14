'use client'

import Link from 'next/link'
import {useCallback, useEffect, useState} from 'react'
import MypageLayout from '../../components/MypageLayout'
import api from "@/app/lib/axios";

const CATEGORY_OPTIONS = [
  { value: '', label: '카테고리 전체' },
  { value: 'OUTER', label: '아우터' },
  { value: 'UPPER', label: '상의' },
  { value: 'LOWER', label: '하의' },
  { value: 'CAP', label: '모자' },
  { value: 'SHOES', label: '신발' },
  { value: 'BAG', label: '가방' },
  { value: 'BEAUTY', label: '뷰티' },
]

const PRODUCT_STATUS_OPTIONS = [
  { value: '', label: '등록 상태 전체' },
  { value: 'DRAFT', label: '임시저장' },
  { value: 'COMPLETED', label: '완료' },
  { value: 'CANCELED', label: '취소' },
]

const SALE_STATUS_OPTIONS = [
  { value: '', label: '판매상태 전체' },
  { value: 'SALE', label: '판매중' },
  { value: 'NOT_SALE', label: '판매중지' },
]

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

const PAGE_SIZE = 10
const PAGE_WINDOW = 5
const apiUrl = process.env.NEXT_PUBLIC_PRODUCT_API_URL || ''


export default function ProductsPage() {
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [saleStatus, setSaleStatus] = useState('')
  const [productStatus, setProductStatus] = useState('')
  const [currentPage, setCurrentPage] = useState(0)

  const handleSearch = async () => {
    setCurrentPage(0)

    const params = new URLSearchParams()
    if (keyword) params.append('name', keyword)
    if (category) params.append('category', category)
    if (saleStatus) params.append('saleStatus', saleStatus)
    if (productStatus) params.append('productStatus', productStatus)
    params.append('page', String(currentPage))
    params.append('size', String(PAGE_SIZE))

    try {
      const url = `${apiUrl}/api/v1/products/sellers?${params.toString()}`
      const res = await api.get<ProductsApiResponse>(url)
      const data = res.data
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
  }

  const [products, setProducts] = useState<ProductResponse[]>([])
  const [pagination, setPagination] = useState<Pagination | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  type Option = { value: string; label: string }

  const getLabel = (options: Option[], value?: string) => {
    if (!value) return '-'
    return options.find((opt) => opt.value === value)?.label ?? value
  }

  const fetchProducts = useCallback(async () => {
    if (!apiUrl) {
      setProducts([])
      setPagination(null)
      setIsLoading(false)
      return
    }
    setIsLoading(true)
    setError(null)
    try {

      const url = `${apiUrl}/api/v1/products/sellers?page=${currentPage}&size=${PAGE_SIZE}`
      const res = await api.get<ProductsApiResponse>(url);
      const data: ProductsApiResponse = res.data
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
  }, [currentPage])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  const totalPages = pagination?.totalPages ?? 0

  const currentBlock = Math.floor(currentPage / PAGE_WINDOW)

  const startPage = currentBlock * PAGE_WINDOW
  const endPage = Math.min(
      startPage + PAGE_WINDOW - 1,
      totalPages - 1
  )

  const canGoPrevBlock = startPage > 0
  const canGoNextBlock = endPage < totalPages - 1


  return (
    <MypageLayout>
      <div style={{ maxWidth: '900px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>상품 관리</h1>
            <p style={{ color: '#666', fontSize: '14px' }}>
              등록한 상품을 검색하고 관리할 수 있어요.
            </p>
          </div>
          <Link
            href="/products/create"
            style={{
              display: 'inline-block',
              padding: '10px 20px',
              borderRadius: '8px',
              border: 'none',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              fontSize: '14px',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            상품 등록
          </Link>
        </div>

        {/* 상품 검색 */}
        <div
          style={{
            background: 'white',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            border: '1px solid #f0f0f0',
            marginBottom: '24px',
          }}
        >
          <div style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px', color: '#333' }}>
            상품 검색
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '12px' }}>
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => {
                setKeyword(e.target.value)
                setCurrentPage(0)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                width: '180px',
                minWidth: '120px',
              }}
            />
            <select
              value={category}
              onChange={(e) => {
                setCategory(e.target.value)
                setCurrentPage(0)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                background: 'white',
                minWidth: '120px',
              }}
            >
              {CATEGORY_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <select
              value={saleStatus}
              onChange={(e) => {
                setSaleStatus(e.target.value)
                setCurrentPage(0)
              }}
              style={{
                padding: '8px 12px',
                borderRadius: '8px',
                border: '1px solid #e0e0e0',
                fontSize: '14px',
                background: 'white',
                minWidth: '100px',
              }}
            >
              {SALE_STATUS_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <select
                value={productStatus}
                onChange={(e) => {
                  setProductStatus(e.target.value)
                  setCurrentPage(0)
                }}
                style={{
                  padding: '8px 12px',
                  borderRadius: '8px',
                  border: '1px solid #e0e0e0',
                  fontSize: '14px',
                  background: 'white',
                  minWidth: '100px',
                }}
            >
              {PRODUCT_STATUS_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
              ))}
            </select>
            <button
              type="button"
              onClick={handleSearch}
              style={{
                padding: '8px 20px',
                borderRadius: '8px',
                border: 'none',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                color: 'white',
                fontSize: '14px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              검색
            </button>
          </div>
        </div>

        {/* 테이블 */}
        <div
          style={{
            background: 'white',
            borderRadius: '12px',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            border: '1px solid #f0f0f0',
            overflow: 'hidden',
          }}
        >
          <div style={{ overflowX: 'auto' }}>
            <table
              style={{
                width: '100%',
                borderCollapse: 'collapse',
                fontSize: '14px',
              }}
            >
              <thead>
                <tr style={{ background: '#f8f9fa', borderBottom: '2px solid #eee' }}>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    상품번호
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    상품명
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    카테고리
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    정가
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                    판매가
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    판매상태
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    상태
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'left', fontWeight: 600, color: '#333' }}>
                    등록일시
                  </th>
                  <th style={{ padding: '14px 12px', textAlign: 'center', fontWeight: 600, color: '#333' }}>
                    관리
                  </th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                    <td style={{ padding: '14px 12px', color: '#333'}}>{product.id}</td>
                    <td style={{ padding: '14px 12px', color: '#333', fontWeight: 500 }}>{product.name}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center', color: '#666' }}>
                      {getLabel(CATEGORY_OPTIONS, product.category)}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                      {product.price != null
                          ? `₩${product.price.toLocaleString()}`
                          : ''}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'right', fontWeight: 600, color: '#333' }}>
                      {product.salePrice != null
                          ? `₩${product.salePrice.toLocaleString()}`
                          : ''}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      {getLabel(SALE_STATUS_OPTIONS, product.saleStatus)}
                    </td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      {getLabel(PRODUCT_STATUS_OPTIONS, product.productStatus)}
                    </td>
                    <td style={{ padding: '14px 12px', color: '#666' }}>{product.createdAt}</td>
                    <td style={{ padding: '14px 12px', textAlign: 'center' }}>
                      <Link
                        href={`/mypage/products/${product.id}/edit`}
                        style={{
                          fontSize: '13px',
                          color: '#667eea',
                          fontWeight: 500,
                          textDecoration: 'none',
                        }}
                      >
                        상세보기
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* 페이징 */}
          {products.length > 0 && totalPages > 1 && (
              <div
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '16px',
                    borderTop: '1px solid #f0f0f0',
                    flexWrap: 'wrap',
                  }}
              >
                {/* 이전 블록 */}
                <button
                    type="button"
                    onClick={() =>
                        setCurrentPage(Math.max(0, startPage - PAGE_WINDOW))
                    }
                    disabled={!canGoPrevBlock}
                    style={{
                      padding: '8px 12px',
                      borderRadius: '8px',
                      border: '1px solid #e0e0e0',
                      background: canGoPrevBlock ? '#fff' : '#f5f5f5',
                      color: canGoPrevBlock ? '#333' : '#999',
                      fontSize: '14px',
                      cursor: canGoPrevBlock ? 'pointer' : 'not-allowed',
                      fontWeight: 500,
                    }}
                >
                  이전
                </button>

                {/* 페이지 번호 (블록 단위) */}
                {Array.from(
                    { length: endPage - startPage + 1 },
                    (_, i) => {
                      const pageIndex = startPage + i
                      const pageNumber = pageIndex + 1
                      const isActive = currentPage === pageIndex

                      return (
                          <button
                              key={pageNumber}
                              type="button"
                              onClick={() => setCurrentPage(pageIndex)}
                              style={{
                                minWidth: '36px',
                                padding: '8px',
                                borderRadius: '8px',
                                border: isActive
                                    ? '2px solid #667eea'
                                    : '1px solid #e0e0e0',
                                background: isActive ? '#f8f8ff' : '#fff',
                                color: isActive ? '#667eea' : '#333',
                                fontSize: '14px',
                                fontWeight: isActive ? 600 : 400,
                                cursor: 'pointer',
                              }}
                          >
                            {pageNumber}
                          </button>
                      )
                    }
                )}

                {/* 다음 블록 */}
                <button
                    type="button"
                    onClick={() =>
                        setCurrentPage(
                            Math.min(totalPages - 1, startPage + PAGE_WINDOW)
                        )
                    }
                    disabled={!canGoNextBlock}
                    style={{
                      padding: '8px 12px',
                      borderRadius: '8px',
                      border: '1px solid #e0e0e0',
                      background: canGoNextBlock ? '#fff' : '#f5f5f5',
                      color: canGoNextBlock ? '#333' : '#999',
                      fontSize: '14px',
                      cursor: canGoNextBlock ? 'pointer' : 'not-allowed',
                      fontWeight: 500,
                    }}
                >
                  다음
                </button>
              </div>
          )}


          {products.length === 0 && (
            <div
              style={{
                padding: '48px 24px',
                textAlign: 'center',
                color: '#999',
                fontSize: '14px',
              }}
            >
              검색 조건에 맞는 상품이 없습니다.
            </div>
          )}
        </div>
      </div>
    </MypageLayout>
  )
}
