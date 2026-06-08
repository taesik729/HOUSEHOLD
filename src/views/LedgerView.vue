<template>
  <AppLayout>
    <div class="page">
      <!-- 월 네비 -->
      <div class="month-nav">
        <button class="mn-btn" @click="prevMonth"><i class="ti ti-chevron-left"></i></button>
        <div class="month-label">{{ year }}년 {{ month }}월</div>
        <button class="mn-btn" @click="nextMonth"><i class="ti ti-chevron-right"></i></button>
      </div>

      <!-- 필터 -->
      <div class="filter-tabs">
        <button v-for="t in TYPES" :key="t.val"
          :class="['f-tab', typeFilter===t.val?'active':'']"
          @click="typeFilter=t.val">{{ t.label }}</button>
      </div>

      <!-- 내역 카드 -->
      <div class="card" style="padding:0 18px">
        <div v-if="loading" class="loading">불러오는 중...</div>
        <div v-else-if="!filtered.length" class="empty">
          <i class="ti ti-receipt-off"></i>
          <p>내역이 없습니다</p>
        </div>
        <div v-else v-for="r in filtered" :key="r.id" class="ledger-item">
          <div :class="['li-dot', r.type]"></div>
          <div class="li-body">
            <div class="li-cat">{{ r.category }}</div>
            <div v-if="r.memo" class="li-memo">{{ r.memo }}</div>
          </div>
          <div class="li-right">
            <div :class="['li-amount', r.type]">
              {{ r.type==='income' ? '+' : '-' }}{{ r.amount.toLocaleString() }}원
            </div>
            <div class="li-date">{{ r.date }}</div>
          </div>
          <div class="li-actions">
            <button class="icon-btn" @click="openEdit(r)"><i class="ti ti-edit"></i></button>
            <button class="icon-btn del" @click="remove(r.id)"><i class="ti ti-trash"></i></button>
          </div>
        </div>
      </div>
    </div>

    <button class="btn-fab" @click="showModal=true"><i class="ti ti-plus"></i></button>
    <LedgerModal v-if="showModal" :edit-row="editRow" @close="closeModal" @saved="onSaved" />
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import LedgerModal from '@/components/LedgerModal.vue'
import { supabase } from '@/supabase/client'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const now   = new Date()
const year  = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const rows  = ref([])
const loading   = ref(false)
const showModal = ref(false)
const editRow   = ref(null)
const typeFilter = ref('all')

const TYPES = [{ val: 'all', label: '전체' }, { val: 'income', label: '수입' }, { val: 'expense', label: '지출' }]

const filtered = computed(() =>
  typeFilter.value === 'all' ? rows.value : rows.value.filter(r => r.type === typeFilter.value)
)

function prevMonth() { if (month.value===1){month.value=12;year.value--}else month.value-- }
function nextMonth() { if (month.value===12){month.value=1;year.value++}else month.value++ }

async function load() {
  loading.value = true
  const from = `${year.value}-${String(month.value).padStart(2,'0')}-01`
  const lastDay = new Date(year.value, month.value, 0).getDate()
  const to   = `${year.value}-${String(month.value).padStart(2,'0')}-${lastDay}`
  const { data } = await supabase.from('household_ledger').select('*')
    .gte('date', from).lte('date', to)
    .order('date', { ascending: false })
  rows.value = data ?? []
  loading.value = false
}

async function remove(id) {
  if (!confirm('삭제하시겠습니까?')) return
  await supabase.from('household_ledger').delete().eq('id', id)
  load()
}

function openEdit(r)  { editRow.value = { ...r }; showModal.value = true }
function closeModal() { showModal.value = false; editRow.value = null }
function onSaved()    { closeModal(); load() }

watch([year, month], load)
onMounted(load)
</script>
