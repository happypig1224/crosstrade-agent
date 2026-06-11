export interface SSEEvent {
  eventType: string
  data: string
}

export function parseSSELines(buffer: string): { events: SSEEvent[]; remaining: string } {
  const events: SSEEvent[] = []
  let remaining = buffer

  while (true) {
    const doubleNewlineIndex = remaining.indexOf('\n\n')
    if (doubleNewlineIndex === -1) {
      break
    }

    const block = remaining.substring(0, doubleNewlineIndex)
    remaining = remaining.substring(doubleNewlineIndex + 2)

    const lines = block.split('\n')
    let dataLine = ''

    for (const line of lines) {
      if (line.startsWith('data: ')) {
        dataLine += line.substring(6)
      } else if (line.startsWith('data:')) {
        dataLine += line.substring(5)
      }
    }

    if (dataLine.trim() === '') {
      continue
    }

    try {
      const parsed = JSON.parse(dataLine)
      events.push({
        eventType: parsed.eventType || '',
        data: dataLine,
      })
    } catch {
      events.push({
        eventType: '',
        data: dataLine,
      })
    }
  }

  return { events, remaining }
}