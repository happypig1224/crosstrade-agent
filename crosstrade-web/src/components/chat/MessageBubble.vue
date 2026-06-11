<template>
  <div :class="['flex mb-4', message.role === 'user' ? 'justify-end' : 'justify-start']">
    <div :class="['max-w-75%', message.role === 'user' ? 'order-2' : 'order-1']">
      <IntentTag
        v-if="message.role === 'assistant' && message.intentType && !message.isStreaming"
        :intent-type="message.intentType"
        class="mb-1"
      />
      <div
        :class="[
          'px-4 py-3 rounded-lg text-sm leading-relaxed',
          message.role === 'user'
            ? 'bg-primary text-white rounded-tr-none'
            : 'bg-white text-gray-800 rounded-tl-none shadow-sm',
        ]"
      >
        <MarkdownContent
          v-if="message.role === 'assistant'"
          :content="message.content"
        />
        <span v-else>{{ message.content }}</span>
        <span
          v-if="message.isStreaming"
          class="inline-block w-2 h-4 ml-1 bg-primary animate-blink"
        />
      </div>
      <div
        :class="[
          'flex items-center gap-2 mt-1 text-xs text-gray-400',
          message.role === 'user' ? 'justify-end' : 'justify-start',
        ]"
      >
        <span>{{ formatTime(message.timestamp) }}</span>
        <span v-if="message.role === 'assistant' && message.totalTokens">
          消耗 {{ message.totalTokens }} tokens
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '@/api/types'
import IntentTag from './IntentTag.vue'
import MarkdownContent from '@/components/common/MarkdownContent.vue'

defineProps<{
  message: Message
}>()

function formatTime(timestamp: number): string {
  const date = new Date(timestamp)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style scoped>
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.animate-blink {
  animation: blink 1s infinite;
}
</style>