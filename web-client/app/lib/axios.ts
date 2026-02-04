import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true, // 모든 요청에 쿠키(httpOnly)를 자동으로 포함
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;