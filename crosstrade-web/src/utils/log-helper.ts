const SENSITIVE_KEYS = ['sessionId', 'userId', 'token', 'password', 'secret']

export function sanitizeLogData(data: unknown): unknown {
  if (data === null || data === undefined) return data
  if (typeof data === 'string') return data
  if (typeof data === 'number' || typeof data === 'boolean') return data
  if (Array.isArray(data)) return data.map(sanitizeLogData)
  if (typeof data === 'object') {
    const result: Record<string, unknown> = {}
    for (const [key, value] of Object.entries(data as Record<string, unknown>)) {
      if (SENSITIVE_KEYS.some((sk) => key.toLowerCase().includes(sk.toLowerCase()))) {
        result[key] = '***'
      } else {
        result[key] = sanitizeLogData(value)
      }
    }
    return result
  }
  return data
}

export function safeLog(level: 'info' | 'warn' | 'error', message: string, data?: unknown) {
  const sanitizedData = data ? sanitizeLogData(data) : undefined
  switch (level) {
    case 'info':
      console.info(`[CrossTrade] ${message}`, sanitizedData)
      break
    case 'warn':
      console.warn(`[CrossTrade] ${message}`, sanitizedData)
      break
    case 'error':
      console.error(`[CrossTrade] ${message}`, sanitizedData)
      break
  }
}