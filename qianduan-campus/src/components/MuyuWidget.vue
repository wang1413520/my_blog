<template>
  <section class="muyu-widget">
    <div class="muyu-header">
      <span class="muyu-label">今日摸鱼指数：</span>
      <span class="muyu-count">{{ count }}</span>
    </div>

    <div class="muyu-stage">
      <button
        ref="muyuButtonRef"
        class="muyu-button"
        type="button"
        :class="{ hitting: isHitting }"
        @click="handleStrike"
      >
        <img class="muyu-image" :src="muyuImage" alt="木鱼" draggable="false" />
        <span
          v-for="floatItem in floatItems"
          :key="floatItem.id"
          class="merit-item"
          :style="{ left: `${floatItem.left}%` }"
        >
          +1
        </span>
      </button>
    </div>

  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import muyuImage from '@/assets/muyu/muyu.png'

const STORAGE_KEY = 'blog_muyu_count'

const count = ref(0)
const isHitting = ref(false)
const floatItems = ref([])
const muyuButtonRef = ref(null)

let floatId = 0
let hitTimer = null

const createFloatText = () => {
  const id = ++floatId
  const item = {
    id,
    left: 46 + Math.random() * 8
  }
  floatItems.value.push(item)

  window.setTimeout(() => {
    floatItems.value = floatItems.value.filter(floatItem => floatItem.id !== id)
  }, 1400)
}

const handleStrike = () => {
  count.value += 1
  localStorage.setItem(STORAGE_KEY, String(count.value))
  createFloatText()

  isHitting.value = false
  void muyuButtonRef.value?.offsetWidth
  isHitting.value = true

  if (hitTimer) {
    window.clearTimeout(hitTimer)
  }

  hitTimer = window.setTimeout(() => {
    isHitting.value = false
  }, 260)
}

onMounted(() => {
  count.value = Number(localStorage.getItem(STORAGE_KEY) || 0)
})
</script>

<style scoped>
.muyu-widget {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 18px;
  min-height: 240px;
  width: 100%;
}

.muyu-header {
  display: flex;
  align-items: baseline;
  gap: 14px;
  flex-wrap: wrap;
}

.muyu-label {
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 0.03em;
}

.muyu-count {
  min-width: 54px;
  padding: 6px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(214, 230, 245, 0.95);
  color: #6aa6de;
  font-size: 28px;
  font-weight: 800;
  text-align: center;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.muyu-stage {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
}

.muyu-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: min(100%, 300px);
  min-height: 220px;
  border: 0;
  background: transparent;
  cursor: pointer;
  padding: 0;
}

.muyu-image {
  width: 230px;
  max-width: 100%;
  height: auto;
  filter: brightness(0) saturate(100%) drop-shadow(0 14px 26px rgba(85, 116, 145, 0.24));
  user-select: none;
  pointer-events: none;
}

.muyu-button.hitting .muyu-image {
  animation: muyu-hit 0.26s ease;
}

.merit-item {
  position: absolute;
  top: 30%;
  transform: translateX(-50%);
  color: #6aa6de;
  font-size: 28px;
  font-weight: 800;
  text-shadow: 0 4px 14px rgba(255, 255, 255, 0.7);
  animation: merit-float 1.4s ease-out forwards;
  pointer-events: none;
}

@keyframes muyu-hit {
  0% {
    transform: scale(1);
  }

  35% {
    transform: scale(1.08, 0.88);
  }

  70% {
    transform: scale(0.96, 1.03);
  }

  100% {
    transform: scale(1);
  }
}

@keyframes merit-float {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(0) scale(0.4);
  }

  15% {
    opacity: 1;
    transform: translateX(-50%) translateY(-18px) scale(1.2);
  }

  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-130px) scale(0.9);
  }
}

@media (max-width: 900px) {
  .muyu-widget {
    min-height: 0;
    gap: 14px;
  }

  .muyu-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .muyu-label {
    font-size: 22px;
  }

  .muyu-count {
    font-size: 22px;
  }

  .muyu-button {
    width: min(100%, 260px);
    justify-content: center;
    min-height: 180px;
  }

  .muyu-image {
    width: min(100%, 200px);
  }

  .merit-item {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .muyu-label {
    font-size: 20px;
  }

  .muyu-count {
    font-size: 20px;
    min-width: 48px;
  }

  .muyu-button {
    width: min(100%, 220px);
    min-height: 150px;
  }

  .muyu-image {
    width: min(100%, 176px);
  }

  .merit-item {
    font-size: 18px;
  }
}
</style>
