import { ref } from 'vue'
import { supabase } from '@/supabase/client'
import { useAuthStore } from '@/stores/auth'

// 기본 카테고리 (신규 가입자용)
const DEFAULT_CATS = [
  { type: 'expense', name: '식비',        icon: '🍚', color: '#FF6B6B', sort_order: 1 },
  { type: 'expense', name: '외식·여가',   icon: '🍽️', color: '#FF8E53', sort_order: 2 },
  { type: 'expense', name: '교통·유류',   icon: '🚗', color: '#4ECDC4', sort_order: 3 },
  { type: 'expense', name: '의료비',      icon: '💊', color: '#45B7D1', sort_order: 4 },
  { type: 'expense', name: '교육비',      icon: '📚', color: '#96CEB4', sort_order: 5 },
  { type: 'expense', name: '공과금·통신', icon: '📱', color: '#6C5CE7', sort_order: 6 },
  { type: 'expense', name: '의류·잡화',   icon: '👗', color: '#FD79A8', sort_order: 7 },
  { type: 'expense', name: '경조사',      icon: '🎁', color: '#FDCB6E', sort_order: 8 },
  { type: 'expense', name: '기타지출',    icon: '💸', color: '#B2BEC3', sort_order: 9 },
  { type: 'income',  name: '농업수입',    icon: '🌾', color: '#00B894', sort_order: 1 },
  { type: 'income',  name: '급여·연금',   icon: '💰', color: '#00CEC9', sort_order: 2 },
  { type: 'income',  name: '임대·이자',   icon: '🏦', color: '#0984E3', sort_order: 3 },
  { type: 'income',  name: '용돈',        icon: '💵', color: '#6C5CE7', sort_order: 4 },
  { type: 'income',  name: '기타수입',    icon: '📈', color: '#55EFC4', sort_order: 5 },
]

export function useCategories() {
  const auth       = useAuthStore()
  const categories = ref([])
  const loading    = ref(false)

  async function fetchCategories() {
    if (!auth.user?.id) return
    loading.value = true
    const { data } = await supabase
      .from('household_categories')
      .select('*')
      .eq('user_id', auth.user.id)
      .order('sort_order')
    // 카테고리가 없으면 기본값 자동 생성
    if (!data || data.length === 0) {
      await initDefaults()
      const { data: d2 } = await supabase
        .from('household_categories').select('*')
        .eq('user_id', auth.user.id).order('sort_order')
      categories.value = d2 ?? []
    } else {
      categories.value = data
    }
    loading.value = false
  }

  async function initDefaults() {
    const rows = DEFAULT_CATS.map(c => ({ ...c, user_id: auth.user.id }))
    await supabase.from('household_categories').insert(rows)
  }

  async function addCategory(type, name, icon, color) {
    const maxOrder = categories.value
      .filter(c => c.type === type)
      .reduce((m, c) => Math.max(m, c.sort_order), 0)
    await supabase.from('household_categories').insert({
      user_id: auth.user.id, type, name, icon, color, sort_order: maxOrder + 1
    })
    await fetchCategories()
  }

  async function updateCategory(id, name, icon, color) {
    await supabase.from('household_categories').update({ name, icon, color }).eq('id', id)
    await fetchCategories()
  }

  async function deleteCategory(id) {
    await supabase.from('household_categories').delete().eq('id', id)
    await fetchCategories()
  }

  function getCatInfo(name) {
    const c = categories.value.find(c => c.name === name)
    return c ? { icon: c.icon, color: c.color } : { icon: '💳', color: '#B2BEC3' }
  }

  function byType(type) {
    return categories.value.filter(c => c.type === type)
  }

  return { categories, loading, fetchCategories, addCategory, updateCategory, deleteCategory, getCatInfo, byType }
}
