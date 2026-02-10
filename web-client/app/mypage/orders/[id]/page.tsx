'use client'

import Link from 'next/link'
import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import MypageLayout from '../../../components/MypageLayout'
import api from '@/app/lib/axios'
interface OrderItem {
  id: number
  productId: number
  productName: string
  quantity: number
  salePrice: number
}

interface OrderDetail {
  memberId: number
  orderId: number
  orderNo: string
  status: string // "PENDING_PAYMENT"
  totalAmount: number
  createdAt : string
  // 상세 정보에 필요한 추가 필드들
  recipientName: string
  recipientPhone: string
  zipCode: string
  address: string
  addressDetail: string
  orderItems: OrderItem[]
  // 결제 정보 등 추가 가능
}

interface OrderDetailResponse {
  isSuccess: boolean
  code: string
  message: string
  result: OrderDetail
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'ORDER_RECEIVED': return { label: '주문접수', color: '#666' }
    case 'PENDING_PAYMENT': return { label: '결제대기', color: '#f59e0b' }
    case 'PAYMENT_COMPLETED': return { label: '결제완료', color: '#22c55e' }
    case 'PREPARING_PRODUCT': return { label: '상품준비중', color: '#3b82f6' }
    case 'SHIPPING': return { label: '배송중', color: '#667eea' }
    case 'SHIPPING_COMPLETED': return { label: '배송완료', color: '#22c55e' }
    case 'CANCEL_REQUESTED' : return { label: "취소 요청", color: '#ef7777'}
    case 'CANCELED': return { label: '주문취소', color: '#ef4444' }
    default: return { label: status, color: '#999' }
  }
}

