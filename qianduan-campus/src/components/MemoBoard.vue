<template>
  <section class="memo-board" v-loading="loading">
    <div class="memo-mascot" aria-hidden="true">
      <img class="memo-mascot-image" :src="memoMascot" alt="" />
    </div>

    <div class="memo-header">
      <div>
        <p class="memo-eyebrow">QUICK NOTES</p>
        <h3 class="memo-title">备忘录</h3>
      </div>
      <div class="memo-badge">{{ pendingCount }} 待办</div>
    </div>

    <div class="memo-composer">
      <el-input
        v-model="draft"
        type="textarea"
        resize="none"
        :rows="3"
        maxlength="120"
        show-word-limit
        placeholder="记下一条想法、待办事项，或者今天准备完成的小目标"
        @keydown.ctrl.enter.prevent="handleAddMemo"
      />

      <div class="memo-composer-footer">
        <span class="memo-tip">Ctrl + Enter 可快速添加</span>
        <el-button
          class="memo-add-btn"
          type="primary"
          :icon="Plus"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleAddMemo"
        >
          添加
        </el-button>
      </div>
    </div>

    <div v-if="!userStore.isLogin()" class="memo-login-empty">
      <el-empty description="登录后可创建备忘录并同步到你的账号" />
    </div>

    <div v-else-if="sortedMemos.length" class="memo-list">
      <article
        v-for="memo in sortedMemos"
        :key="memo.id"
        class="memo-item"
        :class="{ done: memo.status === 1, pinned: memo.isPinned === 1 }"
      >
        <button
          class="memo-toggle"
          type="button"
          :aria-label="memo.status === 1 ? '标记为未完成' : '标记为已完成'"
          @click="toggleStatus(memo)"
        >
          <span class="memo-toggle-dot"></span>
        </button>

        <div class="memo-body">
          <p class="memo-text">{{ memo.content }}</p>
          <div class="memo-meta">
            <span>{{ memo.isPinned === 1 ? '已置顶' : '未置顶' }}</span>
            <span>{{ formatTime(memo.updateTime) }}</span>
          </div>
        </div>

        <div class="memo-actions">
          <el-button
            circle
            text
            :type="memo.isPinned === 1 ? 'warning' : 'info'"
            :icon="memo.isPinned === 1 ? StarFilled : Star"
            @click="togglePin(memo)"
          />
          <el-button
            circle
            text
            type="danger"
            :icon="Delete"
            @click="removeMemo(memo.id)"
          />
        </div>
      </article>
    </div>

    <el-empty v-else description="还没有备忘录，先记下一件想做的小事吧" />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Delete, Plus, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { memoAPI } from '@/api/memo'
import memoMascot from '@/assets/lxh_044_hd.gif'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const draft = ref('')
const memos = ref([])
const loading = ref(false)
const submitting = ref(false)

const canSubmit = computed(() => {
  return !!draft.value.trim() && !submitting.value
})

const sortedMemos = computed(() => {
  return [...memos.value].sort((a, b) => {
    if (a.isPinned !== b.isPinned) {
      return b.isPinned - a.isPinned
    }

    if (a.status !== b.status) {
      return a.status - b.status
    }

    return Number(b.updateTime) - Number(a.updateTime)
  })
})

const pendingCount = computed(() => {
  return memos.value.filter(memo => memo.status === 0).length
})

const normalizeMemo = (memo) => ({
  ...memo,
  id: Number(memo.id),
  status: Number(memo.status),
  isPinned: Number(memo.isPinned),
  createTime: Number(memo.createTime),
  updateTime: Number(memo.updateTime)
})

const loadMemos = async () => {
  if (!userStore.isLogin()) {
    memos.value = []
    return
  }

  loading.value = true

  try {
    const data = await memoAPI.getMemoList()
    memos.value = Array.isArray(data) ? data.map(normalizeMemo) : []
  } catch (error) {
    memos.value = []
    console.error('加载备忘录失败', error)
  } finally {
    loading.value = false
  }
}

const handleAddMemo = async () => {
  const content = draft.value.trim()

  if (!content || submitting.value) {
    return
  }

  submitting.value = true

  try {
    const data = await memoAPI.addMemo({ content })
    memos.value.unshift(normalizeMemo(data))
    draft.value = ''
    ElMessage.success('添加成功')
  } catch (error) {
    console.error('添加备忘录失败', error)
  } finally {
    submitting.value = false
  }
}

const replaceMemo = (nextMemo) => {
  const normalized = normalizeMemo(nextMemo)
  memos.value = memos.value.map(memo => (memo.id === normalized.id ? normalized : memo))
}

const toggleStatus = async (memo) => {
  try {
    const data = await memoAPI.updateMemoStatus(memo.id, {
      status: memo.status === 1 ? 0 : 1
    })
    replaceMemo(data)
  } catch (error) {
    console.error('更新备忘录状态失败', error)
  }
}

const togglePin = async (memo) => {
  try {
    const data = await memoAPI.updateMemoPin(memo.id, {
      isPinned: memo.isPinned === 1 ? 0 : 1
    })
    replaceMemo(data)
  } catch (error) {
    console.error('更新备忘录置顶状态失败', error)
  }
}

