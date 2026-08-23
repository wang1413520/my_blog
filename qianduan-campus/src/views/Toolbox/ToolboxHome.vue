<template>
  <div class="toolbox-page">
    <div class="tool-grid">
      <ToolCard
        v-for="tool in tools"
        :key="tool.to"
        :icon="tool.icon"
        :name="tool.name"
        :description="tool.description"
        :to="tool.to"
      />

      <el-card class="coming-soon-card" shadow="hover">
        <div class="coming-soon-content">
          <div class="coming-soon-icon-wrap">
            <el-icon class="coming-soon-icon" :size="28">
              <MoreFilled />
            </el-icon>
          </div>
          <h3 class="coming-soon-title">敬请期待</h3>
          <p class="coming-soon-desc">
            这里会继续补充新的顺手小工具，后面再慢慢把常用能力加进来。
          </p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { FolderOpened, MoreFilled } from '@element-plus/icons-vue'
import ToolCard from '@/components/ToolCard.vue'

const tools = [
  {
    name: '文件转换中心',
    description: '适合处理 PDF、Word、Markdown、图片等更适合走后端服务完成的转换任务。',
    to: '/toolbox/file-convert',
    icon: FolderOpened
  }
]
</script>

<style scoped>
.toolbox-page {
  display: block;
}

.tool-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 520px), 1fr));
  gap: 20px;
  align-content: start;
}

.coming-soon-card {
  border-radius: 22px;
  border: 1px solid rgba(208, 228, 245, 0.88);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(242, 249, 255, 0.92));
  box-shadow: 0 14px 28px rgba(176, 206, 230, 0.12);
}

.coming-soon-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 180px;
}

.coming-soon-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(124, 183, 232, 0.18), rgba(124, 183, 232, 0.08));
  box-shadow: inset 0 0 0 1px rgba(181, 217, 242, 0.9);
}

.coming-soon-icon {
  color: #5e93c6;
}

.coming-soon-title {
  margin: 0;
  color: #1f2a36;
  font-size: 20px;
  font-weight: 800;
}

.coming-soon-desc {
  margin: 0;
  color: #647487;
  font-size: 14px;
  line-height: 1.75;
}
</style>
