<template>
  <AppLayout>
    <div class="page">
      <div class="month-nav">
        <button class="mn-btn" @click="prevMonth"><i class="ti ti-chevron-left"></i></button>
        <div class="month-label">{{ year }}년 {{ month }}월</div>
        <button class="mn-btn" @click="nextMonth"><i class="ti ti-chevron-right"></i></button>
      </div>

      <!-- 카테고리별 지출 -->
      <div class="card">
        <div class="card-title"><i class="ti ti-chart-pie"></i> 카테고리별 지출</div>
        <div v-if="loading" class="loading">불러오는 중...</div>
        <div v-else-if="!expenseByCategory.length" class="empty">
          <i class="ti ti-chart-pie-off"></i><p>지출 내역이 없습니다</p>
        </div>
        <template v-else>
          <div v-for="item in expenseByCategory" :key="item.category" style="margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:4px">
              <span>{{ item.category }}</span>
              <span style="font-weight:600;color:var(--expense)">{{ item.amount.toLocaleString() }}원</span>
            </div>
            <div class="prog-bar">
              <div class="prog-fill" :style="{width: item.pct+'%', background:'var(--expense)'}"></div>
            </div>
          </div>
        </template>
      </div>

      <!-- 카테고리별 수입 -->
      <div class="card">
        <div class="card-title"><i class="ti ti-chart-bar"></i> 카테고리별 수입</div>
        <div v-if="loading" class="loading">불러오는 중...</div>
        <div v-else-if="!incomeByCategory.length" class="empty">
          <i class="ti ti-chart-bar-off"></i><p>수입 내역이 없습니다</p>
        </div>
        <template v-else>
          <div v-for="item in incomeByCategory" :key="item.category" style="margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;font-size:13px;margin-bottom:4px">
              <span>{{ item.category }}</span>
              <span style="font-weight:600;color:var(--income)">{{ item.amount.toLocaleString() }}원</span>
            </div>
            <div class="prog-bar">
              <div class="prog-fill" :style="{width: item.pct+'%', background:'var(--income)'}"></div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { supabase } from '@/supabase/client'
import { useAuthStore } from '@/stores/auth'

const auth  = useAuthStore()
const now   = new Date()
const year  = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const rows  = ref([])
const loading = ref(false)

function prevMonth() { if (month.value===1){month.value=12;year.value--}else month.value-- }
function nextMonth() { if (month.value===12){month.value=1;year.value++}else month.value++ }

function groupByCategory(type) {
  const filtered = rows.value.filter(r => r.type === type)
  const total = filtered.reduce((s, r) => s + r.amount, 0)
  const map = {}
  filtered.forEach(r => { map[r.category] = (map[r.category] || 0) + r.amount })
  return Object.entries(map)
    .map(([category, amount]) => ({ category, amount, pct: total ? Math.round(amount / total * 100) : 0 }))
    .sort((a, b) => b.amount - a.amount)
}

const expenseByCategory = computed(() => groupByCategory('expense'))
const incomeByCategory  = computed(() => groupByCategory('income'))

async function load() {
  if (!auth.user?.id) { rows.value = []; return }
  loading.value = true
  const from = `${year.value}-${String(month.value).padStart(2,'0')}-01`
  const lastDay = new Date(year.value, month.value, 0).getDate()
  const to   = `${year.value}-${String(month.value).padStart(2,'0')}-${lastDay}`
  const { data } = await supabase.from('household_ledger').select('*')
    .eq('user_id', auth.user.id)
    .gte('date', from).lte('date', to)
  rows.value = data ?? []
  loading.value = false
}

watch([year, month], load)
onMounted(load)
</script>
