<template>
  <AppLayout>
    <div class="page">
      <div class="card">
        <div class="card-title"><i class="ti ti-user"></i> 계정 정보</div>
        <div style="font-size:14px;color:var(--text-sub);padding:4px 0">
          <div style="margin-bottom:6px">이메일</div>
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

      <button class="btn-withdraw" @click="showWithdraw = true">
        회원 탈퇴
      </button>
    </div>

    <!-- 탈퇴 확인 모달 -->
    <div v-if="showWithdraw" class="modal-overlay" @click.self="showWithdraw = false">
      <div class="modal">
        <div class="modal-hd">
          <div class="modal-title">회원 탈퇴</div>
          <button class="icon-btn" @click="showWithdraw = false"><i class="ti ti-x"></i></button>
        </div>

        <div style="font-size:14px;color:var(--text-sub);line-height:1.7;padding:4px 0">
          탈퇴 시 <strong style="color:var(--expense)">모든 가계부 데이터가 삭제</strong>되며 복구할 수 없습니다.<br>
          정말 탈퇴하시겠습니까?
        </div>

        <div class="form-group">
          <label class="form-label">확인을 위해 <strong style="color:var(--expense)">탈퇴</strong> 라고 입력하세요</label>
          <input v-model="confirmPw" type="text" class="form-input"
            placeholder="탈퇴" />
        </div>

        <p v-if="withdrawError" style="font-size:13px;color:var(--expense)">{{ withdrawError }}</p>

        <div class="modal-ft" style="display:flex;gap:8px">
          <button class="btn-secondary" style="flex:1" @click="showWithdraw = false">취소</button>
          <button class="btn-primary" style="flex:1;background:var(--expense)"
            @click="withdraw" :disabled="withdrawing">
            {{ withdrawing ? '처리 중...' : '탈퇴하기' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { supabase } from '@/supabase/client'

const auth   = useAuthStore()
const router = useRouter()

const displayId    = computed(() => auth.user?.email ?? '')
const showWithdraw = ref(false)
const confirmPw    = ref('')
const withdrawing  = ref(false)
const withdrawError = ref('')

async function logout() {
  await auth.logout()
  router.push('/login')
}

async function withdraw() {
  withdrawError.value = ''
  if (confirmPw.value.trim() !== '탈퇴') {
    withdrawError.value = '"탈퇴" 라고 정확히 입력해주세요.'; return
  }
  withdrawing.value = true

  // 1. 데이터 삭제
  await supabase.from('household_ledger').delete().eq('user_id', auth.user.id)

  // 3. 계정 삭제
  const { error: deleteErr } = await supabase.rpc('delete_user')
  if (deleteErr) {
    // RPC 없을 경우 그냥 로그아웃만
    await auth.logout()
    router.push('/login')
    return
  }

  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.btn-withdraw {
  background: none;
  border: none;
  color: var(--text-hint);
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer;
  text-align: center;
  padding: 8px;
  width: 100%;
}
.btn-withdraw:hover { color: var(--expense); }
</style>
