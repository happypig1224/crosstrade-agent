import { computed, ref } from 'vue'
import { useChatStore } from '@/stores/chat-store'
import { useSessionStore } from '@/stores/session-store'
import { useSSEStream } from './use-sse-stream'
import { DEFAULT_USER_ID } from '@/constants/app'
import type { ChatRequest } from '@/api/types'

export function useChat() {
  const chatStore = useChatStore()
  const sessionStore = useSessionStore()
  const { startStream, stopStream } = useSSEStream()
  const inputText = ref('')
  const error = ref<string | null>(null)

  const canSend = computed(() => {
    return !chatStore.isStreaming && inputText.value.trim().length > 0
  })

  const isStreaming = computed(() => chatStore.isStreaming)

  async function sendMessage(text?: string) {
    const messageText = (text || inputText.value).trim()
    if (!messageText || chatStore.isStreaming) return

    error.value = null
    inputText.value = ''

    chatStore.sendMessage(messageText, 'user')
    chatStore.createAssistantMessage()

    if (!sessionStore.currentSessionId) {
      try {
        await sessionStore.createSession(DEFAULT_USER_ID)
      } catch (e: unknown) {
        error.value = e instanceof Error ? e.message : '创建会话失败'
        chatStore.interruptStream()
        return
      }
    }

    if (chatStore.messages.length === 2) {
      sessionStore.updateSessionTitle(sessionStore.currentSessionId, messageText)
    }

    const request: ChatRequest = {
      sessionId: sessionStore.currentSessionId,
      userId: DEFAULT_USER_ID,
      message: messageText,
      region: sessionStore.region || undefined,
    }

    try {
      await startStream(request)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '发送失败'
    }

    sessionStore.updateSessionMessageCount(
      sessionStore.currentSessionId,
      chatStore.messages.length,
    )
  }

  function handleKeydown(e: KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return {
    inputText,
    canSend,
    isStreaming,
    error,
    sendMessage,
    stopStream,
    handleKeydown,
  }
}