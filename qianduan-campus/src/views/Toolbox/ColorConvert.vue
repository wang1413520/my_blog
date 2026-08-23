<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">颜色格式转换</h1>
          <p class="tool-desc">支持 HEX、RGB、HSL 之间互转，并实时显示颜色预览。</p>
        </div>
      </div>

      <div class="tool-panels">
        <section class="tool-card">
          <div class="field-row">
            <label class="field-label">HEX</label>
            <el-input v-model="hexValue" placeholder="#7EB8EB" />
          </div>
          <div class="field-row">
            <label class="field-label">RGB</label>
            <el-input v-model="rgbValue" placeholder="rgb(126, 184, 235)" />
          </div>
          <div class="field-row">
            <label class="field-label">HSL</label>
            <el-input v-model="hslValue" placeholder="hsl(208, 73%, 71%)" />
          </div>
          <div class="button-row">
            <el-button type="primary" @click="convertFromHex">HEX 转换</el-button>
            <el-button @click="convertFromRgb">RGB 转换</el-button>
            <el-button @click="convertFromHsl">HSL 转换</el-button>
            <el-button @click="copyAll">复制结果</el-button>
          </div>
        </section>

        <section class="tool-card preview-card">
          <p class="result-label">颜色预览</p>
          <div class="color-preview" :style="{ background: previewColor }"></div>
          <p class="result-value">{{ previewColor }}</p>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const hexValue = ref('#7EB8EB')
const rgbValue = ref('rgb(126, 184, 235)')
const hslValue = ref('hsl(208, 73%, 71%)')

const previewColor = computed(() => hexValue.value || '#7EB8EB')

const componentToHex = (value) => Number(value).toString(16).padStart(2, '0')

const rgbToHex = (r, g, b) => `#${componentToHex(r)}${componentToHex(g)}${componentToHex(b)}`.toUpperCase()

const hexToRgbTuple = (hex) => {
  const normalized = hex.replace('#', '').trim()
  if (![3, 6].includes(normalized.length)) {
    throw new Error('HEX 格式不正确')
  }

  const full = normalized.length === 3
    ? normalized.split('').map(char => char + char).join('')
    : normalized

  const value = Number.parseInt(full, 16)
  return [(value >> 16) & 255, (value >> 8) & 255, value & 255]
}

const rgbToHsl = (r, g, b) => {
  r /= 255
  g /= 255
  b /= 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  let h
  let s
  const l = (max + min) / 2
  const d = max - min

  if (d === 0) {
    h = s = 0
  } else {
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r:
        h = (g - b) / d + (g < b ? 6 : 0)
        break
      case g:
        h = (b - r) / d + 2
        break
      default:
        h = (r - g) / d + 4
    }
    h /= 6
  }

  return `hsl(${Math.round(h * 360)}, ${Math.round((s || 0) * 100)}%, ${Math.round(l * 100)}%)`
}

const hslToRgb = (h, s, l) => {
  s /= 100
  l /= 100

  const c = (1 - Math.abs(2 * l - 1)) * s
  const x = c * (1 - Math.abs((h / 60) % 2 - 1))
  const m = l - c / 2
  let r = 0
  let g = 0
  let b = 0

  if (h < 60) [r, g, b] = [c, x, 0]
  else if (h < 120) [r, g, b] = [x, c, 0]
  else if (h < 180) [r, g, b] = [0, c, x]
  else if (h < 240) [r, g, b] = [0, x, c]
  else if (h < 300) [r, g, b] = [x, 0, c]
  else [r, g, b] = [c, 0, x]

  return [
    Math.round((r + m) * 255),
    Math.round((g + m) * 255),
    Math.round((b + m) * 255)
  ]
}

const convertFromHex = () => {
  try {
    const [r, g, b] = hexToRgbTuple(hexValue.value)
    rgbValue.value = `rgb(${r}, ${g}, ${b})`
    hslValue.value = rgbToHsl(r, g, b)
  } catch (error) {
    ElMessage.error(error.message)
  }
}

const convertFromRgb = () => {
  const matched = rgbValue.value.match(/\d+/g)
  if (!matched || matched.length < 3) {
    ElMessage.error('RGB 格式不正确')
    return
  }

  const [r, g, b] = matched.map(Number)
  hexValue.value = rgbToHex(r, g, b)
  hslValue.value = rgbToHsl(r, g, b)
}

const convertFromHsl = () => {
  const matched = hslValue.value.match(/-?\d+(\.\d+)?/g)
  if (!matched || matched.length < 3) {
    ElMessage.error('HSL 格式不正确')
    return
  }

  const [h, s, l] = matched.map(Number)
  const [r, g, b] = hslToRgb(h, s, l)
  rgbValue.value = `rgb(${r}, ${g}, ${b})`
  hexValue.value = rgbToHex(r, g, b)
}

const copyAll = async () => {
  const text = `HEX: ${hexValue.value}\nRGB: ${rgbValue.value}\nHSL: ${hslValue.value}`
  await navigator.clipboard.writeText(text)
  ElMessage.success('已复制结果')
}
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';

.preview-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.color-preview {
  width: 100%;
  height: 180px;
  border-radius: 24px;
  border: 1px solid rgba(206, 227, 244, 0.9);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.96);
}
</style>
