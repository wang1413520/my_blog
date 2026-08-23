<template>
  <div class="layout-container">
    <el-container class="layout-shell">
      <el-aside class="sidebar" :class="{ 'sidebar-expanded': sidebarExpanded }" width="280px">
        <button
          class="sidebar-toggle-btn"
          :aria-label="sidebarExpanded ? '收起侧边栏' : '展开侧边栏'"
          :title="sidebarExpanded ? '收起侧边栏' : '展开侧边栏'"
          @click="sidebarExpanded = !sidebarExpanded"
        >
          <span class="toggle-bar"></span>
          <span class="toggle-bar"></span>
          <span class="toggle-bar"></span>
        </button>
        <div class="sidebar-brand" @click="router.push('/home')">
          <el-avatar class="brand-avatar" :size="44" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'M' }}
          </el-avatar>
          <div class="brand-copy">
            <h1 class="logo">{{ brandTitle }}</h1>
          </div>
        </div>

        <div class="sidebar-nav">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="defaultOpenMenus"
            class="side-menu"
            :collapse-transition="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/home">
              <el-icon class="nav-icon nav-icon-house"><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/post/list">
              <el-icon class="nav-icon nav-icon-document"><Document /></el-icon>
              <span>文章</span>
            </el-menu-item>
            <el-sub-menu index="resource-menu">
              <template #title>
                <el-icon class="nav-icon nav-icon-folder"><FolderOpened /></el-icon>
                <span>资源</span>
              </template>
              <el-menu-item index="/resource/all">
                <el-icon class="nav-icon nav-icon-files"><Files /></el-icon>
                <span>全部资源</span>
              </el-menu-item>
              <el-menu-item index="/resource/featured">
                <el-icon class="nav-icon nav-icon-star"><StarFilled /></el-icon>
                <span>站长主推</span>
              </el-menu-item>
              <el-menu-item index="/resource/links">
                <el-icon class="nav-icon nav-icon-link"><Link /></el-icon>
                <span>链接分享</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/toolbox">
              <el-icon class="nav-icon nav-icon-tools"><Tools /></el-icon>
              <span>工具</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.isLogin()" index="/profile">
              <el-icon class="nav-icon nav-icon-user"><User /></el-icon>
              <span>我的</span>
            </el-menu-item>
          </el-menu>

          <div v-if="isAdmin" class="sidebar-admin-entry">
            <p class="sidebar-admin-label">MANAGE</p>
            <el-menu
              :default-active="activeMenu"
              class="side-menu side-menu-admin"
              :collapse-transition="false"
              @select="handleMenuSelect"
            >
              <el-menu-item index="/admin/dashboard">
                <el-icon class="nav-icon nav-icon-admin"><DataAnalysis /></el-icon>
                <span>后台管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/notice">
                <el-icon class="nav-icon nav-icon-admin"><Bell /></el-icon>
                <span>公告管理</span>
              </el-menu-item>
            </el-menu>
          </div>
        </div>
      </el-aside>

      <el-container class="content-shell" direction="vertical">
        <el-header class="header header-mobile-compact" :class="{ 'header-home-mobile': isHomePage }">
          <div class="header-copy">
            <h2 class="header-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <el-button
              v-if="isHomePage"
              class="notice-bell-btn"
              text
              aria-label="查看公告"
              title="查看公告"
              @click="openNoticeDialog"
            >
              <el-badge :is-dot="hasNewNotice" :offset="[-2, 4]">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </el-button>
            <template v-if="userStore.isLogin()">
              <el-button class="publish-btn" type="primary" @click="goToPublish" icon="Edit">
                写文章
              </el-button>
              <el-dropdown @command="handleCommand">
                <div class="user-info">
                  <el-avatar :size="35" :src="userStore.userInfo?.avatar">
                    {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile" icon="User">我的设置</el-dropdown-item>
                    <el-dropdown-item command="logout" icon="SwitchButton" divided>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <div class="user-info guest-user-info" @click="goToLogin">
                <el-avatar :size="35">U</el-avatar>
              </div>
              <el-button class="header-auth-btn" @click="goToLogin">登录</el-button>
            </template>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>

        <el-footer class="footer">
          <p>@2026小怀风的first javaweb project</p>
        </el-footer>
      </el-container>
    </el-container>

    <NoticeDialog />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  DataAnalysis,
  Document,
  Files,
  FolderOpened,
  House,
  Link,
  StarFilled,
  Tools,
  User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useNotice } from '@/composables/useNotice'
