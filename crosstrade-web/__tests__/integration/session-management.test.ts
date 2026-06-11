import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useSessionStore } from '@/stores/session-store'
import * as sessionApi from '@/api/session'

vi.mock('@/api/session', () => ({
  createSession: vi.fn(),
  recoverSession: vi.fn(),
  closeSession: vi.fn(),
}))

describe('Session Management Integration', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('IT-SES-01: should create session and update store', async () => {
    vi.mocked(sessionApi.createSession).mockResolvedValue({
      code: 200,
      message: 'success',
      data: { sessionId: 'abc123' },
    })

    const store = useSessionStore()
    const session = await store.createSession('user1')

    expect(session!.sessionId).toBe('abc123')
    expect(store.currentSessionId).toBe('abc123')
    expect(store.sessions.length).toBe(1)
  })

  it('IT-SES-02: should recover session with message count', async () => {
    vi.mocked(sessionApi.recoverSession).mockResolvedValue({
      code: 200,
      message: 'success',
      data: { sessionId: 'abc', recovered: true, messageCount: 5, todoCount: 2 },
    })

    const store = useSessionStore()
    const result = await store.recoverSession('abc', 'user1')

    expect(result.recovered).toBe(true)
    expect(result.messageCount).toBe(5)
    expect(store.currentSessionId).toBe('abc')
  })

  it('IT-SES-03: should close session and remove from list', async () => {
    vi.mocked(sessionApi.createSession).mockResolvedValue({
      code: 200, message: 'success', data: { sessionId: 'abc123' },
    })
    vi.mocked(sessionApi.closeSession).mockResolvedValue({
      code: 200, message: 'success', data: null as any,
    })

    const store = useSessionStore()
    await store.createSession('user1')
    expect(store.sessions.length).toBe(1)

    await store.closeSession('abc123')
    expect(store.sessions.length).toBe(0)
  })

  it('IT-SES-04: should pass previousSessionId when switching', async () => {
    vi.mocked(sessionApi.createSession)
      .mockResolvedValueOnce({ code: 200, message: 'success', data: { sessionId: 'session_a' } })
      .mockResolvedValueOnce({ code: 200, message: 'success', data: { sessionId: 'session_b' } })

    const store = useSessionStore()
    await store.createSession('user1')
    await store.createSession('user1', 'session_a')

    expect(sessionApi.createSession).toHaveBeenLastCalledWith({
      userId: 'user1',
      previousSessionId: 'session_a',
    })
  })

  it('IT-SES-05: should persist sessions to localStorage', async () => {
    vi.mocked(sessionApi.createSession).mockResolvedValue({
      code: 200, message: 'success', data: { sessionId: 'abc123' },
    })

    const store = useSessionStore()
    await store.createSession('user1')

    const stored = localStorage.getItem('crosstrade_session_list')
    expect(stored).toBeTruthy()
    const parsed = JSON.parse(stored!)
    expect(parsed.length).toBe(1)
    expect(parsed[0].sessionId).toBe('abc123')
  })
})