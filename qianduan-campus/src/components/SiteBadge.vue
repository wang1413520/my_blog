<template>
  <span class="site-badge" :class="`site-badge-${theme}`">
    <span class="badge-text">{{ text }}</span>
  </span>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '吉祥物'
  },
  theme: {
    type: String,
    default: 'gold'
  }
})
</script>

<style scoped>
.site-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  line-height: 1;
  white-space: nowrap;
  vertical-align: middle;
  user-select: none;
  cursor: default;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.site-badge:hover {
  transform: translateY(-1px);
}

/* 金色主题（默认） */
.site-badge-gold {
  color: #8b5a00;
  background: linear-gradient(135deg, #fff1c1 0%, #ffe08a 50%, #f5d060 100%);
  border: 1px solid rgba(200, 150, 30, 0.45);
  box-shadow:
    0 2px 8px rgba(200, 150, 30, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.site-badge-gold:hover {
  box-shadow:
    0 4px 14px rgba(200, 150, 30, 0.32),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

/* 红色主题（备用） */
.site-badge-red {
  color: #8b1a1a;
  background: linear-gradient(135deg, #ffe0e0 0%, #ffb8b8 50%, #f59090 100%);
  border: 1px solid rgba(200, 60, 60, 0.45);
  box-shadow:
    0 2px 8px rgba(200, 60, 60, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

/* 蓝色主题（备用） */
.site-badge-blue {
  color: #1a4a8b;
  background: linear-gradient(135deg, #d8ecff 0%, #b0d4ff 50%, #80b8f0 100%);
  border: 1px solid rgba(60, 120, 200, 0.45);
  box-shadow:
    0 2px 8px rgba(60, 120, 200, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}
</style>
