<template>
  <span
    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
    :style="{ backgroundColor: tagColor + '15', color: tagColor }"
  >
    {{ tagLabel }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { INTENT_TYPE_MAP } from '@/api/types'

const props = defineProps<{
  intentType: string
}>()

const tagLabel = computed(() => {
  const map = INTENT_TYPE_MAP as Record<string, { label: string; color: string }>
  return map[props.intentType]?.label || '未知意图'
})

const tagColor = computed(() => {
  const map = INTENT_TYPE_MAP as Record<string, { label: string; color: string }>
  return map[props.intentType]?.color || '#8c8c8c'
})
</script>