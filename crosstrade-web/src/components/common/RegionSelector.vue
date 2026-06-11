<template>
  <select
    :value="modelValue"
    class="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:border-primary"
    @change="handleChange"
  >
    <option v-for="opt in REGION_OPTIONS" :key="opt.value" :value="opt.value">
      {{ opt.label }}
    </option>
  </select>
</template>

<script setup lang="ts">
import { REGION_OPTIONS } from '@/api/types'

defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

function handleChange(e: Event) {
  const target = e.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}
</script>