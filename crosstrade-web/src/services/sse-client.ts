import { parseSSELines } from '@/utils/sse-parser'
import type { ChatResponse } from '@/api/types'

type TokenHandler = (event: ChatResponse) => void
type DoneHandler = (event: ChatResponse) => void
type ErrorHandler = (error: Error) => void

export class SSEClient {
  private controller: AbortController | null = null
  private reader: ReadableStreamDefaultReader<Uint8Array> | null = null
  private decoder: TextDecoder = new TextDecoder()
  private buffer: string = ''

  private tokenHandlers: TokenHandler[] = []
  private doneHandlers: DoneHandler[] = []
  private errorHandlers: ErrorHandler[] = []

  onToken(handler: TokenHandler): void {
    this.tokenHandlers.push(handler)
  }

  onDone(handler: DoneHandler): void {
    this.doneHandlers.push(handler)
  }

  onError(handler: ErrorHandler): void {
    this.errorHandlers.push(handler)
  }

  async connect(url: string, body: unknown, headers?: Record<string, string>): Promise<void> {
    this.controller = new AbortController()
    this.buffer = ''

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
          ...headers,
        },
        body: JSON.stringify(body),
        signal: this.controller.signal,
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        const message = errorData?.message || `请求失败: ${response.status}`
        throw new Error(message)
      }

      const bodyStream = response.body
      if (!bodyStream) {
        throw new Error('响应体为空')
      }

      this.reader = bodyStream.getReader()

      while (true) {
        const { done, value } = await this.reader.read()
        if (done) break

        const chunk = this.decoder.decode(value, { stream: true })
        this.buffer += chunk

        const { events, remaining } = parseSSELines(this.buffer)
        this.buffer = remaining

        for (const event of events) {
          this.processEvent(event.data)
        }
      }

      if (this.buffer.trim()) {
        const { events } = parseSSELines(this.buffer + '\n\n')
        for (const event of events) {
          this.processEvent(event.data)
        }
        this.buffer = ''
      }
    } catch (error: unknown) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return
      }
      const err = error instanceof Error ? error : new Error(String(error))
      this.errorHandlers.forEach((h) => h(err))
    } finally {
      this.reader = null
    }
  }

  disconnect(): void {
    if (this.reader) {
      this.reader.cancel().catch(() => {})
      this.reader = null
    }
    if (this.controller) {
      this.controller.abort()
      this.controller = null
    }
    this.buffer = ''
  }

  private processEvent(data: string): void {
    try {
      const chatResponse: ChatResponse = JSON.parse(data)
      if (chatResponse.eventType === 'token') {
        this.tokenHandlers.forEach((h) => h(chatResponse))
      } else if (chatResponse.eventType === 'done') {
        this.doneHandlers.forEach((h) => h(chatResponse))
      }
    } catch {
      // ignore parse errors
    }
  }
}