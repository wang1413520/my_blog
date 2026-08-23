<template>
  <el-dialog
    v-model="visible"
    class="notice-dialog"
    width="640px"
    align-center
    :close-on-click-modal="true"
    :show-close="true"
  >
    <template #header>
      <div class="notice-dialog-header">
        <div class="notice-header-icon">
          <el-icon :size="22"><Bell /></el-icon>
        </div>
        <div class="notice-header-copy">
          <span class="notice-header-title">公告板</span>
          <span class="notice-header-sub">Notice Board</span>
        </div>
      </div>
    </template>

    <div v-loading="loading" class="notice-dialog-body">
      <template v-if="notice">
        <h3 class="notice-dialog-title">{{ notice.title }}</h3>
        <div class="notice-dialog-content">{{ notice.content }}</div>
        <div class="notice-dialog-time">发布于 {{ notice.createTime }}</div>
      </template>
      <el-empty v-else-if="!loading" description="暂无公告" />
    </div>

    <template #footer>
      <div class="notice-dialog-footer">
        <el-button class="notice-footer-btn" @click="closeDialog">关闭</el-button>
        <el-button class="notice-footer-btn notice-footer-primary" type="primary" @click="closeToday">
          今日关闭
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { Bell } from '@element-plus/icons-vue'
import { useNotice } from '@/composables/useNotice'

const { visible, loading, notice, closeDialog, closeToday } = useNotice()
</script>

<style scoped>
.notice-dialog :deep(.el-dialog) {
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(63, 116, 168, 0.24);
}

/* 头部横幅：去掉默认内边距，让渐变铺满整个头部 */
.notice-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.notice-dialog-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 26px 28px;
  background: linear-gradient(135deg, #5f9fe0 0%, #7fb2ec 58%, #9cc5f3 100%);
}

.notice-header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  flex-shrink: 0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.22);
  color: #ffffff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.32), 0 10px 20px rgba(36, 92, 148, 0.22);
}

.notice-header-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.notice-header-title {
  color: #ffffff;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.04em;
  line-height: 1.2;
}

.notice-header-sub {
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  margin-top: 4px;
}

/* 关闭按钮：改为白色悬浮在横幅上 */
.notice-dialog :deep(.el-dialog__headerbtn) {
  top: 22px;
  right: 24px;
}

.notice-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #ffffff;
  font-size: 20px;
}

.notice-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #eaf4ff;
}

.notice-dialog-body {
  padding: 4px 2px;
  max-height: 62vh;
  overflow-y: auto;
}

.notice-dialog-title {
  margin: 0 0 16px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2a36;
  line-height: 1.4;
  word-break: break-word;
}

.notice-dialog-content {
  font-size: 15px;
  line-height: 2;
  color: #4b5b6b;
  white-space: pre-wrap;
  word-break: break-word;
}

.notice-dialog-time {
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px dashed #e3ecf4;
  font-size: 12px;
  color: #9aabba;
}

.notice-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.notice-footer-btn {
  min-width: 108px;
  height: 40px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.notice-footer-primary {
  box-shadow: 0 10px 22px rgba(95, 159, 224, 0.28);
}

@media (max-width: 480px) {
  .notice-dialog :deep(.el-dialog) {
    width: calc(100vw - 24px) !important;
    margin-top: 6vh !important;
    border-radius: 18px;
  }

  .notice-dialog-header {
    padding: 20px 18px;
    gap: 12px;
  }

  .notice-header-icon {
    width: 44px;
    height: 44px;
    border-radius: 13px;
  }

  .notice-header-title {
    font-size: 19px;
  }

  .notice-dialog :deep(.el-dialog__headerbtn) {
    top: 18px;
    right: 18px;
  }

  .notice-dialog-body {
    padding: 2px 0;
  }

  .notice-dialog-title {
    font-size: 19px;
  }

  .notice-dialog-content {
    font-size: 14px;
  }

  .notice-dialog-footer {
    gap: 10px;
  }

  .notice-footer-btn {
    flex: 1;
    min-width: 0;
  }
}
</style>
