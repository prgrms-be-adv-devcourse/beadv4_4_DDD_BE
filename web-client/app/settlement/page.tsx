'use client'

import { useState, useEffect } from 'react'

interface SettlementItem {
  id: number
  orderItemId: number
  sellerMemberId: number
  totalSalesAmount: number
  feeAmount: number
  amount: number
  purchaseConfirmedAt: string
}

interface SettlementData {
  id: number
  totalSalesAmount: number
  feeAmount: number
  amount: number
  payoutAt: string | null
  items: SettlementItem[]
}

interface ApiResponse<T> {
  isSuccess: boolean
  message: string
  result: T
}

type StepStatus = 'pending' | 'active' | 'completed'

interface Step {
  id: number
  title: string
  description: string
  details: string[]
  status: StepStatus
}

export default function SettlementPage() {
  const [currentStep, setCurrentStep] = useState(0)
  const [settlementData, setSettlementData] = useState<SettlementData | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear())
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1)

  const steps: Step[] = [
    {
      id: 1,
      title: '1단계: 기본 멤버 데이터 초기화',
      description: 'SYSTEM, SELLER, BUYER 멤버 생성',
      details: [
        'SYSTEM 멤버 생성 (시스템 수수료 수집용)',
        '판매자(SELLER) 멤버 생성',
        '구매자(BUYER) 멤버 생성',
      ],
      status: 'pending',
    },
    {
      id: 2,
      title: '2단계: 주문 생성 및 구매 확정',
      description: '주문 생성 → 구매 확정 → 정산 후보 항목 생성',
      details: [
        '주문 상품 생성 (3개 상품: 10,000원 + 25,000원 + 5,500원 = 40,500원)',
        '주문 상태 변경: 결제 완료 → 배송 완료 → 구매 확정',
        '정산 후보 항목(SettlementCandidateItem) 생성',
        '총 판매 금액: 40,500원',
      ],
      status: 'pending',
    },
    {
      id: 3,
      title: '3단계: 일별 정산 수집 배치 실행',
      description: 'CandidateItem을 수집하여 Settlement(정산서) 생성',
      details: [
        '일별 배치 실행 (매일 03:00 자동 실행)',
        '정산 후보 항목을 수집하여 정산서 생성',
        '판매자 정산서: 판매대금 = 총액 - 수수료(10%) = 36,450원',
        '시스템 정산서: 수수료 = 총액 * 10% = 4,050원',
      ],
      status: 'pending',
    },
    {
      id: 4,
      title: '4단계: 정산서 기간 조정',
      description: '정산서 기간을 저번달로 조정',
      details: [
        '월별 배치는 저번달 정산서만 처리하므로 기간 조정 필요',
        '정산서의 settlementYear, settlementMonth를 저번달로 변경',
      ],
      status: 'pending',
    },
    {
      id: 5,
      title: '5단계: 월별 정산 완료 배치 실행',
      description: '저번달 정산서에 대해 지급 완료 처리',
      details: [
        '월별 배치 실행 (매월 25일 04:00 자동 실행)',
        '저번달 정산서에 대해 지급 완료 처리',
        'payoutAt 필드에 지급 일시 기록',
      ],
      status: 'pending',
    },
  ]

  const fetchSettlement = async (year: number, month: number) => {
    setIsLoading(true)
    setError(null)

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const accessToken = localStorage.getItem('accessToken')

      if (!accessToken) {
        throw new Error('로그인이 필요합니다.')
      }

      const response = await fetch(`${apiUrl}/api/v1/settlements/${year}/${month}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('API 응답 에러:', response.status, errorText)
        let errorMessage = `정산서 조회 실패 (${response.status})`
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

      const apiResponse: ApiResponse<SettlementData> = await response.json()

      if (apiResponse.isSuccess && apiResponse.result) {
        setSettlementData(apiResponse.result)
      } else {
        throw new Error(apiResponse.message || '정산서를 가져올 수 없습니다.')
      }
    } catch (error) {
      console.error('정산서 조회 실패:', error)
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
      setError(errorMessage)
      setSettlementData(null)
    } finally {
      setIsLoading(false)
    }
  }

  const handleStepClick = (stepIndex: number) => {
    if (stepIndex <= currentStep) {
      setCurrentStep(stepIndex)
    }
  }

  const handleNextStep = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1)
    }
  }

  const handlePrevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1)
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
    }).format(amount)
  }

  const formatDate = (dateString: string | null) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  }

  return (
    <div className="settlement-page">
      <div className="container">
        {/* 헤더 */}
        <div className="settlement-header">
          <h1 className="settlement-title">정산 배치 흐름 시각화</h1>
          <p className="settlement-subtitle">
            SettlementFlowTest의 내용을 기반으로 정산 배치의 전체 흐름을 확인할 수 있습니다.
          </p>
        </div>

        {/* 단계 표시 */}
        <div className="steps-container">
          <div className="steps-timeline">
            {steps.map((step, index) => (
              <div
                key={step.id}
                className={`step-item ${index === currentStep ? 'active' : ''} ${
                  index < currentStep ? 'completed' : ''
                }`}
                onClick={() => handleStepClick(index)}
              >
                <div className="step-number">{step.id}</div>
                <div className="step-content">
                  <h3 className="step-title">{step.title}</h3>
                  <p className="step-description">{step.description}</p>
                </div>
                {index < steps.length - 1 && <div className="step-connector" />}
              </div>
            ))}
          </div>

          {/* 현재 단계 상세 정보 */}
          <div className="step-details-card">
            <div className="step-details-header">
              <h2>{steps[currentStep].title}</h2>
              <span className="step-badge">단계 {currentStep + 1} / {steps.length}</span>
            </div>
            <p className="step-details-description">{steps[currentStep].description}</p>
            <ul className="step-details-list">
              {steps[currentStep].details.map((detail, index) => (
                <li key={index}>{detail}</li>
              ))}
            </ul>
          </div>

          {/* 네비게이션 버튼 */}
          <div className="step-navigation">
            <button
              className="nav-button prev"
              onClick={handlePrevStep}
              disabled={currentStep === 0}
            >
              이전 단계
            </button>
            <button
              className="nav-button next"
              onClick={handleNextStep}
              disabled={currentStep === steps.length - 1}
            >
              다음 단계
            </button>
          </div>
        </div>

        {/* 정산서 조회 섹션 */}
        <div className="settlement-query-section">
          <div className="query-card">
            <h2 className="query-title">정산서 조회</h2>
            <p className="query-description">
              해당 월의 정산서를 조회하여 최종 정산 데이터를 확인할 수 있습니다.
            </p>

            <div className="query-controls">
              <div className="date-selector">
                <label htmlFor="year">년도</label>
                <select
                  id="year"
                  value={selectedYear}
                  onChange={(e) => setSelectedYear(Number(e.target.value))}
                >
                  {Array.from({ length: 5 }, (_, i) => new Date().getFullYear() - i).map((year) => (
                    <option key={year} value={year}>
                      {year}년
                    </option>
                  ))}
                </select>
              </div>

              <div className="date-selector">
                <label htmlFor="month">월</label>
                <select
                  id="month"
                  value={selectedMonth}
                  onChange={(e) => setSelectedMonth(Number(e.target.value))}
                >
                  {Array.from({ length: 12 }, (_, i) => i + 1).map((month) => (
                    <option key={month} value={month}>
                      {month}월
                    </option>
                  ))}
                </select>
              </div>

              <button
                className="query-button"
                onClick={() => fetchSettlement(selectedYear, selectedMonth)}
                disabled={isLoading}
              >
                {isLoading ? '조회 중...' : '정산서 조회'}
              </button>
            </div>

            {error && (
              <div className="error-message">
                <p>{error}</p>
              </div>
            )}

            {settlementData && (
              <div className="settlement-result">
                <div className="settlement-summary">
                  <h3 className="result-title">정산서 요약</h3>
                  <div className="summary-grid">
                    <div className="summary-item">
                      <span className="summary-label">정산서 ID</span>
                      <span className="summary-value">#{settlementData.id}</span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">총 판매 금액</span>
                      <span className="summary-value amount">
                        {formatCurrency(settlementData.totalSalesAmount)}
                      </span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">수수료 (10%)</span>
                      <span className="summary-value fee">
                        {formatCurrency(settlementData.feeAmount)}
                      </span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">정산 금액</span>
                      <span className="summary-value payout">
                        {formatCurrency(settlementData.amount)}
                      </span>
                    </div>
                    <div className="summary-item">
                      <span className="summary-label">지급 일시</span>
                      <span className="summary-value">
                        {formatDate(settlementData.payoutAt)}
                      </span>
                    </div>
                  </div>
                </div>

                {settlementData.items && settlementData.items.length > 0 && (
                  <div className="settlement-items">
                    <h3 className="result-title">정산 항목 상세</h3>
                    <div className="items-table">
                      <div className="table-header">
                        <div className="table-cell">주문 항목 ID</div>
                        <div className="table-cell">총 판매 금액</div>
                        <div className="table-cell">수수료</div>
                        <div className="table-cell">정산 금액</div>
                        <div className="table-cell">구매 확정 일시</div>
                      </div>
                      {settlementData.items.map((item) => (
                        <div key={item.id} className="table-row">
                          <div className="table-cell">#{item.orderItemId}</div>
                          <div className="table-cell amount">
                            {formatCurrency(item.totalSalesAmount)}
                          </div>
                          <div className="table-cell fee">
                            {formatCurrency(item.feeAmount)}
                          </div>
                          <div className="table-cell payout">
                            {formatCurrency(item.amount)}
                          </div>
                          <div className="table-cell">
                            {formatDate(item.purchaseConfirmedAt)}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      <style jsx>{`
        .settlement-page {
          min-height: 100vh;
          background-color: #fafafa;
          padding: 40px 0 80px;
        }

        .container {
          max-width: 1200px;
          margin: 0 auto;
          padding: 0 20px;
        }

        .settlement-header {
          text-align: center;
          margin-bottom: 60px;
        }

        .settlement-title {
          font-size: 36px;
          font-weight: 700;
          color: #1a1a1a;
          margin-bottom: 12px;
        }

        .settlement-subtitle {
          font-size: 16px;
          color: #666;
        }

        .steps-container {
          background-color: #ffffff;
          border-radius: 16px;
          padding: 40px;
          margin-bottom: 40px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .steps-timeline {
          display: flex;
          flex-direction: column;
          gap: 0;
          margin-bottom: 40px;
        }

        .step-item {
          display: flex;
          align-items: flex-start;
          gap: 24px;
          padding: 24px;
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.2s;
          position: relative;
        }

        .step-item:hover {
          background-color: #f9f9f9;
        }

        .step-item.active {
          background-color: #f0f4ff;
          border: 2px solid #667eea;
        }

        .step-item.completed {
          opacity: 0.7;
        }

        .step-number {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          font-weight: 700;
          flex-shrink: 0;
        }

        .step-item.completed .step-number {
          background: #4caf50;
        }

        .step-content {
          flex: 1;
        }

        .step-title {
          font-size: 18px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 8px;
        }

        .step-description {
          font-size: 14px;
          color: #666;
          line-height: 1.6;
        }

        .step-connector {
          position: absolute;
          left: 47px;
          top: 72px;
          width: 2px;
          height: calc(100% - 48px);
          background: linear-gradient(to bottom, #667eea, #e5e5e5);
        }

        .step-item:last-child .step-connector {
          display: none;
        }

        .step-details-card {
          background-color: #f9f9f9;
          border-radius: 12px;
          padding: 32px;
          margin-bottom: 32px;
        }

        .step-details-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
        }

        .step-details-header h2 {
          font-size: 24px;
          font-weight: 700;
          color: #1a1a1a;
        }

        .step-badge {
          padding: 6px 16px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border-radius: 20px;
          font-size: 14px;
          font-weight: 600;
        }

        .step-details-description {
          font-size: 16px;
          color: #666;
          margin-bottom: 20px;
        }

        .step-details-list {
          list-style: none;
          padding: 0;
          margin: 0;
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .step-details-list li {
          padding-left: 24px;
          position: relative;
          font-size: 14px;
          color: #333;
          line-height: 1.6;
        }

        .step-details-list li::before {
          content: '✓';
          position: absolute;
          left: 0;
          color: #667eea;
          font-weight: 700;
        }

        .step-navigation {
          display: flex;
          justify-content: space-between;
          gap: 16px;
        }

        .nav-button {
          flex: 1;
          padding: 14px 24px;
          border: 2px solid #667eea;
          border-radius: 8px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
        }

        .nav-button.next {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        .nav-button.next:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .nav-button.prev {
          background-color: #ffffff;
          color: #667eea;
        }

        .nav-button.prev:hover:not(:disabled) {
          background-color: #f0f4ff;
        }

        .nav-button:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .settlement-query-section {
          background-color: #ffffff;
          border-radius: 16px;
          padding: 40px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .query-card {
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .query-title {
          font-size: 28px;
          font-weight: 700;
          color: #1a1a1a;
        }

        .query-description {
          font-size: 16px;
          color: #666;
        }

        .query-controls {
          display: flex;
          gap: 16px;
          align-items: flex-end;
        }

        .date-selector {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .date-selector label {
          font-size: 14px;
          font-weight: 600;
          color: #333;
        }

        .date-selector select {
          padding: 12px 16px;
          border: 2px solid #e5e5e5;
          border-radius: 8px;
          font-size: 15px;
          background-color: #ffffff;
          color: #333;
          cursor: pointer;
          transition: all 0.2s;
        }

        .date-selector select:focus {
          outline: none;
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .query-button {
          padding: 12px 32px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          border: none;
          border-radius: 8px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;
        }

        .query-button:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .query-button:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .error-message {
          padding: 16px;
          background-color: #ffebee;
          border-left: 4px solid #f44336;
          border-radius: 8px;
          color: #c33;
        }

        .settlement-result {
          margin-top: 32px;
          display: flex;
          flex-direction: column;
          gap: 32px;
        }

        .result-title {
          font-size: 20px;
          font-weight: 600;
          color: #1a1a1a;
          margin-bottom: 20px;
        }

        .settlement-summary {
          background-color: #f9f9f9;
          border-radius: 12px;
          padding: 24px;
        }

        .summary-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 20px;
        }

        .summary-item {
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .summary-label {
          font-size: 14px;
          color: #666;
        }

        .summary-value {
          font-size: 18px;
          font-weight: 600;
          color: #1a1a1a;
        }

        .summary-value.amount {
          color: #667eea;
        }

        .summary-value.fee {
          color: #f44336;
        }

        .summary-value.payout {
          color: #4caf50;
          font-size: 20px;
        }

        .settlement-items {
          background-color: #f9f9f9;
          border-radius: 12px;
          padding: 24px;
        }

        .items-table {
          display: flex;
          flex-direction: column;
          gap: 0;
          border: 1px solid #e5e5e5;
          border-radius: 8px;
          overflow: hidden;
        }

        .table-header {
          display: grid;
          grid-template-columns: 1fr 1.5fr 1.5fr 1.5fr 2fr;
          background-color: #667eea;
          color: white;
          font-weight: 600;
          font-size: 14px;
        }

        .table-row {
          display: grid;
          grid-template-columns: 1fr 1.5fr 1.5fr 1.5fr 2fr;
          background-color: #ffffff;
          border-top: 1px solid #e5e5e5;
        }

        .table-row:hover {
          background-color: #f9f9f9;
        }

        .table-cell {
          padding: 16px;
          font-size: 14px;
          color: #333;
        }

        .table-header .table-cell {
          color: white;
        }

        .table-cell.amount {
          color: #667eea;
          font-weight: 600;
        }

        .table-cell.fee {
          color: #f44336;
          font-weight: 600;
        }

        .table-cell.payout {
          color: #4caf50;
          font-weight: 600;
        }

        @media (max-width: 768px) {
          .settlement-title {
            font-size: 28px;
          }

          .steps-container {
            padding: 24px;
          }

          .step-item {
            flex-direction: column;
            align-items: center;
            text-align: center;
          }

          .step-connector {
            display: none;
          }

          .query-controls {
            flex-direction: column;
            align-items: stretch;
          }

          .summary-grid {
            grid-template-columns: 1fr;
          }

          .table-header,
          .table-row {
            grid-template-columns: 1fr;
            gap: 8px;
          }

          .table-cell {
            padding: 12px;
          }

          .table-header .table-cell {
            font-weight: 700;
            background-color: #667eea;
            padding: 8px 12px;
          }
        }
      `}</style>
    </div>
  )
}
