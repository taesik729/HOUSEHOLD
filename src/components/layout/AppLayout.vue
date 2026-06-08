<template>
  <div class="layout">
    <!-- 상단바 -->
    <header class="topbar">
      <div>
        <div class="topbar-title">🏠 우리집 가계부</div>
      </div>
      <button class="icon-btn" style="color:#fff;border-color:rgba(255,255,255,.2)" @click="logout">
        <i class="ti ti-logout"></i>
      </button>
    </header>

    <!-- 콘텐츠 -->
    <main class="content">
      <slot />
    </main>

    <!-- 하단 탭바 -->
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
