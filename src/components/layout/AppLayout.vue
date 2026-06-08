<template>
  <div class="layout">
    <header class="topbar">
      <div class="topbar-title">가계부</div>
      <button class="icon-btn" @click="logout" style="border:none">
        <i class="ti ti-logout" style="color:var(--text-hint)"></i>
      </button>
    </header>
    <main class="content">
      <slot />
    </main>
    <nav class="bottom-nav">
      <RouterLink v-for="item in NAV" :key="item.path"
        :to="item.path" class="bn-item" active-class="active">
        <i :class="`ti ${item.icon}`"></i>
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth   = useAuthStore()
const router = useRouter()

const NAV = [
  { path: '/',        label: '홈',   icon: 'ti-home' },
  { path: '/ledger',  label: '내역', icon: 'ti-list' },
  { path: '/stats',   label: '통계', icon: 'ti-chart-pie' },
  { path: '/settings',label: '설정', icon: 'ti-settings' },
]

async function logout() {
  await auth.logout()
  router.push('/login')
}
</script>
