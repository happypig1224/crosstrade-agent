export interface ChatRequest {
  sessionId: string
  userId: string
  message: string
  region?: string
}

export interface ChatResponse {
  content: string | null
  index: number
  eventType: 'token' | 'done'
  sessionId: string | null
  totalTokens: number
  intentType: string | null
}

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface SessionCreateRequest {
  userId: string
  previousSessionId?: string
}

export interface SessionCreateResponse {
  sessionId: string
}

export interface SessionRecoverRequest {
  sessionId: string
  userId: string
}

export interface SessionRecoverResponse {
  sessionId: string
  recovered: boolean
  messageCount: number
  todoCount: number
}

export interface SessionCloseRequest {
  sessionId: string
}

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  intentType?: string
  totalTokens?: number
  isStreaming?: boolean
  products?: ProductInfo[]
  quote?: QuoteInfo
}

export interface SessionInfo {
  sessionId: string
  userId: string
  title: string
  lastActiveTime: number
  messageCount: number
}

export interface ProductInfo {
  name: string
  description: string
  price: string
  minOrder: string
}

export interface QuoteInfo {
  accepted: boolean
  finalPrice?: string
  minPrice?: string
}

export const INTENT_TYPE_MAP = {
  product_guide: { label: '产品导购', color: '#1890ff' },
  product_info: { label: '产品咨询', color: '#52c41a' },
  quote_negotiate: { label: '报价谈判', color: '#fa8c16' },
  unknown: { label: '未知意图', color: '#8c8c8c' },
} as const

export const ERROR_CODE_MAP = {
  SECURITY_BLOCK: { message: '您的消息包含敏感内容，请重新描述', level: 'error' as const },
  INTENT_FAIL: { message: '抱歉，我暂时无法理解您的问题，请换个方式描述', level: 'warning' as const },
  MEMORY_RECOVER_FAIL: { message: '会话历史暂时无法加载，将以新会话开始', level: 'info' as const },
  SYSTEM_ERROR: { message: '系统繁忙，请稍后重试', level: 'error' as const },
  PARAM_ERROR: { message: '请求参数不合法', level: 'warning' as const },
} as const

export const REGION_OPTIONS = [
  { value: 'US', label: '美国 (US)' },
  { value: 'EU', label: '欧洲 (EU)' },
  { value: 'UK', label: '英国 (UK)' },
  { value: '', label: '默认' },
] as const