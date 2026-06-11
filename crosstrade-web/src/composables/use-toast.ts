import { ref } from 'vue'

export interface ToastItem {
  id: number
  message: string
  level: 'error' | 'warning' | 'info'
}

const toasts = ref<ToastItem[]>([])
let nextId = 0

export function useToast() {
  function show(message: string, level: 'error' | 'warning' | 'info' = 'info') {
    const id = nextId++
    toasts.value = [{ id, message, level }]

    setTimeout(() => {
      toasts.value = toasts.value.filter((t) => t.id !== id)
    }, 3000)
  }

  function error(message: string) {
    show(message, 'error')
  }

  function warning(message: string) {
    show(message, 'warning')
  }

  function info(message: string) {
    show(message, 'info')
  }

  return { toasts, show, error, warning, info }
}