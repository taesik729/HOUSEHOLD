import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/login',     name: 'Login',     component: () => import('@/views/LoginView.vue'),     meta: { public: true } },
  { path: '/',          name: 'Home',      component: () => import('@/views/HomeView.vue') },
  { path: '/ledger',    name: 'Ledger',    component: () => import('@/views/LedgerView.vue') },
  { path: '/stats',     name: 'Stats',     component: () => import('@/views/StatsView.vue') },
  { path: '/settings',        name: 'Settings',       component: () => import('@/views/SettingsView.vue') },
  { path: '/reset-password',  name: 'ResetPassword',  component: () => import('@/views/ResetPasswordView.vue'), meta: { public: true } },
  { path: '/privacy',         name: 'Privacy',        component: () => import('@/views/PrivacyView.vue'),       meta: { public: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  // URL에 recovery 토큰이 있으면 reset-password로 이동
  const hash  = window.location.hash
  const query = to.query
  const isRecovery = hash.includes('type=recovery') ||
                     query.type === 'recovery' ||
                     hash.includes('access_token') ||
                     !!query.code  // PKCE 방식: ?code=xxx
  if (isRecovery && to.path !== '/reset-password') return '/reset-password'

  // 비밀번호 재설정 페이지는 항상 허용
  if (to.path === '/reset-password') return true

  const auth = useAuthStore()
  if (!auth.user) await auth.init()
  if (!to.meta.public && !auth.user) return '/login'
  if (to.path === '/login' && auth.user) return '/'
})

export default router
