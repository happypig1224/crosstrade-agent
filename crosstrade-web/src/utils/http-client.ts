import axios from 'axios'
import type { Result } from '@/api/types'

const httpClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

httpClient.interceptors.response.use(
  (response) => {
    const data = response.data as Result<unknown>
    if (data && data.code !== 200) {
      const error = new Error(data.message || '请求失败') as Error & { code?: number }
      error.code = data.code
      return Promise.reject(error)
    }
    return response
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请稍后重试'))
    }
    if (!error.response) {
      return Promise.reject(new Error('网络异常，请检查网络连接'))
    }
    return Promise.reject(new Error(error.response.data?.message || '系统繁忙，请稍后重试'))
  },
)

export async function request<T>(method: string, url: string, data?: unknown): Promise<Result<T>> {
  const response = await httpClient.request<Result<T>>({
    method,
    url,
    data,
  })
  return response.data
}

export default httpClient