<template>
  <div class="flip-clock" aria-label="当前时间">
    <span class="clock-label">此刻时间</span>
    <div class="clock-group">
      <div
        v-for="(digit, index) in digits"
        :key="`${index}-${digit.value}-${flipKey}`"
        class="flip-digit"
        :class="{ flipping: digit.flipping, separator: digit.value === ':' }"
      >
        <template v-if="digit.value === ':'">
          <span class="separator-dot"></span>
          <span class="separator-dot"></span>
        </template>
        <template v-else>
          <span class="digit-card digit-back">{{ digit.previous }}</span>
          <span class="digit-card digit-front">{{ digit.value }}</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const currentTime = ref(new Date())
const previousText = ref(formatTime(currentTime.value))
const flipKey = ref(0)
let timerId = 0

function formatTime(date) {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

const digits = computed(() => {
  const current = formatTime(currentTime.value)
  const previous = previousText.value

  return current.split('').map((value, index) => ({
    value,
    previous: previous[index] ?? value,
    flipping: value !== previous[index]
  }))
})

function syncTime() {
  const next = new Date()
  const nextText = formatTime(next)
  const currentText = formatTime(currentTime.value)

  if (nextText !== currentText) {
    previousText.value = currentText
    currentTime.value = next
    flipKey.value += 1
    return
  }

  currentTime.value = next
}

onMounted(() => {
  syncTime()
  timerId = window.setInterval(syncTime, 1000)
})

onBeforeUnmount(() => {
  window.clearInterval(timerId)
})
</script>

<style scoped>
.flip-clock {
  display: flex;
  align-items: center;
  gap: 18px;
}

.clock-label {
  font-size: 14px;
  font-weight: 700;
  color: #516274;
  letter-spacing: 0.12em;
}

.clock-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.flip-digit {
  position: relative;
  width: 48px;
  height: 64px;
  perspective: 400px;
}

.flip-digit.separator {
  width: 12px;
  height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 9px;
}

.separator-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #82afd8;
  box-shadow: 0 0 10px rgba(130, 175, 216, 0.24);
}

.digit-card {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 1px solid rgba(205, 227, 245, 0.92);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(233, 244, 255, 0.92));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 10px 22px rgba(177, 205, 228, 0.18);
  color: #1f2a36;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0.02em;
  font-variant-numeric: tabular-nums;
}

.digit-front {
  transform-origin: center top;
}

.digit-back {
  opacity: 0;
  transform-origin: center bottom;
}

.flip-digit.flipping .digit-front {
  animation: flip-front 0.72s ease;
}

.flip-digit.flipping .digit-back {
  animation: flip-back 0.72s ease;
}

@keyframes flip-front {
  0% {
    transform: rotateX(0deg);
  }

  100% {
    transform: rotateX(-360deg);
  }
}

@keyframes flip-back {
  0% {
    opacity: 0.65;
    transform: rotateX(360deg);
  }

  100% {
    opacity: 0;
    transform: rotateX(0deg);
  }
}

@media (max-width: 900px) {
  .flip-clock {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .clock-label {
    font-size: 12px;
  }

  .clock-group {
    gap: 8px;
  }

  .flip-digit {
    width: 38px;
    height: 52px;
  }

  .flip-digit.separator {
    width: 10px;
    height: 52px;
    gap: 7px;
  }

  .digit-card {
    font-size: 28px;
    border-radius: 10px;
  }
}

@media (max-width: 480px) {
  .clock-label {
    display: none;
  }

  .clock-group {
    gap: 6px;
  }

  .flip-digit {
    width: 32px;
    height: 44px;
  }

  .flip-digit.separator {
    width: 8px;
    height: 44px;
    gap: 6px;
  }

  .separator-dot {
    width: 5px;
    height: 5px;
  }

  .digit-card {
    font-size: 23px;
    border-radius: 8px;
  }
}
</style>
