import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true, // 쿠키 전송을 위해 필요
  headers: {
    'Content-Type': 'application/json',
  },
});

// 응답 인터셉터
api.interceptors.response.use(
    (response) => response,
    (error) => {
      // /auths/me 엔드포인트의 401은 정상 케이스이므로 콘솔 에러 출력 안 함
      const isAuthCheckEndpoint = error.config?.url?.includes('/api/v1/auths/me');
      const is401Error = error.response?.status === 401;

      if (isAuthCheckEndpoint && is401Error) {
        // 조용히 reject (콘솔 에러 없음)
        return Promise.reject(error);
      }

      // 다른 에러는 기존대로 콘솔에 출력
      console.error('API Error:', error);
      return Promise.reject(error);
    }
);


export default api;