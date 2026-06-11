<template>
  <div v-if="intentType === 'unknown'" class="flex flex-wrap gap-2 mt-2 px-4">
    <button
      v-for="question in quickQuestions"
      :key="question"
      class="px-3 py-1.5 text-sm rounded-full border border-gray-300 text-gray-600 hover:border-primary hover:text-primary transition-colors"
      @click="$emit('select', question)"
    >
      {{ question }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  intentType: string
}>()

defineEmits<{
  select: [text: string]
}>()

const quickQuestions = ref([
  '推荐一款户外帐篷',
  '我想了解产品价格',
  '请问最低起订量是多少',
  '有什么优惠活动吗',
])
</script>