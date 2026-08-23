<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">Base64 编解码</h1>
          <p class="tool-desc">支持文本 Base64 编解码，也支持本地文件转 Base64，不上传服务器。</p>
        </div>
      </div>

      <DualPanelEditor
        input-label="输入内容"
        output-label="结果"
        input-placeholder="请输入待编码或待解码的文本"
        :input-model-value="inputValue"
        :output-value="outputValue"
        @update:input-model-value="inputValue = $event"
      >
        <template #toolbar>
          <el-button type="primary" @click="encodeText">文本编码</el-button>
          <el-button @click="decodeText">文本解码</el-button>
          <el-upload :auto-upload="false" :show-file-list="false" :on-change="handleFileChange" accept="*">
            <el-button>文件转 Base64</el-button>
          </el-upload>
          <el-button @click="copyResult">复制结果</el-button>
          <el-button @click="clearAll">清空</el-button>
        </template>
      </DualPanelEditor>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import DualPanelEditor from '@/components/DualPanelEditor.vue'

const inputValue = ref('')
const outputValue = ref('')

const encodeText = () => {
  try {
    outputValue.value = btoa(unescape(encodeURIComponent(inputValue.value)))
  } catch (error) {
    ElMessage.error(`编码失败：${error.message}`)
  }
}

const decodeText = () => {
  try {
    outputValue.value = decodeURIComponent(escape(atob(inputValue.value)))
  } catch (error) {
    ElMessage.error(`解码失败：${error.message}`)
  }
}

const handleFileChange = async (file) => {
  if (!file.raw) return
  if (file.raw.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件建议不超过 10MB')
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    outputValue.value = String(reader.result || '')
    ElMessage.success('文件已转为 Base64')
  }
  reader.onerror = () => {
    ElMessage.error('文件读取失败')
  }
  reader.readAsDataURL(file.raw)
}

const copyResult = async () => {
  if (!outputValue.value) {
    ElMessage.warning('没有可复制的内容')
    return
  }

  await navigator.clipboard.writeText(outputValue.value)
  ElMessage.success('已复制结果')
}

const clearAll = () => {
  inputValue.value = ''
  outputValue.value = ''
}
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';
</style>
