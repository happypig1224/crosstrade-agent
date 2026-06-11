<template>
  <div
    v-if="product"
    class="bg-white rounded-lg border border-gray-200 p-4 shadow-sm hover:shadow-md transition-shadow"
  >
    <h4 class="text-base font-semibold text-gray-800 mb-2">{{ product.name }}</h4>
    <p class="text-sm text-gray-600 mb-3">{{ product.description }}</p>
    <div class="flex items-center justify-between">
      <span class="text-lg font-bold text-primary">{{ product.price }}</span>
      <span class="text-xs text-gray-400">起订量: {{ product.minOrder }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ProductInfo } from '@/api/types'

defineProps<{
  product: ProductInfo | null | undefined
}>()
</script>