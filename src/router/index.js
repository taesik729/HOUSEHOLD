import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/login',     name: 'Login',     component: () => import('@/views/LoginView.vue'),     meta: { public: true } },
  { path: '/',          name: 'Home',      component: () => import('@/views/HomeView.vue') },
  { path: '/ledger',    name: 'Ledger',    component: () => import('@/views/LedgerView.vue') },
  { path: '/stats',     name: 'Stats',     component: () => import('@/views/StatsView.vue') },
  { path: '/settings',  name: 'Settings',  component: () => import('@/views/SettingsView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.user) await auth.init()
  if (!to.meta.public && !auth.user) return '/login'
  if (to.path === '/login' && auth.user) return '/'
})

export default router
