import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import type { Message } from '@/api/types'

function createMessage(overrides: Partial<Message> = {}): Message {
  return {
    id: 'test-id',
    role: 'user',
    content: '你好',
    timestamp: new Date(2024, 0, 1, 14, 30).getTime(),
    ...overrides,
  }
}

describe('MessageBubble', () => {
  it('CT-MB-01: should render user message with right alignment and blue background', () => {
    const msg = createMessage({ role: 'user', content: '你好' })
    const wrapper = mount(MessageBubble, { props: { message: msg } })
    expect(wrapper.classes()).toContain('justify-end')
    expect(wrapper.text()).toContain('你好')
    expect(wrapper.find('.bg-primary').exists()).toBe(true)
  })

  it('CT-MB-02: should render AI message with left alignment', () => {
    const msg = createMessage({
      role: 'assistant',
      content: '回复',
      intentType: 'product_guide',
    })
    const wrapper = mount(MessageBubble, { props: { message: msg } })
    expect(wrapper.classes()).toContain('justify-start')
    expect(wrapper.text()).toContain('产品导购')
  })

  it('CT-MB-03: should show streaming cursor animation', () => {
    const msg = createMessage({ role: 'assistant', content: '正在', isStreaming: true })
    const wrapper = mount(MessageBubble, { props: { message: msg } })
    expect(wrapper.find('.animate-blink').exists()).toBe(true)
  })

  it('CT-MB-04: should display token count', () => {
    const msg = createMessage({ role: 'assistant', content: '回复', totalTokens: 256 })
    const wrapper = mount(MessageBubble, { props: { message: msg } })
    expect(wrapper.text()).toContain('256 tokens')
  })

  it('CT-MB-05: should not execute script tags (XSS)', () => {
    const msg = createMessage({ role: 'user', content: '<script>alert(1)</script>' })
    const wrapper = mount(MessageBubble, { props: { message: msg } })
    expect(wrapper.find('script').exists()).toBe(false)
    expect(wrapper.text()).toContain('<script>alert(1)</script>')
  })
})