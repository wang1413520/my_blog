<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  /* 主色调 */
  --color-primary: #5f9fe0;
  --color-primary-hover: #4f93d7;
  --color-primary-active: #427fbe;
  --color-primary-light: rgba(95, 159, 224, 0.16);

  /* 文字色 */
  --color-title: #1f2a36;
  --color-body: #5c6d7d;
  --color-muted: #7a8ea3;
  --color-placeholder: #999;

  /* 背景色 */
  --bg-page: linear-gradient(180deg, #f8fbff 0%, #f1f8ff 28%, #e9f3ff 100%);
  --bg-card: linear-gradient(180deg, #ffffff 0%, #f6faff 100%);
  --bg-card-hover: linear-gradient(180deg, #eaf4ff 0%, #d9eafa 100%);
  --bg-sidebar: linear-gradient(180deg, rgba(243, 249, 255, 0.98) 0%, rgba(233, 244, 255, 0.96) 100%);

  /* 边框色 */
  --border-color: rgba(208, 228, 245, 0.85);
  --border-color-hover: rgba(170, 208, 238, 0.96);

  /* 阴影 */
  --shadow-card: 0 14px 28px rgba(176, 206, 230, 0.12);
  --shadow-card-hover: 0 18px 34px rgba(155, 194, 224, 0.18);
  --shadow-sm: 0 8px 24px rgba(0, 0, 0, 0.08);

  /* 圆角层级 */
  --radius-sm: 12px;
  --radius-md: 18px;
  --radius-lg: 22px;
  --radius-xl: 24px;
  --radius-2xl: 28px;

  /* 间距 */
  --gap-xs: 8px;
  --gap-sm: 12px;
  --gap-md: 16px;
  --gap-lg: 20px;
  --gap-xl: 24px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

#app {
  min-height: 100vh;
}

/* ===== 移动端交互体验优化 ===== */

/* 触屏设备：点击卡片/菜单时不再位移跳动（保留颜色、阴影作为点击反馈），
   并去掉点击时的灰色高亮 */
@media (hover: none) {
  .post-item:hover,
  .resource-row:hover,
  .featured-resource-row:hover,
  .post-card:hover,
  .link-card:hover,
  .link-url:hover,
  .memo-item:hover,
  .tool-card:hover,
  .folder-entry.clickable:hover,
  .el-menu-item:hover,
  .el-sub-menu__title:hover {
    transform: none !important;
  }

  * {
    -webkit-tap-highlight-color: transparent;
  }

  /* 按钮长按不弹系统菜单、不误选中文本；链接保留长按菜单以便复制/分享 */
  button,
  .el-button {
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    user-select: none;
  }
}

/* 小屏：Element Plus 普通按钮增大点击区（link/text/圆形按钮除外） */
@media (max-width: 768px) {
  .el-button:not(.is-link):not(.is-text):not(.is-circle) {
    min-height: 40px;
  }
}
</style>
