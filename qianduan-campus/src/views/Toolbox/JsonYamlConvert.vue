<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">JSON ↔ YAML</h1>
          <p class="tool-desc">把 JSON 和 YAML 在本地浏览器里相互转换，适合处理配置文件。</p>
        </div>
      </div>

      <DualPanelEditor
        :input-label="mode === 'json-to-yaml' ? '输入 JSON' : '输入 YAML'"
        :output-label="mode === 'json-to-yaml' ? '输出 YAML' : '输出 JSON'"
        :input-model-value="inputValue"
        :output-value="outputValue"
        @update:input-model-value="inputValue = $event"
      >
        <template #toolbar>
          <el-radio-group v-model="mode">
            <el-radio-button label="json-to-yaml">JSON → YAML</el-radio-button>
            <el-radio-button label="yaml-to-json">YAML → JSON</el-radio-button>
          </el-radio-group>
          <el-button type="primary" @click="convertContent">开始转换</el-button>
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
import * as yaml from 'js-yaml'
import DualPanelEditor from '@/components/DualPanelEditor.vue'

const mode = ref('json-to-yaml')
const inputValue = ref('')
const outputValue = ref('')

const convertContent = () => {
  try {
    if (mode.value === 'json-to-yaml') {
      outputValue.value = yaml.dump(JSON.parse(inputValue.value), { indent: 2 })
    } else {
      outputValue.value = JSON.stringify(yaml.load(inputValue.value), null, 2)
    }
  } catch (error) {
    ElMessage.error(`转换失败：${error.message}`)
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
}
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';
</style>
