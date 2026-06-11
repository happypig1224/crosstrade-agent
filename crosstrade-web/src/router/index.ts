import { createRouter as _createRouter, createWebHistory } from 'vue-router'
import ChatView from '@/views/ChatView.vue'

const router = _createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'chat',
      component: ChatView,
    },
  ],
})

export default router
