<template>
  <div class="home-container">
    <section class="home-stage">
      <div class="stage-bubbles" aria-hidden="true">
        <span class="bubble bubble-1"></span>
        <span class="bubble bubble-2"></span>
        <span class="bubble bubble-3"></span>
        <span class="bubble bubble-4"></span>
        <span class="bubble bubble-5"></span>
        <span class="bubble bubble-6"></span>
        <span class="bubble bubble-7"></span>
        <span class="bubble bubble-8"></span>
      </div>

      <el-card class="welcome-card" shadow="hover">
        <div class="welcome-grid">
          <div class="welcome-main">
            <div class="welcome-top">
              <div class="welcome-calendar">
                <TodayCalendar />
              </div>
              <div class="welcome-content">
                <MuyuWidget />
              </div>
            </div>

            <div class="welcome-feature">
              <section class="feature-mascot" aria-hidden="true">
                <img class="feature-mascot-image" :src="nanachiMascot" alt="" />
              </section>

              <section class="daily-quote">
                <p class="daily-quote-label">DAILY NOTE</p>
                <p class="daily-quote-text">{{ dailyQuote }}</p>
              </section>
            </div>
          </div>

          <div class="welcome-memo">
            <MemoBoard />
          </div>
        </div>
      </el-card>

      <el-card class="hot-posts-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">最新文章</span>
          </div>
        </template>
        <el-skeleton v-if="articlesLoading" animated :rows="3" :throttle="300">
          <template #template>
            <div class="post-skeleton-list">
              <div v-for="i in 3" :key="i" class="post-skeleton-item">
                <el-skeleton-item variant="h3" style="width: 60%; height: 22px; margin-bottom: 10px;" />
                <el-skeleton-item variant="text" style="width: 90%; height: 16px; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 40%; height: 14px;" />
              </div>
            </div>
          </template>
        </el-skeleton>
        <el-empty v-else-if="latestArticles.length === 0" description="暂时还没有文章" />
        <div v-else class="post-list">
          <div v-for="(column, columnIndex) in articleColumns" :key="columnIndex" class="post-column">
            <div
              v-for="post in column"
              :key="post.id"
              class="post-item"
              @click="goToPostDetail(post.id)"
            >
              <div class="post-header-row">
                <el-avatar class="post-avatar" :src="getAuthorAvatar(post)">
                  {{ getAuthorInitial(post) }}
                </el-avatar>
                <span class="post-author-group">
                  <span class="post-nickname" :class="{ 'owner-name': isSiteOwner(getAuthorName(post)) }">
                    {{ getAuthorName(post) }}
                  </span>
                  <SiteBadge v-if="isSiteOwner(getAuthorName(post))" />
                </span>
                <h4 class="post-title">{{ post.title }}</h4>
              </div>
              <p class="post-excerpt">{{ getPostExcerpt(post) }}</p>
              <div class="post-meta">
                <span>评论 {{ post.commentCount || 0 }}</span>
                <span>点赞 {{ post.likeCount || 0 }}</span>
                <span class="post-time">{{ post.createTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MemoBoard from '@/components/MemoBoard.vue'
import MuyuWidget from '@/components/MuyuWidget.vue'
import TodayCalendar from '@/components/TodayCalendar.vue'
import nanachiMascot from '@/assets/luoxiaohei_mascot_cutout.gif'
import { postAPI } from '@/api/post'
import SiteBadge from '@/components/SiteBadge.vue'
import { isSiteOwner } from '@/composables/useSiteOwner'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const latestArticles = ref([])
const articlesLoading = ref(false)
const dailyQuote = '今天也要慢一点、稳一点，把最重要的一件小事做好。'

const isNarrow = ref(false)

const updateViewport = () => {
  isNarrow.value = window.matchMedia('(max-width: 900px)').matches
}

const articleColumns = computed(() => {
  const posts = latestArticles.value
  if (isNarrow.value) {
    return [posts]
  }

  const left = []
  const right = []
  posts.forEach((post, index) => {
    if (index % 2 === 0) {
      left.push(post)
    } else {
      right.push(post)
    }
  })
  return [left, right]
})

const getAuthorName = (post) => {
  if (post?.isAnonymous === 1) {
    return '站点作者'
  }

  return post?.authorName || '站点作者'
}

const getAuthorInitial = (post) => {
  return getAuthorName(post).charAt(0)
}

const isCurrentUserName = (name) => {
  return name === userStore.userInfo?.nickname || name === userStore.userInfo?.username
}

const getAuthorAvatar = (post) => {
  if (post?.isAnonymous === 1) {
    return ''
  }

  return post?.authorAvatar || (isCurrentUserName(post?.authorName) ? (userStore.userInfo?.avatar || '') : '')
}

const goToPostDetail = (id) => {
  router.push(`/post/detail/${id}`)
}

const getPostExcerpt = (post) => {
  const text = (post?.content || post?.title || '').trim()
  if (!text) {
    return '点进来看看这篇文章里记录了什么。'
  }

  return text
}

const loadLatestArticles = async () => {
  articlesLoading.value = true
  try {
    const data = await postAPI.getPostList({ page: 1, size: 10 })
    latestArticles.value = data.records || []
  } catch (error) {
    console.error('加载最新文章失败', error)
  } finally {
    articlesLoading.value = false
  }
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
  loadLatestArticles()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewport)
})
</script>

