<template>
  <div
    v-if="quote"
    class="bg-white rounded-lg border border-gray-200 p-4 shadow-sm"
  >
    <div class="flex items-center gap-2 mb-2">
      <span
        :class="[
          'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
          quote.accepted ? 'bg-green-50 text-green-600' : 'bg-orange-50 text-orange-600',
        ]"
      >
        {{ quote.accepted ? '已接受报价' : '报价待确认' }}
      </span>
    </div>
    <div class="flex items-center justify-between">
      <span
        v-if="quote.accepted && quote.finalPrice"
        class="text-lg font-bold text-green-600"
      >
        {{ quote.finalPrice }}
      </span>
      <span
        v-if="!quote.accepted && quote.minPrice"
        class="text-lg font-bold text-orange-600"
      >
        最低 {{ quote.minPrice }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { QuoteInfo } from '@/api/types'

defineProps<{
  quote: QuoteInfo | null | undefined
}>()
</script>