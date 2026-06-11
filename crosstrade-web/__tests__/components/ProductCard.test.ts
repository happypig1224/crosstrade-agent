import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductCard from '@/components/chat/ProductCard.vue'

describe('ProductCard', () => {
  it('CT-PC-01: should display product info', () => {
    const product = { name: '帐篷', description: '户外帐篷', price: '$99', minOrder: '100' }
    const wrapper = mount(ProductCard, { props: { product } })
    expect(wrapper.text()).toContain('帐篷')
    expect(wrapper.text()).toContain('户外帐篷')
    expect(wrapper.text()).toContain('$99')
    expect(wrapper.text()).toContain('100')
  })

  it('CT-PC-02: should not render when product is null', () => {
    const wrapper = mount(ProductCard, { props: { product: null } })
    expect(wrapper.find('div').exists()).toBe(false)
  })

  it('CT-PC-02b: should not render when product is undefined', () => {
    const wrapper = mount(ProductCard, { props: { product: undefined } })
    expect(wrapper.find('div').exists()).toBe(false)
  })
})