<style scoped>
.home-container {
  max-width: 1280px;
  margin: 0 auto;
}

.home-stage {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  padding: 26px;
  border-radius: 34px;
  background:
    radial-gradient(circle at 16% 16%, rgba(255, 255, 255, 0.95) 0, rgba(255, 255, 255, 0) 22%),
    radial-gradient(circle at 86% 18%, rgba(228, 244, 255, 0.85) 0, rgba(228, 244, 255, 0) 24%),
    radial-gradient(circle at 84% 74%, rgba(255, 255, 255, 0.55) 0, rgba(255, 255, 255, 0) 20%),
    linear-gradient(180deg, #f7fbff 0%, #eef7ff 40%, #e4f1ff 100%);
  box-shadow: inset 0 0 0 1px rgba(214, 233, 248, 0.8), 0 24px 54px rgba(169, 200, 228, 0.22);
}

.home-stage::before,
.home-stage::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.home-stage::before {
  background:
    radial-gradient(circle at 10% 18%, rgba(255, 255, 255, 0.9) 0 10px, transparent 11px),
    radial-gradient(circle at 22% 58%, rgba(255, 255, 255, 0.72) 0 13px, transparent 14px),
    radial-gradient(circle at 36% 28%, rgba(214, 236, 255, 0.5) 0 8px, transparent 9px),
    radial-gradient(circle at 52% 74%, rgba(255, 255, 255, 0.78) 0 11px, transparent 12px),
    radial-gradient(circle at 68% 20%, rgba(222, 242, 255, 0.55) 0 8px, transparent 9px),
    radial-gradient(circle at 84% 34%, rgba(255, 255, 255, 0.76) 0 12px, transparent 13px);
  opacity: 0.9;
}

.home-stage::after {
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0) 24%),
    radial-gradient(circle at 26% 34%, rgba(255, 255, 255, 0.24) 0, rgba(255, 255, 255, 0) 24%);
  mix-blend-mode: screen;
}

.stage-bubbles {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.bubble {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.34);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow:
    inset -8px -10px 18px rgba(255, 255, 255, 0.18),
    inset 8px 8px 18px rgba(125, 185, 230, 0.1),
    0 10px 24px rgba(175, 213, 241, 0.2);
  backdrop-filter: blur(3px);
}

.bubble::after {
  content: '';
  position: absolute;
  top: 18%;
  left: 24%;
  width: 24%;
  height: 24%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.72);
  filter: blur(1px);
}

