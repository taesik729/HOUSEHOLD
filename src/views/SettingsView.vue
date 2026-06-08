<template>
  <AppLayout>
    <div class="page">
      <div class="card">
        <div class="card-title"><i class="ti ti-user"></i> 계정 정보</div>
        <div style="font-size:14px;color:var(--text-sub);padding:4px 0">
          <div style="margin-bottom:6px">로그인 아이디</div>
          <div style="font-weight:600;color:var(--text)">{{ displayId }}</div>
        </div>
      </div>

      <div class="card">
        <div class="card-title"><i class="ti ti-info-circle"></i> 앱 정보</div>
        <div style="font-size:13px;color:var(--text-sub);line-height:1.8">
          <div>버전: 0.1.0 MVP</div>
          <div>우리집 가계부</div>
        </div>
      </div>

      <button class="btn-primary" style="background:var(--expense)" @click="logout">
        <i class="ti ti-logout"></i> 로그아웃
      </button>
    </div>
  </AppLayout>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'

const auth   = useAuthStore()
const router = useRouter()

const displayId = computed(() => auth.user?.email ?? '')

async function logout() {
  await auth.logout()
  router.push('/login')
}
</script>
