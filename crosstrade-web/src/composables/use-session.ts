import { ref } from 'vue'
import { useSessionStore } from '@/stores/session-store'
import { useChatStore } from '@/stores/chat-store'
import { DEFAULT_USER_ID } from '@/constants/app'
import * as storage from '@/utils/storage-helper'

export function useSession() {
  const sessionStore = useSessionStore()
  const chatStore = useChatStore()
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function createSession() {
    loading.value = true
    error.value = null
    try {
      const previousSessionId = sessionStore.currentSessionId || undefined
      chatStore.clearMessages()
      await sessionStore.createSession(DEFAULT_USER_ID, previousSessionId)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '创建会话失败'
    } finally {
      loading.value = false
    }
  }

  async function recoverSession(sessionId: string) {
    loading.value = true
    error.value = null
    try {
      const previousSessionId = sessionStore.switchSession(sessionId)
      chatStore.clearMessages()
      const result = await sessionStore.recoverSession(sessionId, DEFAULT_USER_ID)
      if (previousSessionId && previousSessionId !== sessionId) {
        // previous session cleanup handled by backend
      }
      return result
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '恢复会话失败'
      try {
        chatStore.clearMessages()
        await sessionStore.createSession(DEFAULT_USER_ID)
      } catch {
        // fallback: create blank session
      }
      return null
    } finally {
      loading.value = false
    }
  }

  async function closeSession(sessionId: string) {
    loading.value = true
    error.value = null
    try {
      await sessionStore.closeSession(sessionId)
      if (sessionStore.currentSessionId === sessionId) {
        chatStore.clearMessages()
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '关闭会话失败'
    } finally {
      loading.value = false
    }
  }

  function switchSession(sessionId: string) {
    const previousSessionId = sessionStore.switchSession(sessionId)
    chatStore.clearMessages()
    return previousSessionId
  }

  async function initSession() {
    const userId = storage.get<string>('user_id') || DEFAULT_USER_ID
    storage.set('user_id', userId)

    if (sessionStore.currentSessionId) {
      try {
        await recoverSession(sessionStore.currentSessionId)
      } catch {
        await createSession()
      }
    } else {
      await createSession()
    }
  }

  return {
    loading,
    error,
    createSession,
    recoverSession,
    closeSession,
    switchSession,
    initSession,
  }
}