.bubble-1 { top: 12%; left: 6%; width: 34px; height: 34px; }
.bubble-2 { top: 56%; left: 10%; width: 54px; height: 54px; }
.bubble-3 { top: 24%; left: 31%; width: 24px; height: 24px; }
.bubble-4 { top: 16%; right: 16%; width: 46px; height: 46px; }
.bubble-5 { top: 62%; right: 8%; width: 30px; height: 30px; }
.bubble-6 { top: 38%; right: 25%; width: 20px; height: 20px; }
.bubble-7 { bottom: 14%; left: 46%; width: 42px; height: 42px; }
.bubble-8 { top: 8%; right: 3%; width: 22px; height: 22px; }

.welcome-card,
.hot-posts-card {
  position: relative;
  z-index: 2;
  border-radius: 28px;
  border: 1px solid rgba(208, 228, 245, 0.8);
  box-shadow: 0 18px 36px rgba(170, 201, 228, 0.16);
}

.welcome-card {
  margin-bottom: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f1f8ff 100%);
}

.welcome-card :deep(.el-card__body) {
  position: relative;
  z-index: 2;
  padding: 24px 28px;
}

.welcome-grid {
  --welcome-panel-height: 456px;
  display: grid;
  grid-template-columns: minmax(560px, 1.35fr) minmax(320px, 0.92fr);
  align-items: stretch;
  gap: 22px;
}

.welcome-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: var(--welcome-panel-height);
  min-width: 0;
  min-height: 0;
}

.welcome-top {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(260px, 1fr);
  align-items: stretch;
  gap: 22px;
  min-height: 200px;
}

.welcome-calendar,
.welcome-content {
  min-width: 0;
}

.welcome-content {
  display: flex;
  align-items: flex-start;
  padding-left: 10px;
  box-sizing: border-box;
  min-height: 200px;
  min-width: 0;
}

.welcome-feature {
  display: grid;
  grid-template-columns: minmax(200px, 228px) minmax(280px, 1fr);
  gap: 18px;
  min-height: 172px;
  min-width: 0;
  align-items: end;
}

.feature-mascot {
  display: flex;
  position: relative;
  align-items: flex-end;
  justify-content: center;
  min-height: 172px;
  padding: 0;
  overflow: visible;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  pointer-events: none;
}

.feature-mascot::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 18px;
  width: 138px;
  height: 26px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(163, 205, 238, 0.34) 0%, rgba(163, 205, 238, 0) 72%);
  transform: translateX(-58%);
  filter: blur(4px);
  opacity: 0.9;
}

.feature-mascot-image {
  display: block;
  width: auto;
  height: auto;
  max-width: min(100%, 188px);
  max-height: 196px;
  object-fit: contain;
  object-position: center bottom;
  box-shadow: none;
  opacity: 0.96;
  filter: drop-shadow(0 14px 20px rgba(137, 176, 206, 0.18));
  animation: mascotFloat 4.8s ease-in-out infinite;
  transform: translate(-6px, 10px);
}

.daily-quote {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  min-height: 172px;
  padding: 2px 8px 0 0;
}

.daily-quote-label {
  margin: 0 0 10px;
  color: #86add3;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.daily-quote-text {
  margin: 0;
  color: #243447;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.5;
  letter-spacing: 0.01em;
}

.welcome-memo {
  display: flex;
  height: var(--welcome-panel-height);
  min-width: 0;
  min-height: 0;
}

.hot-posts-card {
  background: linear-gradient(180deg, #ffffff 0%, #f2f9ff 100%);
}

.hot-posts-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(217, 234, 247, 0.8);
}

.card-header {
  display: flex;
  align-items: center;
}

.card-title {
  display: block;
  font-size: 24px;
  font-weight: 800;
  color: #1f2a36;
}

