<template>
  <div class="flex h-screen bg-gray-50">
    <Toast />
    <div
      v-if="sidebarOpen && isMobile"
      class="fixed inset-0 bg-black bg-opacity-50 z-30 md:hidden"
      @click="sidebarOpen = false"
    />
    <aside
      :class="[
        'flex-shrink-0 transition-all duration-300 z-40',
        isMobile
          ? (sidebarOpen ? 'fixed left-0 top-0 h-full w-64' : 'w-0 overflow-hidden')
          : (sidebarOpen ? 'w-64' : 'w-0 overflow-hidden'),
      ]"
    >
      <div class="h-full flex flex-col bg-white">
        <div class="p-4 border-b border-gray-200">
          <RegionSelector v-model="region" />
        </div>
        <SessionList
          :sessions="sessions"
          :current-session-id="currentSessionId"
          @create="handleCreateSession"
          @switch="handleSwitchSession"
          @close="handleCloseSession"
        />
      </div>
    </aside>

    <main class="flex-1 flex flex-col min-w-0">
      <header class="flex items-center gap-3 px-4 py-3 bg-white border-b border-gray-200">
        <button
          class="md:hidden p-1 text-gray-500 hover:text-gray-700"
          @click="sidebarOpen = !sidebarOpen"
        >
          <span class="text-xl">☰</span>
        </button>
        <h1 class="text-lg font-semibold text-gray-800">跨境电商智能营销助手</h1>
      </header>

      <div ref="messageListRef" class="flex-1 overflow-y-auto p-4">
        <div v-if="messages.length === 0" class="flex items-center justify-center h-full">
          <div class="text-center text-gray-400">
            <div class="text-4xl mb-4">💬</div>
            <p class="text-sm">开始一段新的对话吧</p>
          </div>
        </div>
        <MessageBubble
          v-for="msg in messages"
          :key="msg.id"
          :message="msg"
        />
        <QuickQuestions
          v-if="lastIntentType === 'unknown' && !isStreaming"
          intent-type="unknown"
          @select="handleQuickQuestion"
        />
      </div>

      <ChatInput
        :is-streaming="isStreaming"
        @send="handleSend"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { useChatStore } from '@/stores/chat-store'
import { useSessionStore } from '@/stores/session-store'
import { useChat } from '@/composables/use-chat'
import { useSession } from '@/composables/use-session'
import Toast from '@/components/common/Toast.vue'
import RegionSelector from '@/components/common/RegionSelector.vue'
import SessionList from '@/components/session/SessionList.vue'
import MessageBubble from '@/components/chat/MessageBubble.vue'
import ChatInput from '@/components/chat/ChatInput.vue'

const QuickQuestions = defineAsyncComponent(
  () => import('@/components/chat/QuickQuestions.vue'),
)

const chatStore = useChatStore()
const sessionStore = useSessionStore()
const { sendMessage, isStreaming } = useChat()
const { createSession, closeSession, switchSession, initSession } = useSession()

const messageListRef = ref<HTMLElement>()
const sidebarOpen = ref(true)
const isMobile = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) sidebarOpen.value = true
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  initSession()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

const messages = computed(() => chatStore.messages)
const sessions = computed(() => sessionStore.sessions)
const currentSessionId = computed(() => sessionStore.currentSessionId)
const region = computed({
  get: () => sessionStore.region,
  set: (val: string) => sessionStore.setRegion(val),
})

const lastIntentType = computed(() => {
  const last = chatStore.messages[chatStore.messages.length - 1]
  return last?.role === 'assistant' && last.intentType ? last.intentType : ''
})

let scrollRAF = 0

function scrollToBottom() {
  cancelAnimationFrame(scrollRAF)
  scrollRAF = requestAnimationFrame(() => {
    nextTick(() => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    })
  })
}

function handleSend(text: string) {
  sendMessage(text)
  scrollToBottom()
}

function handleQuickQuestion(text: string) {
  sendMessage(text)
  scrollToBottom()
}

async function handleCreateSession() {
  await createSession()
  scrollToBottom()
}

async function handleSwitchSession(sessionId: string) {
  switchSession(sessionId)
  scrollToBottom()
}

async function handleCloseSession(sessionId: string) {
  await closeSession(sessionId)
}

watch(
  () => chatStore.messages.length,
  () => scrollToBottom(),
)

watch(
  () => chatStore.messages[chatStore.messages.length - 1]?.content,
  () => scrollToBottom(),
)

</script>