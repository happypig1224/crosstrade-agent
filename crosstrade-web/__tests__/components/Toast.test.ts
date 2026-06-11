import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import Toast from '@/components/common/Toast.vue'
import { useToast } from '@/composables/use-toast'

describe('Toast', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('CT-TO-01: should display error toast with red background', () => {
    const { error, toasts } = useToast()
    error('错误')
    const wrapper = mount(Toast)
    expect(wrapper.text()).toContain('错误')
    expect(wrapper.find('.bg-red-500').exists()).toBe(true)
  })

  it('CT-TO-02: should display warning toast with orange background', () => {
    const { warning, toasts } = useToast()
    warning('警告')
    const wrapper = mount(Toast)
    expect(wrapper.text()).toContain('警告')
    expect(wrapper.find('.bg-orange-500').exists()).toBe(true)
  })

  it('CT-TO-03: should display info toast with yellow background', () => {
    const { info, toasts } = useToast()
    info('提示')
    const wrapper = mount(Toast)
    expect(wrapper.text()).toContain('提示')
    expect(wrapper.find('.bg-yellow-500').exists()).toBe(true)
  })

  it('CT-TO-04: should auto dismiss after 3 seconds', async () => {
    vi.useFakeTimers()
    const { show, toasts } = useToast()
    show('测试', 'info')
    const wrapper = mount(Toast)
    expect(wrapper.text()).toContain('测试')
    vi.advanceTimersByTime(3000)
    await wrapper.vm.$nextTick()
    expect(toasts.value.length).toBe(0)
    vi.useRealTimers()
  })
})