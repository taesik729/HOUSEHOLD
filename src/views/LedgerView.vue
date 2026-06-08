<template>
  <AppLayout>
    <div class="page">

      <!-- 월 헤더 -->
      <div class="month-header">
        <div class="month-nav">
          <button class="mn-btn" @click="prevMonth"><i class="ti ti-chevron-left"></i></button>
          <div class="month-label">{{ year }}년 {{ month }}월</div>
          <button class="mn-btn" @click="nextMonth"><i class="ti ti-chevron-right"></i></button>
        </div>
        <div class="summary-row">
          <div class="sum-box expense">
            <div class="sum-box-label">지출</div>
            <div class="sum-box-val">{{ totalExpense.toLocaleString() }}원</div>
          </div>
          <div class="sum-box income">
            <div class="sum-box-label">수입</div>
            <div class="sum-box-val">{{ totalIncome.toLocaleString() }}원</div>
          </div>
        </div>
      </div>

      <!-- 필터 -->
      <div class="filter-bar">
        <button v-for="t in TYPES" :key="t.val"
          :class="['f-tab', typeFilter===t.val?'active':'']"
          @click="typeFilter=t.val">{{ t.label }}</button>
      </div>

      <!-- 내역 -->
      <div v-if="loading" class="loading">불러오는 중...</div>
      <div v-else-if="!filtered.length" class="empty">
        <i class="ti ti-receipt-off"></i>
        <p>내역이 없습니다</p>
      </div>
      <template v-else>
        <div v-for="group in grouped" :key="group.date" class="date-group">
          <div class="date-group-hd">
            <span>{{ formatDate(group.date) }}</span>
            <span class="date-group-total">
              <span v-if="group.income" style="color:var(--income)">+{{ group.income.toLocaleString() }}</span>
              <span v-if="group.income && group.expense"> · </span>
              <span v-if="group.expense" style="color:var(--expense)">-{{ group.expense.toLocaleString() }}</span>
            </span>
          </div>
          <div v-for="r in group.items" :key="r.id" class="tx-item" @click="openEdit(r)">
            <div class="tx-icon" :style="{background: getCatInfo(r.category).color + '20'}">
              {{ getCatInfo(r.category).icon }}
            </div>
            <div class="tx-body">
              <div class="tx-name">{{ r.category }}</div>
              <div v-if="r.memo" class="tx-memo">{{ r.memo }}</div>
            </div>
            <div class="tx-right">
              <div :class="['tx-amount', r.type]">
                {{ r.type==='income' ? '+' : '-' }}{{ r.amount.toLocaleString() }}원
              </div>
            </div>
            <div class="tx-actions" @click.stop>
              <button class="icon-btn" @click="openEdit(r)"><i class="ti ti-edit"></i></button>
              <button class="icon-btn del" @click="remove(r.id)"><i class="ti ti-trash"></i></button>
            </div>
          </div>
        </div>
      </template>

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
import { formatDate } from '@/utils/category.js'
import { useCategories } from '@/composables/useCategories.js'

const auth = useAuthStore()
const { fetchCategories, getCatInfo } = useCategories()
const now   = new Date()
const year  = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const rows  = ref([])
const loading    = ref(false)
const showModal  = ref(false)
const editRow    = ref(null)
const typeFilter = ref('all')

const TYPES = [{ val:'all', label:'전체' }, { val:'income', label:'수입' }, { val:'expense', label:'지출' }]

const filtered = computed(() =>
  typeFilter.value === 'all' ? rows.value : rows.value.filter(r => r.type === typeFilter.value)
)

const totalIncome  = computed(() => rows.value.filter(r=>r.type==='income').reduce((s,r)=>s+r.amount,0))
const totalExpense = computed(() => rows.value.filter(r=>r.type==='expense').reduce((s,r)=>s+r.amount,0))

const grouped = computed(() => {
  const map = {}
  filtered.value.forEach(r => {
    if (!map[r.date]) map[r.date] = { date: r.date, items: [], income: 0, expense: 0 }
    map[r.date].items.push(r)
    if (r.type === 'income')  map[r.date].income  += r.amount
    if (r.type === 'expense') map[r.date].expense += r.amount
  })
  return Object.values(map).sort((a,b) => b.date.localeCompare(a.date))
})

function prevMonth() { if (month.value===1){month.value=12;year.value--}else month.value-- }
function nextMonth() { if (month.value===12){month.value=1;year.value++}else month.value++ }

async function load() {
  if (!auth.user?.id) { rows.value = []; return }
  loading.value = true
  const from = `${year.value}-${String(month.value).padStart(2,'0')}-01`
  const lastDay = new Date(year.value, month.value, 0).getDate()
  const to = `${year.value}-${String(month.value).padStart(2,'0')}-${lastDay}`
  const { data } = await supabase.from('household_ledger').select('*')
    .eq('user_id', auth.user.id)
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
onMounted(async () => { await fetchCategories(); await load() })
</script>
