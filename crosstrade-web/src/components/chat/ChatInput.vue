<template>
  <div class="flex items-end gap-2 px-4 py-3 bg-white border-t border-gray-200">
    <textarea
      ref="textareaRef"
      v-model="modelValue"
      :disabled="isStreaming"
      :placeholder="isStreaming ? 'AI 正在回复中...' : '输入消息...'"
      class="flex-1 resize-none border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary disabled:bg-gray-100 disabled:text-gray-400"
      rows="1"
      @keydown="handleKeydown"
      @input="autoResize"
    />
    <button
      :disabled="!canSend"
      class="px-4 py-2 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      :class="canSend ? 'bg-primary text-white hover:bg-blue-600' : 'bg-gray-200 text-gray-400'"
      @click="handleSend"
    >
      发送
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

const props = defineProps<{
  isStreaming: boolean
}>()

const modelValue = defineModel<string>({ default: '' })

const emit = defineEmits<{
  send: [text: string]
}>()

const textareaRef = ref<HTMLTextAreaElement>()

const canSend = computed(() => !props.isStreaming && modelValue.value.trim().length > 0)

function handleSend() {
  const text = modelValue.value.trim()
  if (!text || props.isStreaming) return
  emit('send', text)
  modelValue.value = ''
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function autoResize() {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = Math.min(textareaRef.value.scrollHeight, 120) + 'px'
  }
}

watch(() => props.isStreaming, () => {
  if (!props.isStreaming && textareaRef.value) {
    textareaRef.value.focus()
  }
})
</script>