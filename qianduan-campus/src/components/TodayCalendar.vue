<template>
  <div class="today-card">
    <div class="calendar-top">
      <span class="calendar-badge">TODAY</span>
      <span class="calendar-week">{{ weekDay }}</span>
    </div>
    <div class="calendar-mascot" aria-hidden="true">
      <img class="calendar-mascot-image" :src="calendarMascot" alt="" />
    </div>
    <div class="calendar-date">{{ day }}</div>
    <div class="calendar-month">{{ year }} / {{ month }}</div>
    <div class="calendar-divider"></div>
    <p class="calendar-copy">{{ copyText }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import calendarMascot from '@/assets/lxh_043_hd.gif'

const now = new Date()
const weekNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const copyPool = [
  '今天也适合来这里看看新鲜事',
  '把日常、资料和碎碎念都留在今天',
  '新的一天，新的校园小动静'
]

const year = now.getFullYear()
const month = String(now.getMonth() + 1).padStart(2, '0')
const day = String(now.getDate()).padStart(2, '0')
const weekDay = weekNames[now.getDay()]
const copyText = computed(() => copyPool[now.getDate() % copyPool.length])
</script>

<style scoped>
.today-card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 250px;
  padding: 24px 22px;
  border-radius: 26px;
  border: 1px solid rgba(208, 228, 245, 0.84);
  background:
    radial-gradient(circle at 22% 20%, rgba(255, 255, 255, 0.92) 0, rgba(255, 255, 255, 0) 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(233, 245, 255, 0.9));
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.95),
    0 18px 32px rgba(173, 202, 225, 0.16);
}

.calendar-mascot {
  position: absolute;
  top: 72px;
  right: 34px;
  width: 128px;
  height: 124px;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  transform: translateY(-8px);
}

.calendar-mascot-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  filter: drop-shadow(0 10px 18px rgba(139, 175, 204, 0.16));
}

.calendar-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.calendar-badge {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(126, 184, 235, 0.14);
  color: #76a7d6;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.calendar-week {
  color: #556476;
  font-size: 14px;
  font-weight: 600;
}

.calendar-date {
  font-size: 76px;
  line-height: 1;
  font-weight: 800;
  color: #1f2a36;
  letter-spacing: -0.04em;
}

.calendar-month {
  margin-top: 10px;
  color: #647689;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.calendar-divider {
  width: 100%;
  height: 1px;
  margin: 18px 0 16px;
  background: linear-gradient(90deg, rgba(175, 207, 232, 0), rgba(175, 207, 232, 0.95), rgba(175, 207, 232, 0));
}

.calendar-copy {
  margin: 0;
  color: #566577;
  font-size: 14px;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .today-card {
    min-height: 0;
    padding: 22px 18px;
  }

  .calendar-date {
    font-size: 64px;
  }

  .calendar-mascot {
    position: static;
    align-self: flex-end;
    width: 96px;
    height: 92px;
    margin: -8px 0 8px;
    opacity: 0.92;
    transform: none;
  }
}

@media (max-width: 480px) {
  .today-card {
    padding: 18px 16px;
    border-radius: 22px;
  }

  .calendar-top {
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 14px;
  }

  .calendar-date {
    font-size: 52px;
  }

  .calendar-month {
    font-size: 14px;
  }

  .calendar-divider {
    margin: 14px 0 12px;
  }

  .calendar-copy {
    font-size: 13px;
  }

  .calendar-mascot {
    display: none;
  }
}
</style>
