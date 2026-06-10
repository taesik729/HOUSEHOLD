<template>
  <div class="login-wrap">
    <div class="login-logo">
      <div class="login-logo-icon">🔐</div>
      <h1>비밀번호 재설정</h1>
    </div>
    <div class="login-card">

      <!-- 준비 중 -->
      <template v-if="status === 'waiting'">
        <p class="login-msg info">이메일 링크를 확인하는 중...</p>
      </template>

      <!-- 폼 -->
      <template v-else-if="status === 'ready'">
        <p class="login-logo-sub">새 비밀번호를 입력해주세요</p>
        <div class="form-group">
          <label class="form-label">새 비밀번호</label>
          <div class="pw-wrap">
            <input v-model="pw" class="form-input" :type="show1 ? 'text' : 'password'" autocomplete="new-password" placeholder="새 비밀번호 입력 (6자 이상)" />
            <button type="button" class="eye-btn" @click="show1 = !show1">{{ show1 ? '🙈' : '👁️' }}</button>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">비밀번호 확인</label>
          <div class="pw-wrap">
            <input v-model="pw2" class="form-input" :type="show2 ? 'text' : 'password'" autocomplete="new-password" placeholder="비밀번호 재입력" @keyup.enter="submit" />
            <button type="button" class="eye-btn" @click="show2 = !show2">{{ show2 ? '🙈' : '👁️' }}</button>
          </div>
          <p v-if="pw2" :class="['match-msg', pw === pw2 ? 'ok' : 'ng']">
            {{ pw === pw2 ? '✅ 비밀번호가 일치합니다' : '❌ 비밀번호가 일치하지 않습니다' }}
          </p>
        </div>
        <p v-if="message" :class="['login-msg', isError ? 'error' : 'success']">{{ message }}</p>
        <button class="btn-primary" @click="submit" :disabled="loading">
          {{ loading ? '처리 중...' : '비밀번호 변경' }}
        </button>
      </template>

      <!-- 링크 만료 -->
      <template v-else-if="status === 'expired'">
        <p class="login-msg error">링크가 만료됐거나 유효하지 않습니다.<br>다시 요청해주세요.</p>
        <button class="btn-primary" @click="router.replace('/login')">로그인으로 돌아가기</button>
      </template>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { supabase } from '@/supabase/client'

const router  = useRouter()
const pw      = ref('')
const pw2     = ref('')
const loading = ref(false)
const message = ref('')
const isError = ref(false)
const status  = ref('waiting') // waiting | ready | expired
const show1   = ref(false)
const show2   = ref(false)

let unsubscribe = null

onMounted(() => {
  // 이미 recovery 세션이 있으면 바로 폼 표시
  supabase.auth.getSession().then(({ data }) => {
    if (data.session) {
      status.value = 'ready'
    }
  })

  // PASSWORD_RECOVERY 이벤트 대기
  const { data } = supabase.auth.onAuthStateChange((event, session) => {
    if (event === 'PASSWORD_RECOVERY' && session) {
      status.value = 'ready'
    }
  })
  unsubscribe = data.subscription

  // 10초 후에도 ready 안 되면 만료 처리
  setTimeout(() => {
    if (status.value === 'waiting') status.value = 'expired'
  }, 10000)
})

onUnmounted(() => {
  unsubscribe?.unsubscribe()
})

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
    await supabase.auth.signOut()
    setTimeout(() => router.replace('/login'), 1500)
  }
}
</script>

<style scoped>
.login-logo-sub { font-size: 14px; color: var(--text-hint); text-align: center; margin-top: -8px; }
.login-msg {
  font-size: 13px;
  padding: 10px 12px;
  border-radius: var(--radius);
  text-align: center;
}
.login-msg.error   { background: var(--expense-light); color: var(--expense); }
.login-msg.success { background: var(--income-light);  color: var(--income); }
.login-msg.info    { background: var(--bg-sub); color: var(--text-sub); }
.pw-wrap  { position: relative; }
.pw-wrap .form-input { padding-right: 44px; }
.eye-btn  { position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
            background: none; border: none; cursor: pointer; font-size: 18px; padding: 0; }
.match-msg { font-size: 12px; margin-top: 4px; }
.match-msg.ok { color: var(--income); }
.match-msg.ng { color: var(--expense); }
</style>
