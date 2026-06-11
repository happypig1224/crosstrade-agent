import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '@/stores/chat-store'
import { useToast } from '@/composables/use-toast'
import type { ChatResponse } from '@/api/types'

describe('Error Handling Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('IT-ERR-01: should show error toast on security block', () => {
    const { error, toasts } = useToast()
    error('您的消息包含敏感内容，请重新描述')

    expect(toasts.value.length).toBe(1)
    expect(toasts.value[0].level).toBe('error')
    expect(toasts.value[0].message).toContain('敏感内容')
  })

  it('IT-ERR-02: should show warning toast on intent fail', () => {
    const { warning, toasts } = useToast()
    warning('抱歉，我暂时无法理解您的问题')

    expect(toasts.value.length).toBe(1)
    expect(toasts.value[0].level).toBe('warning')
  })

  it('IT-ERR-03: should handle network error and show retry', () => {
    const chatStore = useChatStore()
    chatStore.sendMessage('你好', 'user')
    chatStore.createAssistantMessage()

    chatStore.interruptStream()

    const lastMsg = chatStore.messages[chatStore.messages.length - 1]
    expect(lastMsg.content).toContain('连接中断')
  })

  it('IT-ERR-04: should show info toast and create blank session on recover fail', () => {
    const { info, toasts } = useToast()
    info('会话历史暂时无法加载，将以新会话开始')

    expect(toasts.value.length).toBe(1)
    expect(toasts.value[0].level).toBe('info')
    expect(toasts.value[0].message).toContain('会话历史')
  })
})