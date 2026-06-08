<template>
  <AppLayout>
    <div class="page">
      <!-- 월 네비 -->
      <div class="month-nav">
        <button class="mn-btn" @click="prevMonth"><i class="ti ti-chevron-left"></i></button>
        <div class="month-label">{{ year }}년 {{ month }}월</div>
        <button class="mn-btn" @click="nextMonth"><i class="ti ti-chevron-right"></i></button>
      </div>

      <!-- 요약 카드 -->
      <div class="summary-grid">
        <div class="sum-card income">
          <div class="sum-label"><i class="ti ti-trending-up"></i> 수입</div>
          <div class="sum-val">{{ totalIncome.toLocaleString() }}<span class="sum-unit">원</span></div>
        </div>
        <div class="sum-card expense">
          <div class="sum-label"><i class="ti ti-trending-down"></i> 지출</div>
          <div class="sum-val">{{ totalExpense.toLocaleString() }}<span class="sum-unit">원</span></div>
        </div>
        <div class="sum-card balance">
          <div class="sum-label"><i class="ti ti-wallet"></i> 잔액</div>
          <div class="sum-val">{{ balance.toLocaleString() }}<span class="sum-unit">원</span></div>
        </div>
      </div>

      <!-- 최근 내역 -->
      <div class="card">
        <div class="card-title"><i class="ti ti-clock"></i> 최근 내역</div>
        <div v-if="loading" class="loading">불러오는 중...</div>
        <div v-else-if="!recent.length" class="empty">
          <i class="ti ti-receipt-off"></i>
          <p>이번 달 내역이 없습니다</p>
        </div>
        <template v-else>
          <div v-for="r in recent" :key="r.id" class="ledger-item">
            <div :class="['li-dot', r.type]"></div>
            <div class="li-body">
              <div class="li-cat">{{ r.category }}</div>
              <div v-if="r.memo" class="li-memo">{{ r.memo }}</div>
            </div>
            <div class="li-right">
              <div :class="['li-amount', r.type]">
                {{ r.type === 'income' ? '+' : '-' }}{{ r.amount.toLocaleString() }}원
              </div>
              <div class="li-date">{{ r.date }}</div>
            </div>
          </div>
          <RouterLink to="/ledger" style="display:block;text-align:center;margin-top:12px;font-size:13px;color:var(--primary-bright);text-decoration:none">
            전체 내역 보기 →
          </RouterLink>
        </template>
      </div>
    </div>

    <!-- FAB -->
    <button class="btn-fab" @click="showModal = true">
      <i class="ti ti-plus"></i>
    </button>

    <!-- 입력 모달 -->
    <LedgerModal v-if="showModal" @close="showModal = false" @saved="onSaved" />
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { RouterLink } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import LedgerModal from '@/components/LedgerModal.vue'
import { supabase } from '@/supabase/client'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const now = new Date()
const year  = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const rows    = ref([])
const loading = ref(false)
const showModal = ref(false)

function prevMonth() {
  if (month.value === 1) { month.value = 12; year.value-- }
  else month.value--
}
function nextMonth() {
  if (month.value === 12) { month.value = 1; year.value++ }
  else month.value++
}

const totalIncome  = computed(() => rows.value.filter(r => r.type === 'income').reduce((s, r) => s + r.amount, 0))
const totalExpense = computed(() => rows.value.filter(r => r.type === 'expense').reduce((s, r) => s + r.amount, 0))
const balance      = computed(() => totalIncome.value - totalExpense.value)
const recent       = computed(() => [...rows.value].sort((a,b) => b.date.localeCompare(a.date)).slice(0, 5))

async function load() {
  loading.value = true
  const from = `${year.value}-${String(month.value).padStart(2,'0')}-01`
  const lastDay = new Date(year.value, month.value, 0).getDate()
  const to   = `${year.value}-${String(month.value).padStart(2,'0')}-${lastDay}`
  const { data } = await supabase
    .from('household_ledger')
    .select('*')
    .eq('user_id', auth.user?.id)
    .gte('date', from).lte('date', to)
    .order('date', { ascending: false })
  rows.value = data ?? []
  loading.value = false
}

function onSaved() { showModal.value = false; load() }

watch([year, month], load)
onMounted(load)
</script>
