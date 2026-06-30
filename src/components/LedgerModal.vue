<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-hd">
        <div class="modal-title">{{ form.id ? '내역 수정' : '내역 추가' }}</div>
        <button class="icon-btn" @click="$emit('close')"><i class="ti ti-x"></i></button>
      </div>

      <!-- 수입/지출 토글 -->
      <div class="type-toggle">
        <button :class="['type-btn income', form.type==='income'?'active':'']"
          @click="form.type='income'">
          <i class="ti ti-trending-up"></i> 수입
        </button>
        <button :class="['type-btn expense', form.type==='expense'?'active':'']"
          @click="form.type='expense'">
          <i class="ti ti-trending-down"></i> 지출
        </button>
      </div>

      <!-- 날짜 -->
      <div class="form-group">
        <label class="form-label">날짜</label>
        <div class="date-split">
          <input v-model="dateY" type="number" class="form-input" placeholder="년" @input="syncDate" />
          <span class="date-sep">-</span>
          <input v-model="dateM" type="number" class="form-input" placeholder="월" @input="syncDate" />
          <span class="date-sep">-</span>
          <input v-model="dateD" type="number" class="form-input" placeholder="일" @input="syncDate" />
        </div>
      </div>

      <!-- 금액 -->
      <div class="form-group">
        <label class="form-label">금액 (원)</label>
        <input v-model="form.amount" type="number" class="form-input" placeholder="0" />
      </div>

      <!-- 카테고리 -->
      <div class="form-group">
        <label class="form-label">카테고리</label>
        <div class="cat-grid">
          <button v-for="c in (form.type==='income' ? incomeCats : expenseCats)"
            :key="c.id" type="button"
            :class="['cat-btn', form.category===c.name ? 'active' : '']"
            @click="form.category = c.name">
            <span class="cat-icon">{{ c.icon }}</span>
            <span class="cat-label">{{ c.name }}</span>
          </button>
        </div>
      </div>

      <!-- 메모 -->
      <div class="form-group">
        <label class="form-label">메모 (선택)</label>
        <input v-model="form.memo" type="text" class="form-input" placeholder="간단한 메모" />
      </div>

      <p v-if="errMsg" style="font-size:13px;color:var(--expense)">{{ errMsg }}</p>

      <div class="modal-ft">
        <button class="btn-primary" @click="save" :disabled="saving">
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { supabase } from '@/supabase/client'
import { useAuthStore } from '@/stores/auth'
import { useCategories } from '@/composables/useCategories.js'

const props = defineProps({ editRow: { type: Object, default: null } })
const emit  = defineEmits(['close', 'saved'])
const auth  = useAuthStore()

const { categories, fetchCategories, byType } = useCategories()
const incomeCats  = computed(() => byType('income'))
const expenseCats = computed(() => byType('expense'))

onMounted(fetchCategories)

const today = new Date().toISOString().slice(0, 10)
const form = ref({ id: null, type: 'expense', date: today, amount: '', category: '', memo: '' })
const saving = ref(false)
const errMsg = ref('')

const dateY = ref(today.slice(0, 4))
const dateM = ref(today.slice(5, 7))
const dateD = ref(today.slice(8, 10))

function syncDate() {
  const y = String(dateY.value).padStart(4, '0')
  const m = String(dateM.value).padStart(2, '0')
  const d = String(dateD.value).padStart(2, '0')
  form.value.date = `${y}-${m}-${d}`
}

watch(() => props.editRow, (r) => {
  if (r) {
    form.value = { ...r }
    dateY.value = r.date?.slice(0, 4) ?? today.slice(0, 4)
    dateM.value = r.date?.slice(5, 7) ?? today.slice(5, 7)
    dateD.value = r.date?.slice(8, 10) ?? today.slice(8, 10)
  }
}, { immediate: true })

async function save() {
  errMsg.value = ''
  if (!form.value.date || !form.value.amount || !form.value.category) {
    errMsg.value = '날짜, 금액, 카테고리를 모두 입력해주세요.'; return
  }
  saving.value = true
  const payload = {
    user_id:  auth.user.id,
    type:     form.value.type,
    date:     form.value.date,
    amount:   Number(form.value.amount),
    category: form.value.category,
    memo:     form.value.memo || null,
  }
  if (form.value.id) {
    await supabase.from('household_ledger').update(payload).eq('id', form.value.id)
  } else {
    await supabase.from('household_ledger').insert(payload)
  }
  saving.value = false
  emit('saved')
}
</script>