.post-list {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.post-column {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 0;
}

.post-item {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(220, 236, 248, 0.85);
  background: #ffffff;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 10px 24px rgba(181, 209, 231, 0.1);
}

.post-item:hover {
  border-color: rgba(162, 202, 236, 0.95);
  background: linear-gradient(180deg, rgba(234, 245, 255, 0.9) 0%, rgba(248, 252, 255, 0.92) 100%);
  transform: translateX(4px) translateY(-2px);
  box-shadow: 0 18px 34px rgba(144, 185, 219, 0.22);
}

.post-avatar {
  --el-avatar-size: 32px;
  width: 32px;
  height: 32px;
  font-size: 14px;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px rgba(220, 236, 248, 0.85);
  background: #f2f7fc;
}

.post-header-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.post-author-group {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.post-nickname {
  font-size: 15px;
  font-weight: 600;
  color: #2c3f53;
  white-space: nowrap;
}

.post-nickname.owner-name {
  color: #e67e22;
}

.post-title {
  display: -webkit-box;
  flex: 1 1 auto;
  overflow: hidden;
  margin: 0;
  min-width: 0;
  color: #1f2f44;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.5;
  letter-spacing: 0.01em;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.post-excerpt {
  display: -webkit-box;
  overflow: hidden;
  margin: 12px 0 0;
  color: #2b5ea8;
  font-size: 14px;
  line-height: 1.7;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
  margin-top: 14px;
  font-size: 13px;
  color: #6b7a8b;
}

.post-meta .post-time {
  color: #8b9bae;
}

.owner-name {
  color: #e67e22;
  font-weight: 600;
}

.post-skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-skeleton-item {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(220, 236, 248, 0.85);
  background: #ffffff;
}

@keyframes mascotFloat {
  0%,
  100% {
    transform: translate(-6px, 10px) translateY(0);
  }

  50% {
    transform: translate(-6px, 10px) translateY(-6px);
  }
}

@media (max-width: 900px) {
  .home-stage {
    padding: 18px;
    border-radius: 24px;
  }

  .post-list {
    flex-direction: column;
  }

  .welcome-card :deep(.el-card__body) {
    padding: 22px 18px;
  }

  .welcome-grid {
    --welcome-panel-height: auto;
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .welcome-main,
  .welcome-memo {
    height: auto;
  }

  .welcome-top,
  .welcome-feature {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .welcome-content {
    padding-left: 0;
    min-height: 0;
    justify-content: center;
  }

  .feature-mascot {
    min-height: 0;
    justify-content: center;
    align-items: center;
  }

  .feature-mascot::after {
    width: 120px;
    bottom: 10px;
    transform: translateX(-50%);
  }

  .daily-quote {
    min-height: 0;
    padding: 0;
  }

  .daily-quote-text {
    font-size: 26px;
  }

  .feature-mascot-image {
    max-width: min(100%, 164px);
    max-height: 168px;
    transform: none;
  }

  .welcome-memo {
    min-height: 0;
  }

  .bubble-2,
  .bubble-4,
  .bubble-7 {
    display: none;
  }
}

@media (max-width: 768px) {
  .home-stage {
    padding: 14px;
    border-radius: 22px;
  }

  .welcome-card,
  .hot-posts-card {
    border-radius: 22px;
  }

  .welcome-card :deep(.el-card__body) {
    padding: 18px 14px;
  }

  .welcome-main {
    gap: 14px;
  }

  .daily-quote-text {
    font-size: 22px;
    line-height: 1.45;
  }

  .card-title {
    font-size: 20px;
  }

  .post-item {
    padding: 16px;
  }

  .post-avatar {
    --el-avatar-size: 28px;
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .post-meta {
    gap: 10px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .home-container {
    margin-top: -4px;
  }

  .home-stage {
    padding: 12px;
    border-radius: 18px;
  }

  .welcome-grid {
    gap: 16px;
  }

  .welcome-card :deep(.el-card__body) {
    padding-top: 14px;
  }

  .welcome-top,
  .welcome-feature {
    gap: 12px;
  }

  .welcome-content {
    justify-content: flex-start;
  }

  .feature-mascot {
    display: none;
  }

  .daily-quote-text {
    font-size: 18px;
  }

  .post-title {
    white-space: normal;
    line-height: 1.45;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .post-avatar {
    --el-avatar-size: 24px;
    width: 24px;
    height: 24px;
    font-size: 11px;
  }

  .post-item:hover {
    transform: none;
  }

  .bubble-1,
  .bubble-3,
  .bubble-5,
  .bubble-6,
  .bubble-8 {
    display: none;
  }
}
</style>
