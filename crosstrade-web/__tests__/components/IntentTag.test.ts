import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import IntentTag from '@/components/chat/IntentTag.vue'

describe('IntentTag', () => {
  it('CT-IT-01: should render product_guide tag with blue color', () => {
    const wrapper = mount(IntentTag, { props: { intentType: 'product_guide' } })
    expect(wrapper.text()).toBe('产品导购')
    expect(wrapper.find('span').attributes('style')).toContain('#1890ff')
  })

  it('CT-IT-02: should render product_info tag with green color', () => {
    const wrapper = mount(IntentTag, { props: { intentType: 'product_info' } })
    expect(wrapper.text()).toBe('产品咨询')
    expect(wrapper.find('span').attributes('style')).toContain('#52c41a')
  })

  it('CT-IT-03: should render quote_negotiate tag with orange color', () => {
    const wrapper = mount(IntentTag, { props: { intentType: 'quote_negotiate' } })
    expect(wrapper.text()).toBe('报价谈判')
    expect(wrapper.find('span').attributes('style')).toContain('#fa8c16')
  })

  it('CT-IT-04: should render unknown tag with gray color', () => {
    const wrapper = mount(IntentTag, { props: { intentType: 'unknown' } })
    expect(wrapper.text()).toBe('未知意图')
    expect(wrapper.find('span').attributes('style')).toContain('#8c8c8c')
  })
})