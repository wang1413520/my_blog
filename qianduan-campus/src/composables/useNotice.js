import { ref } from 'vue'
import { noticeAPI } from '@/api/notice'

// localStorage 键：
//  notice_closed_date —— "今日关闭"记录的日期（YYYY-MM-DD）
//  notice_seen_version —— 已读过的公告版本号（update_time）
const CLOSED_DATE_KEY = 'notice_closed_date'
const SEEN_VERSION_KEY = 'notice_seen_version'

// 模块顶层 ref：全站共享一份弹窗状态（单例）
const visible = ref(false)
const loading = ref(false)
const notice = ref(null)
// 铃铛红点：有未读（今天未关闭或版本有更新）时为 true
const hasNew = ref(false)

function todayStr() {
  const d = new Date()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

// 版本号 = update_time；后端新增/编辑公告时都会刷新 update_time，
// 因此“已读版本 != 最新版本”即可触发重新弹出
function getVersion(item) {
  return (item && (item.updateTime || item.createTime)) || ''
}

async function loadActiveNotice() {
  if (loading.value) return
  loading.value = true
  try {
    const data = await noticeAPI.getActiveNotices()
    // 兼容后端返回数组或 {records: []} 分页结构
    const list = Array.isArray(data) ? data : (data?.records || [])
    notice.value = list[0] || null
  } catch (err) {
    // 接口失败时静默处理，不阻塞首页
    notice.value = null
  } finally {
    loading.value = false
  }
}

// 自动检查（进入首页时调用）：今日已关闭 且 版本已读 → 不再弹；
// 否则弹出最新公告
async function autoCheck() {
  if (!notice.value) {
    await loadActiveNotice()
  }
  const item = notice.value
  if (!item) {
    hasNew.value = false
    return
  }
  const version = getVersion(item)
  const closedToday = localStorage.getItem(CLOSED_DATE_KEY) === todayStr()
  const seen = !!version && localStorage.getItem(SEEN_VERSION_KEY) === version
  if (closedToday && seen) {
    hasNew.value = false
    return
  }
  hasNew.value = true
  visible.value = true
}

// 用户点击铃铛主动查看：不受“今日关闭”限制
function openDialog() {
  if (!notice.value) {
    loadActiveNotice()
  }
  visible.value = true
  hasNew.value = false
}

// “今日关闭”：记录日期 + 记录已读版本
function closeToday() {
  const version = getVersion(notice.value)
  localStorage.setItem(CLOSED_DATE_KEY, todayStr())
  if (version) {
    localStorage.setItem(SEEN_VERSION_KEY, version)
  }
  hasNew.value = false
  visible.value = false
}

// 普通关闭（点 X / 遮罩）：今天之内仍会再次自动弹出
function closeDialog() {
  visible.value = false
}

export function useNotice() {
  return {
    visible,
    loading,
    notice,
    hasNew,
    autoCheck,
    openDialog,
    closeToday,
    closeDialog
  }
}
