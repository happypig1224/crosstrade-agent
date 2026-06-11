import { ref, onUnmounted } from 'vue'
import { SSEClient } from '@/services/sse-client'
import { useChatStore } from '@/stores/chat-store'
import type { ChatRequest, ChatResponse } from '@/api/types'

export function useSSEStream() {
  const sseClient = ref(new SSEClient())
  const isConnected = ref(false)
  const chatStore = useChatStore()

  function setupCallbacks() {
    sseClient.value.onToken((event: ChatResponse) => {
      chatStore.appendToken(event.content || '')
    })

    sseClient.value.onDone((event: ChatResponse) => {
      chatStore.finishStream(event)
      isConnected.value = false
    })

    sseClient.value.onError((error: Error) => {
      chatStore.interruptStream()
      isConnected.value = false
      throw error
    })
  }

  async function startStream(request: ChatRequest) {
    sseClient.value.disconnect()
    sseClient.value = new SSEClient()
    setupCallbacks()
    isConnected.value = true
    await sseClient.value.connect('/api/v1/chat/stream', request)
  }

  function stopStream() {
    sseClient.value.disconnect()
    chatStore.interruptStream()
    isConnected.value = false
  }

  onUnmounted(() => {
    sseClient.value.disconnect()
  })

  return {
    startStream,
    stopStream,
    isConnected,
  }
}