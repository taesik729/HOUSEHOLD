<template>
  <div class="login-wrap">
    <div class="login-logo">
      <div class="login-logo-icon">🔐</div>
      <h1>비밀번호 재설정</h1>
      <p>새 비밀번호를 입력해주세요</p>
    </div>
    <div class="login-card">
      <div class="form-group">
        <label class="form-label">새 비밀번호</label>
        <input v-model="pw" class="form-input" type="password" autocomplete="new-password" placeholder="새 비밀번호 입력" />
      </div>
      <div class="form-group">
        <label class="form-label">비밀번호 확인</label>
        <input v-model="pw2" class="form-input" type="password" autocomplete="new-password" placeholder="비밀번호 재입력"
          @keyup.enter="submit" />
      </div>
      <p v-if="message" :class="['login-msg', isError ? 'error' : 'success']">{{ message }}</p>
      <button class="btn-primary" @click="submit" :disabled="loading">
        {{ loading ? '처리 중...' : '비밀번호 변경' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { supabase } from '@/supabase/client'

const router  = useRouter()
const pw      = ref('')
const pw2     = ref('')
const loading = ref(false)
const message = ref('')
const isError = ref(false)

async function submit() {
  if (!pw.value || !pw2.value) {
    message.value = '비밀번호를 입력해주세요.'; isError.value = true; return
  }
  if (pw.value !== pw2.value) {
    message.value = '비밀번호가 일치하지 않습니다.'; isError.value = true; return
  }
  if (pw.value.length < 6) {
    message.value = '비밀번호는 6자 이상이어야 합니다.'; isError.value = true; return
  }
  loading.value = true
  const { error } = await supabase.auth.updateUser({ password: pw.value })
  loading.value = false
  if (error) {
    message.value = '변경 실패. 다시 시도해주세요.'; isError.value = true
  } else {
    message.value = '✅ 비밀번호가 변경됐습니다.'
    isError.value = false
    setTimeout(() => router.replace('/login'), 1500)
  }
}
</script>

<style scoped>
.login-msg {
  font-size: 13px;
  padding: 10px 12px;
  border-radius: var(--radius);
  text-align: center;
}
.login-msg.error   { background: var(--expense-light); color: var(--expense); }
.login-msg.success { background: var(--income-light);  color: var(--income); }
</style>
