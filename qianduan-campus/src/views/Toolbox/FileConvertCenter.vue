<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">文件转换中心</h1>
          <p class="tool-desc">
            走后端转换接口，适合处理浏览器本地不太好完成的文档和图片格式转换，比如 Markdown、PDF、Word、PNG、JPG。
          </p>
        </div>
      </div>

      <div class="tool-panels">
        <section class="tool-card">
          <div class="field-row">
            <label class="field-label">选择文件</label>
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="true"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
            >
              <el-button type="primary">选择待转换文件</el-button>
              <template #tip>
                <div class="upload-tip">
                  文件会发送到后端进行转换，适合处理 PDF、Word、Markdown 和图片这类需要服务端能力的格式。
                </div>
              </template>
            </el-upload>
          </div>

          <div v-if="selectedSourceType" class="source-summary">
            <span class="summary-tag">源格式 {{ formatTypeLabel(selectedSourceType) }}</span>
            <span v-if="currentMaxSizeText" class="summary-tag">大小上限 {{ currentMaxSizeText }}</span>
          </div>

          <div class="field-row">
            <label class="field-label">目标格式</label>
            <el-select
              v-model="targetType"
              placeholder="请先上传文件"
              :disabled="!selectedFile || !targetOptions.length"
            >
              <el-option
                v-for="item in targetOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <p v-if="selectedFile && !targetOptions.length" class="field-tip warning-text">
              当前文件格式暂不支持转换，请更换文件或刷新支持格式。
            </p>
            <p v-else-if="selectedSourceType && targetOptions.length" class="field-tip">
              当前可转换为：{{ targetOptions.map(item => item.shortLabel).join(' / ') }}
            </p>
          </div>

          <div v-if="showImageOptions" class="tool-panels convert-options">
            <div class="field-row">
              <label class="field-label">图片质量</label>
              <el-input-number v-model="quality" :min="1" :max="100" />
            </div>
            <div class="field-row">
              <label class="field-label">宽度</label>
              <el-input-number v-model="width" :min="1" :max="6000" />
            </div>
            <div class="field-row">
              <label class="field-label">高度</label>
              <el-input-number v-model="height" :min="1" :max="6000" />
            </div>
          </div>

          <p v-if="showImageOptions" class="field-tip">
            图片质量、宽度和高度只会在图片转换时生效，文档类转换不会用到这些参数。
          </p>

          <div class="button-row">
            <el-button type="primary" :loading="converting" @click="submitConvert">开始转换</el-button>
            <el-button :disabled="converting" @click="resetForm">重置</el-button>
            <el-button :disabled="converting" @click="refreshTypes">刷新支持格式</el-button>
          </div>

          <div v-if="converting" class="upload-progress-card">
            <img class="upload-progress-mascot" :src="uploadMascot" alt="upload mascot" />
            <div class="upload-progress-copy">
              <p class="upload-progress-title">{{ progressTitle }}</p>
              <p class="upload-progress-text">{{ progressText }}</p>
              <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="10" />
            </div>
          </div>
        </section>

        <section class="tool-card">
          <p class="result-label">支持的转换方向</p>
          <div v-if="displayTypeGroups.length" class="type-groups">
            <div v-for="group in displayTypeGroups" :key="group.category" class="type-group">
              <h3 class="type-group-title">{{ group.category }}</h3>
              <div class="type-chips">
                <span
                  v-for="type in group.types"
                  :key="`${group.category}-${type.from.join('-')}-${type.to.join('-')}`"
                  class="type-chip"
                >
                  {{ type.from.map(formatTypeLabel).join(' / ') }}
                  →
                  {{ type.to.map(formatTypeLabel).join(' / ') }}
                </span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无可用转换类型" />

          <div v-if="resultUrl" class="result-box convert-result">
            <p class="result-label">转换结果</p>
            <p class="result-value">{{ resultFileName }}</p>
            <p class="field-tip">
              文档和图片结果默认走后端下载代理；如果后端返回的是 OSS 公网地址，也会直接打开。
            </p>
            <div class="button-row">
              <el-button type="primary" @click="openResult">打开结果</el-button>
              <el-button @click="copyResultUrl">复制链接</el-button>
            </div>
          </div>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { toolboxAPI } from '@/api/toolbox'
import uploadMascot from '@/assets/lxh_011_hd.gif'

const TYPE_LABELS = {
  pdf: 'PDF 文档',
  doc: 'DOC 文档',
  docx: 'DOCX 文档',
  md: 'Markdown',
  html: 'HTML 页面',
  txt: 'TXT 文本',
  png: 'PNG 图片',
  jpg: 'JPG 图片',
  jpeg: 'JPEG 图片',
  webp: 'WebP 图片',
  bmp: 'BMP 图片',
  gif: 'GIF 图片',
  zip: 'ZIP 压缩包'
}

const IMAGE_TYPES = ['png', 'jpg', 'jpeg', 'webp', 'bmp', 'gif']

