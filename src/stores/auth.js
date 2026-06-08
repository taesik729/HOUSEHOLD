import { defineStore } from 'pinia'
import { ref } from 'vue'
import { supabase } from '@/supabase/client'

export const useAuthStore = defineStore('auth', () => {
  const user    = ref(null)
  const loading = ref(false)
  const error   = ref('')

  async function login(email, password) {
    loading.value = true
    error.value   = ''
    const { error: err } = await supabase.auth.signInWithPassword({ email, password })
    loading.value = false
    if (err) { error.value = '이메일 또는 비밀번호가 올바르지 않습니다.'; return false }
    return true
  }

  async function signup(email, password) {
    loading.value = true
    error.value   = ''
    const { error: err } = await supabase.auth.signUp({ email, password })
    loading.value = false
    if (err) { error.value = err.message; return false }
    return true
  }

  async function logout() {
    await supabase.auth.signOut()
    user.value = null
  }

  async function init() {
    return new Promise((resolve) => {
      supabase.auth.getSession().then(({ data }) => {
        user.value = data.session?.user ?? null
        resolve()
      })
      supabase.auth.onAuthStateChange((_e, session) => {
        user.value = session?.user ?? null
      })
    })
  }

  return { user, loading, error, login, signup, logout, init }
})
