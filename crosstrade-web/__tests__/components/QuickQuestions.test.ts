import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import QuickQuestions from '@/components/chat/QuickQuestions.vue'

describe('QuickQuestions', () => {
  it('CT-QQ-01: should show quick buttons when intentType is unknown', () => {
    const wrapper = mount(QuickQuestions, { props: { intentType: 'unknown' } })
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThanOrEqual(3)
    expect(buttons.length).toBeLessThanOrEqual(4)
  })

  it('CT-QQ-02: should hide quick buttons when intentType is not unknown', () => {
    const wrapper = mount(QuickQuestions, { props: { intentType: 'product_guide' } })
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBe(0)
  })

  it('CT-QQ-03: should emit select event when clicking button', () => {
    const wrapper = mount(QuickQuestions, { props: { intentType: 'unknown' } })
    const button = wrapper.find('button')
    button.trigger('click')
    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')![0][0]).toBe(button.text())
  })
})