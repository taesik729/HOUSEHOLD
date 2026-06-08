<template>
  <div class="login-wrap">
    <div class="login-logo">
      <div class="login-logo-icon">🏠</div>
      <h1>우리집 가계부</h1>
      <p>가정 수입·지출 관리</p>
    </div>
    <div class="login-card">

      <!-- 비밀번호 찾기 모드 -->
      <template v-if="isForgot">
        <div class="form-group">
          <label class="form-label">가입한 이메일</label>
          <input v-model="email" class="form-input" type="email" placeholder="이메일 입력"
            @keyup.enter="sendReset" autocomplete="email" />
        </div>
        <p v-if="message" :class="['login-msg', isError ? 'error' : 'success']">{{ message }}</p>
        <button class="btn-primary" @click="sendReset" :disabled="sending">
          {{ sending ? '발송 중...' : '재설정 링크 발송' }}
        </button>
        <button class="toggle-btn" @click="isForgot = false; message = ''">
          ← 로그인으로 돌아가기
        </button>
      </template>

      <!-- 로그인 / 회원가입 모드 -->
      <template v-else>
        <div class="form-group">
          <label class="form-label">이메일</label>
          <input v-model="email" class="form-input" type="email" placeholder="이메일 입력"
            @keyup.enter="submit" autocomplete="email" />
        </div>
        <div class="form-group">
          <label class="form-label">비밀번호</label>
          <input v-model="pw" class="form-input" type="password" placeholder="비밀번호 입력"
            @keyup.enter="submit" autocomplete="current-password" />
        </div>

        <p v-if="message" :class="['login-msg', isError ? 'error' : 'success']">{{ message }}</p>

        <button class="btn-primary" @click="submit" :disabled="auth.loading">
          {{ auth.loading ? '처리 중...' : (isSignup ? '회원가입' : '로그인') }}
        </button>

        <div class="bottom-links">
          <button class="toggle-btn" @click="toggle">
            {{ isSignup ? '이미 계정이 있으신가요? 로그인' : '계정이 없으신가요? 회원가입' }}
          </button>
          <button v-if="!isSignup" class="forgot-btn" @click="isForgot = true; message = ''">
            비밀번호를 잊으셨나요?
          </button>
        </div>
      </template>

    </div>

    <div class="privacy-link">
      <RouterLink to="/privacy">개인정보처리방침</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { supabase } from '@/supabase/client'

const auth     = useAuthStore()
const router   = useRouter()
const email    = ref('')
const pw       = ref('')
const isSignup = ref(false)
const isForgot = ref(false)
const message  = ref('')
const isError  = ref(false)
const sending  = ref(false)

function toggle() {
  isSignup.value = !isSignup.value
  message.value  = ''
}

async function submit() {
  if (!email.value || !pw.value) {
    message.value = '이메일과 비밀번호를 입력해주세요.'; isError.value = true; return
  }
  message.value = ''
  if (isSignup.value) {
    const ok = await auth.signup(email.value.trim(), pw.value)
    if (ok) {
      message.value = '✅ 가입 완료! 로그인해주세요.'
      isError.value  = false
      isSignup.value = false
    } else {
      message.value = auth.error; isError.value = true
    }
  } else {
    const ok = await auth.login(email.value.trim(), pw.value)
    if (ok) router.replace('/')
    else { message.value = auth.error; isError.value = true }
  }
}

async function sendReset() {
  if (!email.value) {
    message.value = '이메일을 입력해주세요.'; isError.value = true; return
  }
  sending.value = true
  message.value = ''
  const { error } = await supabase.auth.resetPasswordForEmail(email.value.trim(), {
    redirectTo: `${window.location.origin}/reset-password`
  })
  sending.value = false
  if (error) {
    message.value = '발송 실패. 이메일을 확인해주세요.'; isError.value = true
  } else {
    message.value = '✅ 재설정 링크를 이메일로 발송했습니다.'
    isError.value = false
  }
}
</script>

<style scoped>
.bottom-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.toggle-btn {
  background: none;
  border: none;
  color: var(--primary-bright);
  font-size: 13px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  text-decoration: underline;
  width: 100%;
}
.privacy-link {
  text-align: center;
  margin-top: 16px;
}
.privacy-link a {
  font-size: 12px;
  color: var(--text-hint);
  text-decoration: underline;
}
.forgot-btn {
  background: none;
  border: none;
  color: var(--text-hint);
  font-size: 12px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  text-decoration: underline;
  width: 100%;
}
.login-msg {
  font-size: 13px;
  padding: 10px 12px;
  border-radius: var(--radius);
  text-align: center;
}
.login-msg.error   { background: var(--expense-light); color: var(--expense); }
.login-msg.success { background: var(--income-light);  color: var(--income); }
</style>