const uploadRef = ref(null)
const selectedFile = ref(null)
const selectedSourceType = ref('')
const targetType = ref('')
const quality = ref(90)
const width = ref(null)
const height = ref(null)
const converting = ref(false)
const uploadProgress = ref(0)
const uploadStage = ref('idle')
const typeGroups = ref([])
const resultUrl = ref('')
const resultFileName = ref('')

const flatSupportedTypes = computed(() => {
  return typeGroups.value.flatMap(group =>
    (group.types || []).map(type => ({
      ...type,
      category: group.category
    }))
  )
})

const displayTypeGroups = computed(() => {
  return typeGroups.value
    .map(group => ({
      ...group,
      types: (group.types || [])
        .map(type => {
          const from = (type.from || []).map(item => String(item).toLowerCase())
          const to = (type.to || []).map(item => String(item).toLowerCase())

          return {
            ...type,
            from,
            to: to.filter(target => !from.includes(target))
          }
        })
        .filter(type => type.from.length && type.to.length)
    }))
    .filter(group => group.types.length)
})

const matchedTypeRules = computed(() => {
  if (!selectedSourceType.value) {
    return []
  }

  return flatSupportedTypes.value.filter(type =>
    (type.from || []).map(item => String(item).toLowerCase()).includes(selectedSourceType.value)
  )
})

const targetOptions = computed(() => {
  const optionMap = new Map()

  matchedTypeRules.value.forEach(rule => {
    const sourceTypes = (rule.from || []).map(item => String(item).toLowerCase())

    ;(rule.to || []).forEach(target => {
      const value = String(target).toLowerCase()
      if (!sourceTypes.includes(value) && !optionMap.has(value)) {
        optionMap.set(value, {
          value,
          shortLabel: value.toUpperCase(),
          label: formatTypeLabel(value)
        })
      }
    })
  })

  return Array.from(optionMap.values())
})

const currentMaxSizeText = computed(() => {
  const sizes = matchedTypeRules.value
    .map(item => Number(item.maxSize))
    .filter(size => Number.isFinite(size) && size > 0)

  if (!sizes.length) {
    return ''
  }

  return formatFileSize(Math.max(...sizes))
})

const resolvedResultUrl = computed(() => resolveResultUrl(resultUrl.value))
const showImageOptions = computed(() => IMAGE_TYPES.includes(targetType.value))
const progressPercent = computed(() => {
  if (uploadStage.value === 'converting') {
    return 100
  }

  return Math.min(Math.max(uploadProgress.value, 0), 100)
})

const progressTitle = computed(() => {
  return uploadStage.value === 'converting' ? '正在转换文件' : '正在上传文件'
})

const progressText = computed(() => {
  if (uploadStage.value === 'converting') {
    return '文件已经上传完成，小黑正在陪你等待转换结果。'
  }

  return `当前上传进度 ${progressPercent.value}%`
})

const getFileExtension = (fileName = '') => {
  const parts = String(fileName).toLowerCase().split('.')
  return parts.length > 1 ? parts.pop() : ''
}

function formatTypeLabel(type) {
  const normalized = String(type || '').toLowerCase()
  return TYPE_LABELS[normalized] || normalized.toUpperCase()
}

function formatFileSize(size) {
  if (size >= 1024 * 1024) {
    return `${(size / 1024 / 1024).toFixed(size >= 10 * 1024 * 1024 ? 0 : 1)}MB`
  }

  if (size >= 1024) {
    return `${Math.round(size / 1024)}KB`
  }

  return `${size}B`
}

function getOssPublicBase() {
  if (import.meta.env.VITE_OSS_PUBLIC_BASE) {
    return import.meta.env.VITE_OSS_PUBLIC_BASE.replace(/\/$/, '')
  }

  return ''
}

function resolveResultUrl(url) {
  const raw = String(url || '').trim()
  if (!raw) {
    return ''
  }

  if (/^(https?:|blob:|data:)/i.test(raw)) {
    return raw
  }

  if (raw.startsWith('/api')) {
    return `${window.location.origin}${raw}`
  }

  if (raw.startsWith('api/')) {
    return `${window.location.origin}/${raw}`
  }

  const ossBase = getOssPublicBase()
  if (ossBase) {
    return `${ossBase}/${raw.replace(/^\/+/, '')}`
  }

  return `${window.location.origin}/${raw.replace(/^\/+/, '')}`
}

const resetProgressState = () => {
  uploadProgress.value = 0
  uploadStage.value = 'idle'
}

const syncTargetType = () => {
  const availableTargets = targetOptions.value.map(item => item.value)

  if (!availableTargets.length) {
    targetType.value = ''
    return
  }

  if (!availableTargets.includes(targetType.value)) {
    targetType.value = availableTargets[0]
  }
}

