<template>
  <div class="markdown-content" v-html="sanitizedHtml" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown-renderer'
import { sanitize } from '@/utils/xss-sanitizer'

const props = defineProps<{
  content: string
}>()

const sanitizedHtml = computed(() => {
  const html = renderMarkdown(props.content)
  return sanitize(html)
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.6;
  word-break: break-word;
}

.markdown-content :deep(p) {
  margin-bottom: 8px;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(pre) {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
  font-size: 13px;
}

.markdown-content :deep(code) {
  background-color: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 13px;
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 20px;
  margin-bottom: 8px;
}

.markdown-content :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #ddd;
  padding-left: 12px;
  color: #666;
  margin-bottom: 8px;
}
</style>