<template>
  <el-card class="tool-card" shadow="hover" @click="$router.push(to)">
    <div class="tool-card-content">
      <div class="tool-icon-wrap">
        <el-icon class="tool-icon" :size="28">
          <component :is="icon" />
        </el-icon>
      </div>
      <h3 class="tool-name">{{ name }}</h3>
      <p class="tool-desc">{{ description }}</p>
    </div>
  </el-card>
</template>

<script setup>
defineProps({
  icon: {
    type: [Object, Function],
    required: true
  },
  name: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  to: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.tool-card {
  border-radius: 22px;
  border: 1px solid rgba(208, 228, 245, 0.88);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(242, 249, 255, 0.92));
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  box-shadow: 0 14px 28px rgba(176, 206, 230, 0.12);
}

.tool-card:hover {
  transform: translateY(-5px);
  border-color: rgba(170, 208, 238, 0.96);
  box-shadow: 0 18px 34px rgba(155, 194, 224, 0.18);
}

.tool-card-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 180px;
}

.tool-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(124, 183, 232, 0.18), rgba(124, 183, 232, 0.08));
  box-shadow: inset 0 0 0 1px rgba(181, 217, 242, 0.9);
}

.tool-icon {
  color: #5e93c6;
}

.tool-name {
  margin: 0;
  color: #1f2a36;
  font-size: 20px;
  font-weight: 800;
}

.tool-desc {
  margin: 0;
  color: #647487;
  font-size: 14px;
  line-height: 1.75;
}
</style>