const handleFileChange = file => {
  selectedFile.value = file.raw
  selectedSourceType.value = getFileExtension(file.name || file.raw?.name)
  resultUrl.value = ''
  resultFileName.value = ''
  resetProgressState()
  syncTargetType()

  if (!matchedTypeRules.value.length && selectedSourceType.value) {
    ElMessage.warning(`暂不支持 ${selectedSourceType.value.toUpperCase()} 格式的文件转换`)
  }
}

const handleFileRemove = () => {
  selectedFile.value = null
  selectedSourceType.value = ''
  targetType.value = ''
  resetProgressState()
}

const refreshTypes = async () => {
  const data = await toolboxAPI.getSupportedTypes()
  typeGroups.value = data.groups || []
  syncTargetType()
}

const submitConvert = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }

  if (!selectedSourceType.value || !matchedTypeRules.value.length) {
    ElMessage.warning('当前文件格式暂不支持转换')
    return
  }

  if (!targetType.value) {
    ElMessage.warning('请选择目标格式')
    return
  }

  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('targetType', targetType.value)

  if (showImageOptions.value) {
    formData.append('quality', String(quality.value))

    if (width.value) {
      formData.append('width', String(width.value))
    }

    if (height.value) {
      formData.append('height', String(height.value))
    }
  }

  converting.value = true
  uploadProgress.value = 0
  uploadStage.value = 'uploading'

  try {
    const data = await toolboxAPI.convertFile(formData, {
      onUploadProgress: event => {
        if (!event.total) {
          return
        }

        const percent = Math.round((event.loaded / event.total) * 100)
        uploadProgress.value = Math.min(percent, 100)

        if (percent >= 100) {
          uploadStage.value = 'converting'
        }
      }
    })

    resultUrl.value = data.resultUrl || ''
    resultFileName.value = data.sourceName
      ? `${data.sourceName} → ${formatTypeLabel(data.targetType || targetType.value)}`
      : '转换结果'
    resetProgressState()
    ElMessage.success('转换完成')
  } catch (error) {
    resultUrl.value = ''
    resultFileName.value = ''
    resetProgressState()
  } finally {
    converting.value = false
  }
}

const openResult = () => {
  if (resolvedResultUrl.value) {
    window.open(resolvedResultUrl.value, '_blank')
  }
}

const copyResultUrl = async () => {
  if (!resolvedResultUrl.value) {
    ElMessage.warning('暂无结果链接')
    return
  }

  await navigator.clipboard.writeText(resolvedResultUrl.value)
  ElMessage.success('结果链接已复制')
}

const resetForm = () => {
  selectedFile.value = null
  selectedSourceType.value = ''
  targetType.value = ''
  quality.value = 90
  width.value = null
  height.value = null
  resultUrl.value = ''
  resultFileName.value = ''
  resetProgressState()
  uploadRef.value?.clearFiles()
}

onMounted(() => {
  refreshTypes()
})
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';

.source-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: -4px 0 6px;
}

.summary-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(232, 244, 255, 0.92);
  color: #51789e;
  font-size: 12px;
  font-weight: 700;
}

.field-tip {
  margin: 10px 0 0;
  color: #6f8499;
  font-size: 12px;
  line-height: 1.6;
}

.warning-text {
  color: #c07c5f;
}

.convert-options {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.type-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.type-group-title {
  margin: 0 0 10px;
  color: #1f2a36;
  font-size: 16px;
  font-weight: 700;
}

.type-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.type-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(232, 244, 255, 0.92);
  color: #5f88b0;
  font-size: 13px;
  font-weight: 600;
}

.convert-result {
  margin-top: 22px;
}

.upload-tip {
  margin-top: 10px;
  color: #6c8095;
  font-size: 12px;
}

.upload-progress-card {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  margin-top: 18px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(246, 251, 255, 0.98), rgba(234, 245, 255, 0.94));
  box-shadow: inset 0 0 0 1px rgba(204, 227, 245, 0.88);
}

.upload-progress-mascot {
  display: block;
  width: 96px;
  height: 96px;
  object-fit: contain;
  filter: drop-shadow(0 12px 20px rgba(126, 173, 214, 0.18));
}

.upload-progress-copy {
  min-width: 0;
}

.upload-progress-title {
  margin: 0 0 6px;
  color: #1f2a36;
  font-size: 18px;
  font-weight: 800;
}

.upload-progress-text {
  margin: 0 0 14px;
  color: #63809d;
  font-size: 13px;
  line-height: 1.6;
}

.upload-progress-card :deep(.el-progress-bar__outer) {
  background: rgba(214, 232, 247, 0.75);
}

.upload-progress-card :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #8cc5f2 0%, #63a7df 100%);
}

@media (max-width: 900px) {
  .convert-options {
    grid-template-columns: 1fr;
  }

  .upload-progress-card {
    grid-template-columns: 1fr;
    justify-items: center;
    text-align: center;
  }

  .upload-progress-copy {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .convert-options :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
