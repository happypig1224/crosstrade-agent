import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { nanoid } from 'nanoid'
import type { Message, ChatResponse } from '@/api/types'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<Message[]>([])
  const isStreaming = ref(false)
  const currentStreamContent = ref('')
  const currentIntentType = ref('')
  const currentTokenCount = ref(0)

  const canSend = computed(() => !isStreaming.value)
  const lastMessage = computed(() => messages.value[messages.value.length - 1] || null)

  function sendMessage(text: string, role: 'user' | 'assistant' = 'user') {
    const message: Message = {
      id: nanoid(),
      role,
      content: text,
      timestamp: Date.now(),
    }
    messages.value.push(message)
    return message
  }

  function createAssistantMessage(): Message {
    const message: Message = {
      id: nanoid(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true,
    }
    messages.value.push(message)
    isStreaming.value = true
    currentStreamContent.value = ''
    currentIntentType.value = ''
    currentTokenCount.value = 0
    return message
  }

  function appendToken(content: string) {
    currentStreamContent.value += content
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant' && last.isStreaming) {
      last.content = currentStreamContent.value
    }
  }

  function finishStream(done: ChatResponse) {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.isStreaming = false
      last.intentType = done.intentType || undefined
      last.totalTokens = done.totalTokens || undefined
    }
    isStreaming.value = false
    currentIntentType.value = done.intentType || ''
    currentTokenCount.value = done.totalTokens || 0
  }

  function interruptStream() {
    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant' && last.isStreaming) {
      last.isStreaming = false
      last.content += '\n\n[连接中断，请重试]'
    }
    isStreaming.value = false
  }

  function clearMessages() {
    messages.value = []
    isStreaming.value = false
    currentStreamContent.value = ''
    currentIntentType.value = ''
    currentTokenCount.value = 0
  }

  function setMessages(msgs: Message[]) {
    messages.value = msgs
  }

  return {
    messages,
    isStreaming,
    currentStreamContent,
    currentIntentType,
    currentTokenCount,
    canSend,
    lastMessage,
    sendMessage,
    createAssistantMessage,
    appendToken,
    finishStream,
    interruptStream,
    clearMessages,
    setMessages,
  }
})