const formatMoney = (amount: number) => new Intl.NumberFormat('ko-KR').format(amount) + '원'

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', padding: '12px 0', borderBottom: '1px solid #f0f0f0', gap: '16px' }}>
      <span style={{ minWidth: '120px', color: '#666', fontSize: '14px' }}>{label}</span>
      <span style={{ color: '#333', fontSize: '14px' }}>{value}</span>
    </div>
  )
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
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
      <h2 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '16px', color: '#333' }}>{title}</h2>
      {children}
    </div>
  )
}
export default function OrderDetailPage() {
  const params = useParams()
  const router = useRouter()
  // params.id는 파일명이 [id]인 경우 string 혹은 string[]일 수 있음
  const orderId = typeof params?.id === 'string' ? params.id : ''

  const [order, setOrder] = useState<OrderDetail | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // API 호출
  useEffect(() => {
    if (!orderId) return

    const fetchOrderDetail = async () => {
      try {
        const res = await api.get<OrderDetailResponse>(`/api/v1/orders/${orderId}`)
        if (res.data.isSuccess && res.data.result) {
          setOrder(res.data.result)
        } else {
          // 실패 처리
          setOrder(null)
        }
      } catch (error) {
        console.error('상세 조회 실패:', error)
        setOrder(null)
      } finally {
        setIsLoading(false)
      }
    }
    fetchOrderDetail()
  }, [orderId])

  // --- 취소 핸들러 ---
  const handleCancel = async () => {
    if (!order) return
    if (!confirm('정말 주문을 취소하시겠습니까?')) return

    try {
      await api.post(`/api/v1/orders/${order.orderId}/cancel`)
      alert('취소 요청이 접수되었습니다.')
    } catch (e) {
      alert('취소 요청 실패')
    }
  }

  // --- 환불 핸들러 ---
  const handleRefund = async () => {
    if (!order) return
    if (!confirm('정말 주문을 환불하시겠습니까?')) return

    try {
      await api.post(`/api/v1/orders/${order.orderId}/refund`)
      alert('환불 요청이 접수되었습니다.')
    } catch (e) {
      alert('환불 요청 실패')
    }
  }

  if (isLoading) {
    return (
        <MypageLayout>
          <div style={{ padding: '40px', textAlign: 'center', color: '#999' }}>로딩 중...</div>
        </MypageLayout>
    )
  }

  if (!order) {
    return (
        <MypageLayout>
          <div style={{ maxWidth: '600px', padding: '40px 0' }}>
            <p style={{ color: '#999', marginBottom: '16px' }}>주문 정보를 찾을 수 없습니다.</p>
            <Link href="/mypage/orders" style={{ display: 'inline-block', padding: '10px 20px', borderRadius: '8px', background: '#667eea', color: 'white', textDecoration: 'none' }}>
              목록으로 돌아가기
            </Link>
          </div>
        </MypageLayout>
    )
  }

  const statusInfo = getStatusLabel(order.status)

  return (
      <MypageLayout>
        <div style={{ maxWidth: '700px' }}>
          <div style={{ marginBottom: '24px' }}>
            <Link href="/mypage/orders" style={{ fontSize: '14px', color: '#667eea', textDecoration: 'none', marginBottom: '8px', display: 'inline-block' }}>
              ← 목록으로
            </Link>
            <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '8px' }}>주문 상세</h1>
            <p style={{ color: '#666', fontSize: '14px' }}>주문번호: {order.orderNo}</p>
          </div>

          {/* 주문 정보 카드 */}
          <Card title="주문 정보">
            <DetailRow label="주문일시" value={order.createdAt ? new Date(order.createdAt).toLocaleString() : '-'} />
            <DetailRow label="주문번호" value={order.orderNo} />
            <DetailRow label="배송상태" value={<span style={{ color: statusInfo.color, fontWeight: 600 }}>{statusInfo.label}</span>} />
            <DetailRow label="결제금액" value={<strong style={{ fontSize: '16px' }}>{formatMoney(order.totalAmount)}</strong>} />
          </Card>

          {/* 주문 상품 카드 */}
          <Card title="주문 상품">
            {order.orderItems?.map((item, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid #f0f0f0' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ color: '#333', fontWeight: 500 }}>{item.productName}</div>
                    <div style={{ color: '#888', fontSize: '13px', marginTop: '4px' }}>
                      {formatMoney(item.salePrice)} x {item.quantity}개
                    </div>
                  </div>
                  <div style={{ fontWeight: 600, color: '#333' }}>
                    {formatMoney(item.salePrice * item.quantity)}
                  </div>
                </div>
            ))}
            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '16px 0 0', fontWeight: 700, fontSize: '16px', color: '#333' }}>
              <span>총 결제금액</span>
              <span>{formatMoney(order.totalAmount)}</span>
            </div>
          </Card>

          {/* 배송 정보 카드 */}
          <Card title="배송 정보">
            <DetailRow label="받는 분" value={order.recipientName} />
            <DetailRow label="연락처" value={order.recipientPhone} />
            <DetailRow label="주소" value={`(${order.zipCode}) ${order.address} ${order.addressDetail}`} />
          </Card>

          {/* 하단 버튼 */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px', marginTop: '24px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', gap: '12px' }}>
              {/* 주문 상태에 따라 취소 버튼 노출 여부 결정 (예: 배송 전까지만 취소 가능) */}
              {(order.status === 'PENDING_PAYMENT' || order.status === 'ORDER_RECEIVED') && (
                  <button
                      type="button"
                      onClick={handleCancel}
                      style={{ padding: '10px 20px', borderRadius: '8px', border: '1px solid #e0e0e0', background: 'white', color: '#333', fontSize: '14px', fontWeight: 500, cursor: 'pointer' }}
                  >
                    주문 취소
                  </button>
              )}
            </div>
              {/* 필요시 반품/교환 버튼 */}
              <div style={{ display: 'flex', gap: '12px' }}>
                  {/* 주문 상태에 따라 취소 버튼 노출 여부 결정 (예: 배송 전까지만 취소 가능) */}
                  {(order.status === 'SHIPPING' || order.status === 'DELIVERED') && (
                      <button
                          type="button"
                          onClick={handleRefund}
                          style={{ padding: '10px 20px', borderRadius: '8px', border: '1px solid #e0e0e0', background: 'white', color: '#333', fontSize: '14px', fontWeight: 500, cursor: 'pointer' }}
                      >
                        환불 요청
                      </button>
                  )}
                </div>
                <button
                    onClick={() => router.push('/mypage/orders')}
                    style={{ padding: '10px 20px', borderRadius: '8px', background: '#333', color: 'white', fontSize: '14px', fontWeight: 500, border: 'none', cursor: 'pointer' }}
                >
                  목록으로
                </button>
              </div>
            </div>
      </MypageLayout>
)
}
