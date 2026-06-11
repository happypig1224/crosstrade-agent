import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '@/stores/chat-store'
import type { ChatResponse } from '@/api/types'

describe('SSE Stream Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('IT-SSE-01: complete streaming conversation', () => {
    const chatStore = useChatStore()

    chatStore.sendMessage('你好', 'user')
    chatStore.createAssistantMessage()

    const tokenEvents: ChatResponse[] = [
      { content: '你', index: 0, eventType: 'token', sessionId: null, totalTokens: 0, intentType: null },
      { content: '好', index: 1, eventType: 'token', sessionId: null, totalTokens: 0, intentType: null },
      { content: '！', index: 2, eventType: 'token', sessionId: null, totalTokens: 0, intentType: null },
    ]

    for (const event of tokenEvents) {
      chatStore.appendToken(event.content || '')
    }

    const doneEvent: ChatResponse = {
      content: null,
      index: 3,
      eventType: 'done',
      sessionId: 'abc123',
      totalTokens: 50,
      intentType: 'product_guide',
    }
    chatStore.finishStream(doneEvent)

    expect(chatStore.messages.length).toBe(2)
    expect(chatStore.messages[0].role).toBe('user')
    expect(chatStore.messages[1].role).toBe('assistant')
    expect(chatStore.messages[1].content).toBe('你好！')
    expect(chatStore.messages[1].intentType).toBe('product_guide')
    expect(chatStore.messages[1].isStreaming).toBe(false)
  })

  it('IT-SSE-02: should lock during streaming', () => {
    const chatStore = useChatStore()
    chatStore.sendMessage('你好', 'user')
    chatStore.createAssistantMessage()

    expect(chatStore.isStreaming).toBe(true)
    expect(chatStore.canSend).toBe(false)
  })

  it('IT-SSE-03: should unlock after stream completes', () => {
    const chatStore = useChatStore()
    chatStore.sendMessage('你好', 'user')
    chatStore.createAssistantMessage()

    const doneEvent: ChatResponse = {
      content: null, index: 0, eventType: 'done',
      sessionId: 'abc', totalTokens: 10, intentType: 'product_info',
    }
    chatStore.finishStream(doneEvent)

    expect(chatStore.isStreaming).toBe(false)
    expect(chatStore.canSend).toBe(true)
  })

  it('IT-SSE-04: should handle stream interruption', () => {
    const chatStore = useChatStore()
    chatStore.sendMessage('你好', 'user')
    chatStore.createAssistantMessage()
    chatStore.appendToken('正在回复')

    chatStore.interruptStream()

    const lastMsg = chatStore.messages[chatStore.messages.length - 1]
    expect(lastMsg.content).toContain('连接中断，请重试')
    expect(chatStore.isStreaming).toBe(false)
  })
})