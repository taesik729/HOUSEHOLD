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

      <!-- 날짜 / 금액 -->
      <div class="form-grid2">
        <div class="form-group">
          <label class="form-label">날짜</label>
          <input v-model="form.date" type="date" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">금액 (원)</label>
          <input v-model="form.amount" type="number" class="form-input" placeholder="0" />
        </div>
      </div>

      <!-- 카테고리 -->
      <div class="form-group">
        <label class="form-label">카테고리</label>
        <select v-model="form.category" class="form-input">
          <option value="">선택</option>
          <template v-if="form.type==='income'">
            <option v-for="c in incomeCats" :key="c.id" :value="c.name">{{ c.icon }} {{ c.name }}</option>
          </template>
          <template v-else>
            <option v-for="c in expenseCats" :key="c.id" :value="c.name">{{ c.icon }} {{ c.name }}</option>
          </template>
        </select>
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

watch(() => props.editRow, (r) => {
  if (r) form.value = { ...r }
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
