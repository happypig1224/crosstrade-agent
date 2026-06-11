<template>
  <div class="fixed top-4 left-1/2 -translate-x-1/2 z-50 flex flex-col items-center gap-2">
    <TransitionGroup name="toast">
      <div
        v-for="item in toasts"
        :key="item.id"
        :class="[
          'px-4 py-3 rounded-lg shadow-lg text-white text-sm font-medium min-w-280px max-w-480px text-center',
          levelClass(item.level),
        ]"
      >
        <span class="mr-2">{{ levelIcon(item.level) }}</span>
        {{ item.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/use-toast'
import type { ToastItem } from '@/composables/use-toast'

const { toasts } = useToast()

function levelClass(level: ToastItem['level']): string {
  switch (level) {
    case 'error':
      return 'bg-red-500'
    case 'warning':
      return 'bg-orange-500'
    case 'info':
      return 'bg-yellow-500'
    default:
      return 'bg-gray-500'
  }
}

function levelIcon(level: ToastItem['level']): string {
  switch (level) {
    case 'error':
      return '✕'
    case 'warning':
      return '⚠'
    case 'info':
      return 'ℹ'
    default:
      return '•'
  }
}
</script>

<style scoped>
.toast-enter-active {
  transition: all 0.3s ease;
}
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>