import NoticeDialog from '@/components/NoticeDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 公告：单例状态（弹窗 + 铃铛共享）
const { hasNew: hasNewNotice, autoCheck, openDialog: openNoticeDialog, closeDialog: closeNoticeDialog } = useNotice()

// 进入首页时自动检查公告；离开首页时关闭弹窗
watch(
  () => route.path,
  path => {
    if (path === '/home') {
      autoCheck()
    } else {
      closeNoticeDialog()
    }
  },
  { immediate: true }
)

const sidebarExpanded = ref(false)

const isAdmin = computed(() => Number(userStore.userInfo?.role) === 1)
const currentYear = new Date().getFullYear()
const defaultOpenMenus = computed(() => ['resource-menu'])
const brandTitle = computed(() => {
  if (userStore.isLogin()) {
    return userStore.userInfo?.nickname || userStore.userInfo?.username || '我的博客'
  }

  return '小枫风的博客'
})

const activeMenu = computed(() => {
  if (route.path === '/resource') {
    return '/resource/all'
  }
  if (route.path.startsWith('/post')) {
    return '/post/list'
  }
  if (route.path.startsWith('/toolbox')) {
    return '/toolbox'
  }
  if (route.path.startsWith('/admin/notice')) {
    return '/admin/notice'
  }
  if (route.path.startsWith('/admin')) {
    return '/admin/dashboard'
  }
  if (route.path.startsWith('/profile')) {
    return '/profile'
  }
  return route.path
})

const isHomePage = computed(() => activeMenu.value === '/home')

const pageTitle = computed(() => {
  if (route.path.startsWith('/post/publish')) return '写文章'
  if (route.path.startsWith('/post/detail')) return '文章详情'
  if (route.path.startsWith('/post')) return '文章'
  if (route.path.startsWith('/resource')) return '资源'
  if (route.path.startsWith('/toolbox/file-convert')) return '文件转换中心'
  if (route.path.startsWith('/toolbox')) return '工具'
  if (route.path.startsWith('/profile')) return '我的'
  if (route.path.startsWith('/admin')) return '后台管理'
  return '首页'
})

const handleMenuSelect = index => {
  router.push(index)
}

const goToPublish = () => {
  router.push('/post/publish')
}

const goToLogin = () => {
  router.push('/login')
}

const handleCommand = command => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }

  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.clearUserInfo()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background:
    radial-gradient(circle at 14% 16%, rgba(255, 255, 255, 0.82) 0, rgba(255, 255, 255, 0) 18%),
    linear-gradient(180deg, #f8fbff 0%, #f1f8ff 28%, #e9f3ff 100%);
}

.layout-shell {
  min-height: 100vh;
}

.sidebar {
  width: 88px !important;
  height: 100vh;
  position: sticky;
  top: 0;
  padding: 24px 12px 18px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(248, 252, 255, 0.7) 0%, rgba(237, 247, 255, 0.66) 100%);
  border-right: 1px solid rgba(212, 230, 245, 0.55);
  backdrop-filter: blur(18px);
  transition: width 0.32s ease, padding 0.32s ease;
}

.sidebar:hover,
.sidebar-expanded {
  width: 280px !important;
  padding-left: 18px;
  padding-right: 18px;
}

.sidebar-expanded .brand-copy,
.sidebar:hover .brand-copy {
  width: auto;
  opacity: 1;
}

.sidebar-expanded .sidebar-admin-label,
.sidebar:hover .sidebar-admin-label {
  opacity: 1;
}

.sidebar-expanded .side-menu :deep(.el-menu-item),
.sidebar-expanded .side-menu :deep(.el-sub-menu__title),
.sidebar:hover .side-menu :deep(.el-menu-item),
.sidebar:hover .side-menu :deep(.el-sub-menu__title) {
  justify-content: flex-start;
  border-radius: 22px 0 0 22px;
}

.sidebar-expanded .side-menu :deep(.el-menu-item span),
.sidebar-expanded .side-menu :deep(.el-sub-menu__title span),
.sidebar:hover .side-menu :deep(.el-menu-item span),
.sidebar:hover .side-menu :deep(.el-sub-menu__title span) {
  width: auto;
  opacity: 1;
  margin-left: 18px;
  transform: translateX(0);
}

.sidebar-toggle-btn {
  display: none;
  position: absolute;
  top: 20px;
  right: -40px;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(212, 230, 245, 0.88);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  z-index: 110;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(121, 164, 206, 0.12);
}

