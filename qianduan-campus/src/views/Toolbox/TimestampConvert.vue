<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">时间戳转换</h1>
          <p class="tool-desc">支持秒级、毫秒级时间戳和日期时间双向转换，也可以一键获取当前时间。</p>
        </div>
      </div>

      <div class="tool-panels single-column">
        <section class="tool-card">
          <div class="field-row">
            <label class="field-label">时间戳</label>
            <el-input v-model="timestampInput" placeholder="请输入 10 位或 13 位时间戳" />
          </div>
          <div class="field-row">
            <label class="field-label">日期时间</label>
            <el-input v-model="dateInput" placeholder="例如：2026-06-25 12:30:00" />
          </div>
          <div class="button-row">
            <el-button type="primary" @click="timestampToDate">时间戳 → 日期</el-button>
            <el-button @click="dateToTimestamp">日期 → 时间戳</el-button>
            <el-button @click="fillNow">当前时间</el-button>
            <el-button @click="copyResult">复制结果</el-button>
            <el-button @click="clearAll">清空</el-button>
          </div>
          <div class="result-box">
            <p class="result-label">转换结果</p>
            <p class="result-value">{{ result || '这里会显示转换结果' }}</p>
          </div>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const timestampInput = ref('')
const dateInput = ref('')
const result = ref('')

const formatDate = (date) => {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const timestampToDate = () => {
  const raw = timestampInput.value.trim()
  if (!raw) {
    ElMessage.warning('请输入时间戳')
    return
  }

  const numeric = Number(raw)
  if (Number.isNaN(numeric)) {
    ElMessage.error('时间戳格式不正确')
    return
  }

  const date = raw.length === 10 ? new Date(numeric * 1000) : new Date(numeric)
  result.value = formatDate(date)
}

const dateToTimestamp = () => {
  const raw = dateInput.value.trim()
  if (!raw) {
    ElMessage.warning('请输入日期时间')
    return
  }

  const date = new Date(raw.replace(/-/g, '/'))
  if (Number.isNaN(date.getTime())) {
    ElMessage.error('日期格式不正确')
    return
  }

  result.value = `${date.getTime()} ms / ${Math.floor(date.getTime() / 1000)} s`
}

const fillNow = () => {
  const now = new Date()
  timestampInput.value = String(now.getTime())
  dateInput.value = formatDate(now)
  result.value = `${now.getTime()} ms / ${Math.floor(now.getTime() / 1000)} s`
}

const copyResult = async () => {
  if (!result.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }

  await navigator.clipboard.writeText(result.value)
  ElMessage.success('已复制结果')
}

const clearAll = () => {
  timestampInput.value = ''
  dateInput.value = ''
  result.value = ''
}
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';
</style>
