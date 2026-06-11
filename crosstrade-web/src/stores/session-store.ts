import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { SessionInfo } from '@/api/types'
import * as sessionApi from '@/api/session'
import * as storage from '@/utils/storage-helper'

export const useSessionStore = defineStore('session', () => {
  const sessions = ref<SessionInfo[]>(storage.get<SessionInfo[]>('session_list') || [])
  const currentSessionId = ref<string>(storage.get<string>('current_sid') || '')
  const region = ref<string>(storage.get<string>('region') || '')

  const currentSession = computed(() =>
    sessions.value.find((s) => s.sessionId === currentSessionId.value) || null,
  )

  function persistSessions() {
    storage.set('session_list', sessions.value)
  }

  function persistCurrentSessionId() {
    storage.set('current_sid', currentSessionId.value)
  }

  function persistRegion() {
    storage.set('region', region.value)
  }

  async function createSession(userId: string, previousSessionId?: string) {
    const result = await sessionApi.createSession({ userId, previousSessionId })
    if (result.code === 200 && result.data) {
      const newSession: SessionInfo = {
        sessionId: result.data.sessionId,
        userId,
        title: '新会话',
        lastActiveTime: Date.now(),
        messageCount: 0,
      }
      sessions.value.unshift(newSession)
      currentSessionId.value = result.data.sessionId
      persistSessions()
      persistCurrentSessionId()
      return newSession
    }
    throw new Error(result.message || '创建会话失败')
  }

  async function recoverSession(sessionId: string, userId: string) {
    const result = await sessionApi.recoverSession({ sessionId, userId })
    if (result.code === 200 && result.data) {
      currentSessionId.value = sessionId
      persistCurrentSessionId()
      return result.data
    }
    throw new Error(result.message || '恢复会话失败')
  }

  async function closeSession(sessionId: string) {
    const result = await sessionApi.closeSession({ sessionId })
    if (result.code === 200) {
      sessions.value = sessions.value.filter((s) => s.sessionId !== sessionId)
      if (currentSessionId.value === sessionId) {
        currentSessionId.value = sessions.value.length > 0 ? sessions.value[0].sessionId : ''
        persistCurrentSessionId()
      }
      persistSessions()
      return
    }
    throw new Error(result.message || '关闭会话失败')
  }

  function switchSession(sessionId: string) {
    const previousId = currentSessionId.value
    currentSessionId.value = sessionId
    persistCurrentSessionId()
    return previousId
  }

  function setRegion(value: string) {
    region.value = value
    persistRegion()
  }

  function updateSessionTitle(sessionId: string, title: string) {
    const session = sessions.value.find((s) => s.sessionId === sessionId)
    if (session) {
      session.title = title.substring(0, 20)
      persistSessions()
    }
  }

  function updateSessionMessageCount(sessionId: string, count: number) {
    const session = sessions.value.find((s) => s.sessionId === sessionId)
    if (session) {
      session.messageCount = count
      session.lastActiveTime = Date.now()
      persistSessions()
    }
  }

  return {
    sessions,
    currentSessionId,
    region,
    currentSession,
    createSession,
    recoverSession,
    closeSession,
    switchSession,
    setRegion,
    updateSessionTitle,
    updateSessionMessageCount,
  }
})