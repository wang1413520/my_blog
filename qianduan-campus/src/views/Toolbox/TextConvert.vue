<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">Markdown ↔ HTML</h1>
          <p class="tool-desc">支持 Markdown 和 HTML 双向转换，写文档、整理笔记都会更顺手。</p>
        </div>
      </div>

      <DualPanelEditor
        :input-label="mode === 'md-to-html' ? '输入 Markdown' : '输入 HTML'"
        :output-label="mode === 'md-to-html' ? '输出 HTML / 预览' : '输出 Markdown'"
        :input-model-value="inputValue"
        :output-value="outputValue"
        @update:input-model-value="inputValue = $event"
      >
        <template #toolbar>
          <el-radio-group v-model="mode">
            <el-radio-button label="md-to-html">Markdown → HTML</el-radio-button>
            <el-radio-button label="html-to-md">HTML → Markdown</el-radio-button>
          </el-radio-group>
          <el-button type="primary" @click="convertContent">开始转换</el-button>
          <el-button @click="copyResult">复制结果</el-button>
          <el-button @click="clearAll">清空</el-button>
        </template>
        <template #output>
          <div v-if="mode === 'md-to-html'" class="preview-panel">
            <div class="preview-source">
              <el-input :model-value="outputValue" type="textarea" :rows="7" readonly resize="none" />
            </div>
            <div class="preview-render" v-html="previewHtml"></div>
          </div>
          <el-input v-else :model-value="outputValue" type="textarea" :rows="14" readonly resize="none" />
        </template>
      </DualPanelEditor>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import TurndownService from 'turndown'
import DualPanelEditor from '@/components/DualPanelEditor.vue'

marked.setOptions({
  breaks: true,
  gfm: true
})

const turndownService = new TurndownService()
const mode = ref('md-to-html')
const inputValue = ref('')
const outputValue = ref('')

const previewHtml = computed(() => (mode.value === 'md-to-html' ? outputValue.value : ''))

const convertContent = () => {
  try {
    if (mode.value === 'md-to-html') {
      outputValue.value = marked.parse(inputValue.value)
    } else {
      outputValue.value = turndownService.turndown(inputValue.value)
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

.preview-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 18px;
}

.preview-panel :deep(.el-textarea__inner) {
  min-height: 160px !important;
}

.preview-render {
  border-radius: 18px;
  border: 1px solid rgba(214, 232, 246, 0.88);
  padding: 18px;
  color: #2d3a48;
  line-height: 1.8;
  background: rgba(255, 255, 255, 0.68);
}

.preview-render :deep(pre) {
  overflow: auto;
}
</style>
