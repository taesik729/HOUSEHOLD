<template>
  <AppLayout>
    <div class="page">

      <!-- 계정 정보 -->
      <div class="section-card">
        <div class="section-title"><i class="ti ti-user"></i> 계정 정보</div>
        <div class="info-row">
          <span class="info-label">이메일</span>
          <span class="info-val">{{ displayId }}</span>
        </div>
      </div>

      <!-- 카테고리 관리 -->
      <div class="section-card">
        <div class="section-title"><i class="ti ti-tag"></i> 카테고리 관리</div>

        <!-- 수입/지출 탭 -->
        <div class="cat-tabs">
          <button :class="['cat-tab', catType==='expense'?'active':'']" @click="catType='expense'">지출</button>
          <button :class="['cat-tab', catType==='income'?'active':'']"  @click="catType='income'">수입</button>
        </div>

        <div v-if="loading" class="loading" style="padding:16px">불러오는 중...</div>
        <div v-else>
          <!-- 카테고리 목록 -->
          <div v-for="cat in byType(catType)" :key="cat.id" class="cat-item">
            <div class="cat-icon-preview" :style="{background: cat.color + '20'}">{{ cat.icon }}</div>
            <div class="cat-name">{{ cat.name }}</div>
            <div style="display:flex;gap:4px;margin-left:auto">
              <button class="icon-btn" @click="openEdit(cat)"><i class="ti ti-edit"></i></button>
              <button class="icon-btn del" @click="deleteCategory(cat.id)"><i class="ti ti-trash"></i></button>
            </div>
          </div>

          <!-- 추가 버튼 -->
          <button class="cat-add-btn" @click="openAdd">
            <i class="ti ti-plus"></i> 카테고리 추가
          </button>
        </div>
      </div>

      <!-- 앱 정보 -->
      <div class="section-card">
        <div class="section-title"><i class="ti ti-info-circle"></i> 앱 정보</div>
        <div class="info-row">
          <span class="info-label">버전</span>
          <span class="info-val">0.1.0</span>
        </div>
      </div>

      <button class="btn-primary" style="margin:0 16px;width:calc(100% - 32px);background:var(--expense)" @click="logout">
        <i class="ti ti-logout"></i> 로그아웃
      </button>
      <button class="btn-withdraw" @click="showWithdraw = true">회원 탈퇴</button>
      <div style="text-align:center;margin-top:4px">
        <RouterLink to="/privacy" style="font-size:12px;color:var(--text-hint);text-decoration:underline">개인정보처리방침</RouterLink>
      </div>
    </div>

    <!-- 카테고리 추가/수정 모달 -->
    <div v-if="showCatModal" class="modal-overlay" @click.self="closeCatModal">
      <div class="modal">
        <div class="modal-hd">
          <div class="modal-title">{{ editCat ? '카테고리 수정' : '카테고리 추가' }}</div>
          <button class="icon-btn" @click="closeCatModal"><i class="ti ti-x"></i></button>
        </div>

        <div class="form-group">
          <label class="form-label">카테고리명</label>
          <input v-model="catForm.name" class="form-input" placeholder="예: 헬스·운동" />
        </div>

        <div class="form-group">
          <label class="form-label">아이콘</label>
          <div class="emoji-grid">
            <div v-for="e in EMOJIS" :key="e"
              :class="['emoji-dot', catForm.icon===e?'selected':'']"
              @click="catForm.icon=e">{{ e }}</div>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">색상</label>
          <div class="color-grid">
            <div v-for="c in COLORS" :key="c"
              :class="['color-dot', catForm.color===c?'selected':'']"
              :style="{background:c}"
              @click="catForm.color=c">
            </div>
          </div>
        </div>

        <!-- 미리보기 -->
        <div style="display:flex;align-items:center;gap:10px;padding:12px;background:var(--bg-sub);border-radius:var(--radius)">
          <div class="cat-icon-preview" :style="{background: catForm.color + '20'}">{{ catForm.icon }}</div>
          <span style="font-size:15px;font-weight:500">{{ catForm.name || '카테고리명' }}</span>
        </div>

        <p v-if="catError" style="font-size:13px;color:var(--expense)">{{ catError }}</p>

        <div class="modal-ft">
          <button class="btn-primary" @click="saveCat" :disabled="saving">
            {{ saving ? '저장 중...' : '저장' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 탈퇴 확인 모달 -->
    <div v-if="showWithdraw" class="modal-overlay" @click.self="showWithdraw = false">
      <div class="modal">
        <div class="modal-hd">
          <div class="modal-title">회원 탈퇴</div>
          <button class="icon-btn" @click="showWithdraw = false"><i class="ti ti-x"></i></button>
        </div>
        <div style="font-size:14px;color:var(--text-sub);line-height:1.7;padding:4px 0">
          탈퇴 시 <strong style="color:var(--expense)">모든 가계부 데이터가 삭제</strong>되며 복구할 수 없습니다.
        </div>
        <div class="form-group">
          <label class="form-label">확인을 위해 <strong style="color:var(--expense)">탈퇴</strong> 라고 입력하세요</label>
          <input v-model="confirmPw" type="text" class="form-input" placeholder="탈퇴" />
        </div>
        <p v-if="withdrawError" style="font-size:13px;color:var(--expense)">{{ withdrawError }}</p>
        <div class="modal-ft" style="display:flex;gap:8px">
          <button class="btn-secondary" style="flex:1" @click="showWithdraw = false">취소</button>
          <button class="btn-primary" style="flex:1;background:var(--expense)" @click="withdraw" :disabled="withdrawing">
            {{ withdrawing ? '처리 중...' : '탈퇴하기' }}
          </button>
        </div>
      </div>
    </div>

  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { useCategories } from '@/composables/useCategories.js'
import { supabase } from '@/supabase/client'

const auth   = useAuthStore()
const router = useRouter()
const { categories, loading, fetchCategories, addCategory, updateCategory, deleteCategory, byType } = useCategories()

const displayId   = computed(() => auth.user?.email ?? '')
const catType     = ref('expense')
const showCatModal = ref(false)
const editCat      = ref(null)
const catForm      = ref({ name: '', icon: '💳', color: '#FF6B6B' })
const catError     = ref('')
const saving       = ref(false)

const EMOJIS = [
  '🍚','🍔','🍽️','🍺','🎂','🍞','☕','🍜',
  '🚗','🚌','✈️','⛽','🚕','🚲','🛵','🚢',
  '💊','🏥','💉','🩺','🧴','🏃','💪','🧘',
  '📚','🎓','✏️','🖥️','📱','🎮','🎵','🎨',
  '🏠','🛒','💡','🔧','🧹','🛁','🛋️','🪴',
  '💰','💳','🏦','📈','💵','🎁','💸','🪙',
  '👗','👟','💄','💍','👜','🕶️','🧴','🪒',
  '🐶','🐱','⚽','🏊','🎭','📷','🌿','🌾',
]

const COLORS = [
  '#FF6B6B','#FF8E53','#FDCB6E','#00B894','#00CEC9',
  '#4ECDC4','#45B7D1','#0984E3','#6C5CE7','#FD79A8',
  '#55EFC4','#96CEB4','#B2BEC3','#636E72','#2D3436'
]

const showWithdraw  = ref(false)
const confirmPw     = ref('')
const withdrawing   = ref(false)
const withdrawError = ref('')

function openAdd() {
  editCat.value  = null
  catForm.value  = { name: '', icon: '💳', color: '#FF6B6B' }
  catError.value = ''
  showCatModal.value = true
}

function openEdit(cat) {
  editCat.value  = cat
  catForm.value  = { name: cat.name, icon: cat.icon, color: cat.color }
  catError.value = ''
  showCatModal.value = true
}

function closeCatModal() { showCatModal.value = false }

async function saveCat() {
  catError.value = ''
  if (!catForm.value.name.trim()) { catError.value = '카테고리명을 입력해주세요.'; return }
  if (!catForm.value.icon.trim()) { catError.value = '아이콘을 입력해주세요.'; return }
  saving.value = true
  if (editCat.value) {
    await updateCategory(editCat.value.id, catForm.value.name, catForm.value.icon, catForm.value.color)
  } else {
    await addCategory(catType.value, catForm.value.name, catForm.value.icon, catForm.value.color)
  }
  saving.value = false
  closeCatModal()
}

async function logout() {
  await auth.logout()
  router.push('/login')
}

async function withdraw() {
  withdrawError.value = ''
  if (confirmPw.value.trim() !== '탈퇴') {
    withdrawError.value = '"탈퇴" 라고 정확히 입력해주세요.'; return
  }
  withdrawing.value = true
  await supabase.from('household_ledger').delete().eq('user_id', auth.user.id)
  await supabase.from('household_categories').delete().eq('user_id', auth.user.id)
  await supabase.rpc('delete_user')
  try { await auth.logout() } catch (e) {}
  auth.user = null
  router.push('/login')
}

onMounted(fetchCategories)
</script>

<style scoped>
.section-card  { background: var(--bg-card); margin: 0; border-bottom: 1px solid var(--border); padding: 16px 20px; }
.section-title { font-size: 13px; font-weight: 600; color: var(--text-hint); margin-bottom: 12px;
                 display: flex; align-items: center; gap: 6px; text-transform: uppercase; letter-spacing: .05em; }
.info-row      { display: flex; justify-content: space-between; align-items: center; padding: 4px 0; }
.info-label    { font-size: 14px; color: var(--text-sub); }
.info-val      { font-size: 14px; font-weight: 500; color: var(--text); }

.cat-tabs      { display: flex; gap: 0; border: 1.5px solid var(--border-md); border-radius: var(--radius);
                 overflow: hidden; margin-bottom: 14px; }
.cat-tab       { flex: 1; padding: 9px; text-align: center; font-size: 14px; font-weight: 500;
                 border: none; background: none; color: var(--text-sub); cursor: pointer; }
.cat-tab.active { background: var(--primary-mid); color: #fff; }

.cat-item      { display: flex; align-items: center; gap: 10px; padding: 10px 0;
                 border-bottom: 1px solid var(--border); }
.cat-item:last-of-type { border-bottom: none; }
.cat-icon-preview { width: 38px; height: 38px; border-radius: 50%;
                    display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0; }
.cat-name      { font-size: 14px; font-weight: 500; }
.cat-add-btn   { width: 100%; padding: 10px; margin-top: 8px; border: 1.5px dashed var(--border-md);
                 border-radius: var(--radius); background: none; color: var(--primary-bright);
                 font-size: 14px; cursor: pointer; display: flex; align-items: center;
                 justify-content: center; gap: 6px; }
.cat-add-btn:hover { background: var(--primary-light); }

.emoji-grid    { display: grid; grid-template-columns: repeat(8, 1fr); gap: 4px; }
.emoji-dot     { width: 38px; height: 38px; border-radius: 8px; cursor: pointer; font-size: 20px;
                 display: flex; align-items: center; justify-content: center;
                 border: 2px solid transparent; transition: background .1s; }
.emoji-dot:hover    { background: var(--bg-sub); }
.emoji-dot.selected { border-color: var(--primary-mid); background: var(--primary-light); }

.color-grid    { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; }
.color-dot     { width: 32px; height: 32px; border-radius: 50%; cursor: pointer;
                 transition: transform .15s; border: 2px solid transparent; }
.color-dot:hover    { transform: scale(1.15); }
.color-dot.selected { border-color: var(--text); transform: scale(1.15); }

.btn-withdraw  { background: none; border: none; color: var(--text-hint); font-size: 13px;
                 text-decoration: underline; cursor: pointer; text-align: center;
                 padding: 12px; width: 100%; }
.btn-withdraw:hover { color: var(--expense); }
</style>
