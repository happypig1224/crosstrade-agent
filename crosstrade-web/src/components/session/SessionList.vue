<template>
  <div class="flex flex-col h-full bg-white border-r border-gray-200">
    <div class="p-4 border-b border-gray-200">
      <button
        class="w-full px-4 py-2 bg-primary text-white rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
        @click="$emit('create')"
      >
        + 新建会话
      </button>
    </div>
    <div class="flex-1 overflow-y-auto">
      <SessionItem
        v-for="session in sessions"
        :key="session.sessionId"
        :session="session"
        :is-active="session.sessionId === currentSessionId"
        @switch="$emit('switch', $event)"
        @close="$emit('close', $event)"
      />
      <div v-if="sessions.length === 0" class="p-4 text-center text-sm text-gray-400">
        暂无会话
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { SessionInfo } from '@/api/types'
import SessionItem from './SessionItem.vue'

defineProps<{
  sessions: SessionInfo[]
  currentSessionId: string
}>()

defineEmits<{
  create: []
  switch: [sessionId: string]
  close: [sessionId: string]
}>()
</script>