const removeMemo = async (id) => {
  try {
    await memoAPI.deleteMemo(id)
    memos.value = memos.value.filter(memo => memo.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除备忘录失败', error)
  }
}

const formatTime = (timestamp) => {
  const date = new Date(Number(timestamp))
  if (Number.isNaN(date.getTime())) {
    return '--'
  }

  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  return `${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  loadMemos()
})
</script>

<style scoped>
.memo-board {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  gap: 18px;
  width: 100%;
  padding: 36px 22px 22px;
  box-sizing: border-box;
  border-radius: 26px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(242, 249, 255, 0.94) 100%);
  box-shadow:
    inset 0 0 0 1px rgba(215, 233, 247, 0.85),
     0 18px 34px rgba(170, 204, 230, 0.14);
}

.memo-mascot {
  position: absolute;
  top: 18px;
  right: 118px;
  width: 116px;
  height: 92px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  pointer-events: none;
  z-index: 0;
}

.memo-mascot-image {
  display: block;
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 10px 18px rgba(139, 175, 204, 0.14));
}

.memo-header {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.memo-eyebrow {
  margin: 0 0 6px;
  color: #84acd1;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.memo-title {
  margin: 0;
  color: #1f2a36;
  font-size: 28px;
  font-weight: 800;
}

.memo-badge {
  flex-shrink: 0;
  padding: 8px 12px;
  border-radius: 999px;
  color: #5d93c7;
  font-size: 13px;
  font-weight: 700;
  background: rgba(230, 243, 255, 0.92);
  box-shadow: inset 0 0 0 1px rgba(198, 223, 244, 0.9);
}

.memo-composer {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.memo-composer :deep(.el-textarea__inner) {
  min-height: 96px;
  border-radius: 18px;
  border-color: rgba(210, 229, 244, 0.9);
  background: rgba(252, 254, 255, 0.98);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
  color: #304153;
  line-height: 1.7;
}

.memo-composer :deep(.el-input__count) {
  background: transparent;
  color: #8aa4be;
}

.memo-composer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.memo-tip {
  color: #8ea5bb;
  font-size: 12px;
}

.memo-add-btn {
  --el-button-bg-color: #5f9fe0;
  --el-button-border-color: #5f9fe0;
  --el-button-hover-bg-color: #4f93d7;
  --el-button-hover-border-color: #4f93d7;
  --el-button-active-bg-color: #427fbe;
  --el-button-active-border-color: #427fbe;
  box-shadow: 0 12px 22px rgba(95, 159, 224, 0.26);
}

.memo-list {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  gap: 12px;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
  scrollbar-gutter: stable;
}

.memo-list::-webkit-scrollbar {
  width: 8px;
}

.memo-list::-webkit-scrollbar-track {
  border-radius: 999px;
  background: rgba(233, 242, 250, 0.72);
}

.memo-list::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(138, 181, 219, 0.78);
}

.memo-list::-webkit-scrollbar-thumb:hover {
  background: rgba(118, 165, 207, 0.92);
}

.memo-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: inset 0 0 0 1px rgba(219, 236, 248, 0.9);
  transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}

.memo-item:hover {
  transform: translateY(-2px);
  box-shadow:
    inset 0 0 0 1px rgba(210, 231, 246, 0.95),
    0 14px 24px rgba(171, 203, 227, 0.14);
}

.memo-item.done {
  background: rgba(246, 250, 253, 0.9);
}

.memo-item.done .memo-text {
  color: #89a0b6;
  text-decoration: line-through;
}

.memo-item.pinned {
  background:
    linear-gradient(180deg, rgba(255, 252, 244, 0.96) 0%, rgba(255, 248, 231, 0.9) 100%);
  box-shadow: inset 0 0 0 1px rgba(243, 223, 178, 0.95);
}

.memo-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  margin-top: 2px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: rgba(230, 243, 255, 0.9);
  cursor: pointer;
}

.memo-toggle-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #73addf;
  transition: all 0.25s ease;
}

.memo-item.done .memo-toggle-dot {
  background: #73addf;
  box-shadow: 0 0 0 4px rgba(115, 173, 223, 0.18);
}

.memo-body {
  min-width: 0;
}

.memo-text {
  margin: 0;
  color: #243447;
  font-size: 14px;
  line-height: 1.75;
  word-break: break-word;
}

.memo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: #8ba2b7;
  font-size: 12px;
}

.memo-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

@media (max-width: 900px) {
  .memo-board {
    height: auto;
    min-height: 0;
    padding: 18px;
    border-radius: 22px;
  }

  .memo-login-empty {
    position: relative;
    z-index: 1;
    display: flex;
    flex: 1 1 auto;
    align-items: center;
    justify-content: center;
    min-height: 220px;
  }

  .memo-list {
    flex: initial;
    max-height: 332px;
    padding-right: 2px;
  }

  .memo-title {
    font-size: 24px;
  }

  .memo-mascot {
    display: none;
  }

  .memo-composer-footer {
    flex-direction: column;
    align-items: stretch;
  }

  .memo-tip {
    order: 2;
  }

  .memo-add-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .memo-board {
    padding: 16px 14px;
    border-radius: 18px;
  }

  .memo-title {
    font-size: 22px;
  }

  .memo-list {
    max-height: 280px;
  }

  .memo-item {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .memo-actions {
    grid-column: 2;
    justify-content: flex-start;
    gap: 6px;
  }
}
</style>
