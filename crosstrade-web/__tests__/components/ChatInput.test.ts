import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ChatInput from '@/components/chat/ChatInput.vue'

describe('ChatInput', () => {
  it('CT-CI-01: should emit send event when clicking send button with text', async () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '你好' } })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('send')).toBeTruthy()
    expect(wrapper.emitted('send')![0][0]).toBe('你好')
  })

  it('CT-CI-02: should disable send button when input is empty', () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '' } })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('CT-CI-03: should disable send button when input is only whitespace', () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '   ' } })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('CT-CI-04: should disable textarea and button during streaming', () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: true, modelValue: '你好' } })
    expect(wrapper.find('textarea').attributes('disabled')).toBeDefined()
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('CT-CI-05: should emit send on Enter key', async () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '你好' } })
    await wrapper.find('textarea').trigger('keydown', { key: 'Enter' })
    expect(wrapper.emitted('send')).toBeTruthy()
  })

  it('CT-CI-06: should not emit send on Shift+Enter', async () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '你好' } })
    await wrapper.find('textarea').trigger('keydown', { key: 'Enter', shiftKey: true })
    expect(wrapper.emitted('send')).toBeFalsy()
  })

  it('CT-CI-07: should clear input after send', async () => {
    const wrapper = mount(ChatInput, { props: { isStreaming: false, modelValue: '你好' } })
    await wrapper.find('button').trigger('click')
    expect(wrapper.vm.modelValue).toBe('')
  })
})