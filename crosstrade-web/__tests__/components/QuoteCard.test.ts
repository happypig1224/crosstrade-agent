import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import QuoteCard from '@/components/chat/QuoteCard.vue'

describe('QuoteCard', () => {
  it('CT-QC-01: should display accepted quote with green color', () => {
    const quote = { accepted: true, finalPrice: '$850' }
    const wrapper = mount(QuoteCard, { props: { quote } })
    expect(wrapper.text()).toContain('已接受报价')
    expect(wrapper.text()).toContain('$850')
    expect(wrapper.find('.text-green-600').exists()).toBe(true)
  })

  it('CT-QC-02: should display pending quote with orange color', () => {
    const quote = { accepted: false, minPrice: '$780' }
    const wrapper = mount(QuoteCard, { props: { quote } })
    expect(wrapper.text()).toContain('报价待确认')
    expect(wrapper.text()).toContain('$780')
    expect(wrapper.find('.text-orange-600').exists()).toBe(true)
  })

  it('CT-QC-03: should not render when quote is null', () => {
    const wrapper = mount(QuoteCard, { props: { quote: null } })
    expect(wrapper.find('div').exists()).toBe(false)
  })
})