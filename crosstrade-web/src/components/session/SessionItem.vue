<template>
  <div
    :class="[
      'flex items-center justify-between px-4 py-3 cursor-pointer transition-colors group',
      isActive ? 'bg-blue-50 border-l-2 border-primary' : 'hover:bg-gray-50',
    ]"
    @click="$emit('switch', session.sessionId)"
  >
    <div class="flex-1 min-w-0">
      <div class="text-sm font-medium truncate" :class="isActive ? 'text-primary' : 'text-gray-700'">
        {{ session.title }}
      </div>
      <div class="text-xs text-gray-400 mt-0.5">
        {{ formatTime(session.lastActiveTime) }} · {{ session.messageCount }} 条消息
      </div>
    </div>
    <button
      class="opacity-0 group-hover:opacity-100 p-1 text-gray-400 hover:text-red-500 transition-all"
      title="关闭会话"
      @click.stop="handleClose"
    >
      ✕
    </button>
  </div>
</template>

<script setup lang="ts">
import type { SessionInfo } from '@/api/types'

const props = defineProps<{
  session: SessionInfo
  isActive: boolean
}>()

const emit = defineEmits<{
  switch: [sessionId: string]
  close: [sessionId: string]
}>()

function handleClose() {
  if (confirm('确定关闭此会话？')) {
    emit('close', props.session.sessionId)
  }
}

function formatTime(timestamp: number): string {
  const date = new Date(timestamp)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
  }
  return (date.getMonth() + 1) + '/' + date.getDate()
}
</script>