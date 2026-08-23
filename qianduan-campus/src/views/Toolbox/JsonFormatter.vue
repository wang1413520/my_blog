<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">JSON 格式化</h1>
          <p class="tool-desc">支持格式化、压缩和校验，适合快速查看或整理 JSON 数据。</p>
        </div>
      </div>

      <DualPanelEditor
        input-label="输入 JSON"
        output-label="结果"
        input-placeholder="请输入 JSON 内容"
        :input-model-value="inputValue"
        :output-value="outputValue"
        @update:input-model-value="inputValue = $event"
      >
        <template #toolbar>
          <el-button type="primary" @click="formatJson">格式化</el-button>
          <el-button @click="minifyJson">压缩</el-button>
          <el-button @click="validateJson">校验</el-button>
          <el-button @click="copyResult">复制结果</el-button>
          <el-button @click="clearAll">清空</el-button>
        </template>
      </DualPanelEditor>

      <el-alert
        v-if="message"
        class="tool-alert"
        :title="message"
        :type="messageType"
        show-icon
        :closable="false"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import DualPanelEditor from '@/components/DualPanelEditor.vue'

const inputValue = ref('')
const outputValue = ref('')
const message = ref('')
const messageType = ref('success')

const parseJson = () => JSON.parse(inputValue.value)

const formatJson = () => {
  try {
    outputValue.value = JSON.stringify(parseJson(), null, 2)
    message.value = 'JSON 格式化完成'
    messageType.value = 'success'
  } catch (error) {
    message.value = `JSON 解析失败：${error.message}`
    messageType.value = 'error'
  }
}

const minifyJson = () => {
  try {
    outputValue.value = JSON.stringify(parseJson())
    message.value = 'JSON 压缩完成'
    messageType.value = 'success'
  } catch (error) {
    message.value = `JSON 解析失败：${error.message}`
    messageType.value = 'error'
  }
}

const validateJson = () => {
  try {
    parseJson()
    message.value = 'JSON 语法校验通过'
    messageType.value = 'success'
  } catch (error) {
    message.value = `JSON 语法错误：${error.message}`
    messageType.value = 'error'
  }
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
  message.value = ''
}
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';
</style>