.toggle-bar {
  display: block;
  width: 16px;
  height: 2px;
  border-radius: 2px;
  background: #6d88a6;
  transition: transform 0.2s ease;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16px;
  width: 100%;
  min-height: 44px;
  margin: 8px 0 22px;
  padding: 0 8px;
  background: transparent;
  box-shadow: none;
  flex: 0 0 auto;
  cursor: pointer;
}

/* 收起态：头像水平居中，与菜单图标对齐 */
.sidebar:not(:hover):not(.sidebar-expanded) .sidebar-brand {
  justify-content: center;
  padding-left: 0;
  padding-right: 0;
}

.brand-avatar {
  flex-shrink: 0;
  box-shadow: 0 10px 22px rgba(95, 159, 224, 0.18);
}

.brand-copy {
  min-width: 0;
  width: 0;
  opacity: 0;
  overflow: hidden;
  transition: width 0.22s ease, opacity 0.18s ease;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: #111111;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding-top: 0;
}

.side-menu {
  border-right: none;
  background: transparent;
  overflow: visible;
}

.sidebar-admin-entry {
  margin-top: auto;
  padding-top: 18px;
}

.sidebar-admin-label {
  margin: 0 18px 12px;
  color: #111111;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  opacity: 0;
  transition: opacity 0.22s ease;
}

.side-menu-admin {
  padding-top: 14px;
  border-top: 1px solid rgba(212, 230, 245, 0.72);
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  position: relative;
  width: 100%;
  height: 58px;
  line-height: 58px;
  margin-bottom: 10px;
  padding: 0 18px !important;
  justify-content: center;
  border-radius: 22px;
  font-size: 16px;
  font-weight: 600;
  color: #6d88a6;
  transition: color 0.25s ease, background 0.25s ease, transform 0.25s ease;
}

.side-menu :deep(.el-menu-item .el-icon),
.side-menu :deep(.el-sub-menu__title .el-icon) {
  display: inline-flex;
  align-items: center;
  align-self: center;
  width: 24px;
  height: 24px;
  min-width: 24px;
  margin-left: 0;
  margin-right: 0;
  line-height: 24px;
  font-size: 20px;
  justify-content: center;
}

.side-menu :deep(.el-menu-item .el-icon svg),
.side-menu :deep(.el-sub-menu__title .el-icon svg) {
  display: block;
}

.sidebar:not(:hover):not(.sidebar-expanded) .side-menu :deep(.el-menu-item .el-icon),
.sidebar:not(:hover):not(.sidebar-expanded) .side-menu :deep(.el-sub-menu__title .el-icon) {
  transform: none;
}

/* 收起态：二级菜单项文字隐藏、图标纯居中，与一级图标列对齐 */
.sidebar:not(:hover):not(.sidebar-expanded) .side-menu :deep(.el-sub-menu .el-menu-item span) {
  width: 0;
  opacity: 0;
  margin-left: 0;
  overflow: hidden;
  transform: translateX(10px);
}

.side-menu :deep(.el-menu-item span),
.side-menu :deep(.el-sub-menu__title span) {
  width: 0;
  opacity: 0;
  overflow: hidden;
  white-space: nowrap;
  color: #111111;
  transform: translateX(10px);
  transition: width 0.22s ease, opacity 0.18s ease, transform 0.22s ease, margin-left 0.22s ease;
}

.side-menu :deep(.el-sub-menu__icon-arrow) {
  display: none !important;
}

.side-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: auto;
  margin-left: 0;
  border-radius: 16px;
  font-size: 15px;
}

.side-menu :deep(.el-menu--popup) {
  min-width: 92px !important;
  padding-left: 2px;
  padding-right: 2px;
}

.side-menu :deep(.el-menu--popup .el-menu-item) {
  margin-right: 14px;
}

.side-menu :deep(.el-menu--popup .el-menu-item:hover) {
  background: transparent;
  transform: none;
}

.side-menu :deep(.el-sub-menu .el-menu-item span) {
  width: auto;
  opacity: 1;
  margin-left: 18px;
  transform: none;
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  color: #5d93c7;
  background: rgba(220, 238, 251, 0.72);
  transform: translateX(2px);
}

.side-menu :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: #5d93c7;
  background: linear-gradient(135deg, rgba(186, 220, 248, 0.2), rgba(232, 245, 255, 0.72));
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #5d93c7;
  background: linear-gradient(135deg, rgba(186, 220, 248, 0.36), rgba(234, 245, 255, 0.96));
  box-shadow: inset 0 0 0 1px rgba(202, 228, 247, 0.85), 0 16px 28px rgba(117, 167, 217, 0.18);
}

