import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RegionSelector from '@/components/common/RegionSelector.vue'

describe('RegionSelector', () => {
  it('CT-RS-01: should have 4 options', () => {
    const wrapper = mount(RegionSelector, { props: { modelValue: '' } })
    const options = wrapper.findAll('option')
    expect(options.length).toBe(4)
  })

  it('CT-RS-02: should emit update:modelValue when selecting', async () => {
    const wrapper = mount(RegionSelector, { props: { modelValue: '' } })
    const select = wrapper.find('select')
    await select.setValue('US')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0][0]).toBe('US')
  })

  it('CT-RS-03: should reflect initial modelValue', () => {
    const wrapper = mount(RegionSelector, { props: { modelValue: 'EU' } })
    const select = wrapper.find('select')
    expect((select.element as HTMLSelectElement).value).toBe('EU')
  })
})