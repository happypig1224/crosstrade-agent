import { request } from '@/utils/http-client'
import type { ChatRequest, Result } from '@/api/types'

export function syncChat(chatRequest: ChatRequest): Promise<Result<string>> {
  return request<string>('POST', '/chat/sync', chatRequest)
}