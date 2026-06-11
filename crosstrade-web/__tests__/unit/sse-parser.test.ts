import { describe, it, expect } from 'vitest'
import { parseSSELines } from '@/utils/sse-parser'

describe('SSE Parser', () => {
  it('UT-SSE-01: should parse complete token event', () => {
    const input = 'data:{"content":"你","index":0,"eventType":"token"}\n\n'
    const { events, remaining } = parseSSELines(input)
    expect(events).toHaveLength(1)
    expect(events[0].eventType).toBe('token')
    const data = JSON.parse(events[0].data)
    expect(data.content).toBe('你')
    expect(data.index).toBe(0)
    expect(remaining).toBe('')
  })

  it('UT-SSE-02: should parse complete done event', () => {
    const input = 'data:{"eventType":"done","sessionId":"abc","totalTokens":100,"intentType":"product_guide"}\n\n'
    const { events } = parseSSELines(input)
    expect(events).toHaveLength(1)
    const data = JSON.parse(events[0].data)
    expect(data.eventType).toBe('done')
    expect(data.sessionId).toBe('abc')
    expect(data.totalTokens).toBe(100)
    expect(data.intentType).toBe('product_guide')
  })

  it('UT-SSE-03: should parse multiple consecutive events', () => {
    const input = 'data:{"content":"你","eventType":"token"}\n\ndata:{"content":"好","eventType":"token"}\n\n'
    const { events } = parseSSELines(input)
    expect(events).toHaveLength(2)
    expect(JSON.parse(events[0].data).content).toBe('你')
    expect(JSON.parse(events[1].data).content).toBe('好')
  })

  it('UT-SSE-04: should buffer incomplete lines', () => {
    const input = 'data:{"content":"你'
    const { events, remaining } = parseSSELines(input)
    expect(events).toHaveLength(0)
    expect(remaining).toBe('data:{"content":"你')
  })

  it('UT-SSE-05: should handle chunked arrival', () => {
    const chunk1 = 'data:{"cont'
    const { events: events1, remaining: remaining1 } = parseSSELines(chunk1)
    expect(events1).toHaveLength(0)

    const chunk2 = remaining1 + 'ent":"你"}\n\n'
    const { events: events2 } = parseSSELines(chunk2)
    expect(events2).toHaveLength(1)
    expect(JSON.parse(events2[0].data).content).toBe('你')
  })

  it('UT-SSE-06: should ignore empty data lines', () => {
    const input = 'data:\n\n'
    const { events } = parseSSELines(input)
    expect(events).toHaveLength(0)
  })
})