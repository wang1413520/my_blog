import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { hideLayout: true }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'post/list',
        name: 'PostList',
        component: () => import('@/views/Post/PostList.vue'),
        meta: { title: '文章' }
      },
      {
        path: 'post/detail/:id',
        name: 'PostDetail',
        component: () => import('@/views/Post/PostDetail.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'post/publish',
        name: 'PostPublish',
        component: () => import('@/views/Post/PostPublish.vue'),
        meta: { title: '写文章', requireAuth: true }
      },
      {
        path: 'resource',
        redirect: '/resource/all'
      },
      {
        path: 'resource/all',
        name: 'Resource',
        component: () => import('@/views/Resource/ResourceList.vue'),
        meta: { title: '资源' }
      },
      {
        path: 'resource/featured',
        name: 'ResourceFeatured',
        component: () => import('@/views/Resource/ResourceList.vue'),
        meta: { title: '站长主推' }
      },
      {
        path: 'resource/links',
        name: 'ResourceLinks',
        component: () => import('@/views/Resource/LinkShareList.vue'),
        meta: { title: '链接分享' }
      },
      {
        path: 'toolbox',
        name: 'ToolboxHome',
        component: () => import('@/views/Toolbox/ToolboxHome.vue'),
        meta: { title: '工具' }
      },
      {
        path: 'toolbox/file-convert',
        name: 'FileConvertCenter',
        component: () => import('@/views/Toolbox/FileConvertCenter.vue'),
        meta: { title: '文件转换中心', requireAuth: true, requireAdmin: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '我的', requireAuth: true }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/Admin/AdminDashboard.vue'),
        meta: { title: '后台管理', requireAuth: true, requireAdmin: true }
      },
      {
        path: 'admin/notice',
        name: 'AdminNotice',
        component: () => import('@/views/Admin/AdminNotice.vue'),
        meta: { title: '公告管理', requireAuth: true, requireAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requireAuth && !userStore.isLogin()) {
    next('/login')
    return
  }

  if (to.meta.requireAdmin && Number(userStore.userInfo?.role) !== 1) {
    ElMessage.warning('该功能正在完善中，敬请期待')
    next(false)
    return
  }

  next()
})

export default router