.sidebar-expanded .side-menu :deep(.el-sub-menu .el-menu-item.is-active),
.sidebar:hover .side-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: transparent;
  box-shadow: none;
}

.sidebar:not(:hover):not(.sidebar-expanded) .side-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: transparent;
  box-shadow: none;
}

.side-menu :deep(.el-menu--popup .el-menu-item.is-active) {
  background: transparent;
  box-shadow: none;
}

.content-shell {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.header {
  background: rgba(250, 253, 255, 0.78);
  border-bottom: 1px solid rgba(215, 232, 247, 0.85);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  min-height: 88px;
  position: sticky;
  top: 0;
  z-index: 99;
  backdrop-filter: blur(12px);
}

.header-copy {
  min-width: 0;
}

.header-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1f2a36;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
}

.publish-btn {
  --el-button-bg-color: #5f9fe0;
  --el-button-border-color: #5f9fe0;
  --el-button-hover-bg-color: #4f93d7;
  --el-button-hover-border-color: #4f93d7;
  --el-button-active-bg-color: #427fbe;
  --el-button-active-border-color: #427fbe;
  box-shadow: 0 12px 24px rgba(95, 159, 224, 0.26);
}

.notice-bell-btn {
  --el-text-color-regular: #5d8bb8;
  padding: 6px;
  border-radius: 10px;
  transition: background 0.25s ease, transform 0.2s ease;
}

.notice-bell-btn:hover {
  background: rgba(217, 235, 248, 0.68);
  color: #5d93c7;
  transform: translateY(-1px);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 999px;
  transition: background 0.3s ease;
}

.user-info:hover {
  background: rgba(217, 235, 248, 0.68);
}

.username {
  max-width: 12ch;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #2f3c4b;
  font-size: 14px;
  font-weight: 600;
}

.main-content {
  flex: 1;
  width: 100%;
  min-width: 0;
  padding: 28px 32px 36px;
}

.footer {
  padding: 18px 20px 24px;
  background: transparent;
  text-align: center;
  color: #8ea5bb;
  font-size: 14px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@media (max-width: 900px) {
  .sidebar-toggle-btn {
    display: flex;
  }
  .layout-shell {
    flex-direction: column;
  }

  .sidebar,
  .sidebar:hover {
    width: 100% !important;
    height: auto;
    position: relative;
    padding: 24px 18px 12px;
    box-shadow: none;
  }

  .sidebar-admin-label,
  .side-menu :deep(.el-menu-item span),
  .side-menu :deep(.el-sub-menu__title span),
  .side-menu :deep(.el-sub-menu .el-menu-item span) {
    width: auto !important;
    opacity: 1 !important;
    margin-left: 18px !important;
    transform: none !important;
    overflow: visible;
  }

  .side-menu :deep(.el-sub-menu__icon-arrow) {
    display: none !important;
  }

  .sidebar-brand,
  .sidebar:hover .sidebar-brand {
    width: 100%;
    min-height: 44px;
    margin: 8px 0 22px;
    padding: 0 8px;
  }

  .side-menu :deep(.el-menu-item),
  .side-menu :deep(.el-sub-menu__title) {
    justify-content: flex-start;
    border-radius: 18px;
  }

  .header {
    padding: 18px 20px;
    min-height: auto;
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .header-copy,
  .header-right {
    width: 100%;
  }

  .header-right {
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }

  .publish-btn {
    flex: 1 1 180px;
  }

  .main-content {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .sidebar,
  .sidebar:hover {
    padding: calc(18px + env(safe-area-inset-top)) 14px 10px;
  }

  .logo {
    font-size: 20px;
  }

  .username {
    display: none;
  }

  .brand-avatar {
    --el-avatar-size: 40px;
  }

  .header {
    padding: calc(16px + env(safe-area-inset-top)) 14px;
    gap: 12px;
  }

  .header-mobile-compact {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    align-items: start;
    column-gap: 10px;
    row-gap: 10px;
  }

  .header-mobile-compact .header-right {
    grid-column: 1 / -1;
    width: 100%;
    justify-content: flex-start;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  /* 铃铛：保证 44px 触控点击区 */
  .notice-bell-btn {
    min-width: 44px;
    min-height: 44px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
  }

  .header-title {
    margin-bottom: 0;
    font-size: 20px;
  }

  .publish-btn {
    width: 100%;
    flex: 1 1 100%;
  }

  .main-content {
    padding: 16px 14px 24px;
  }

  .footer {
    padding: 18px 20px calc(24px + env(safe-area-inset-bottom));
  }
}
</style>
