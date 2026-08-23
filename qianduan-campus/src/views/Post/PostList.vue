<template>
  <div class="post-list-container">
    <el-card class="search-card" shadow="never">
      <div class="search-panel">
        <div class="search-copy">
          <h2 class="search-title">文章列表</h2>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item>
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索文章标题或内容"
              clearable
              style="width: 320px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
            <el-button @click="handleReset" icon="Refresh">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <div v-loading="loading" class="post-list">
      <el-empty v-if="postList.length === 0 && !loading" description="暂无文章" />
      <template v-if="loading && postList.length === 0">
        <el-skeleton v-for="i in 3" :key="i" animated :throttle="300" class="post-skeleton-card">
          <template #template>
            <div class="skeleton-author">
              <el-skeleton-item variant="circle" style="width: 28px; height: 28px;" />
              <div class="skeleton-author-text">
                <el-skeleton-item variant="text" style="width: 80px; height: 14px;" />
                <el-skeleton-item variant="text" style="width: 100px; height: 12px;" />
              </div>
            </div>
            <el-skeleton-item variant="h3" style="width: 65%; height: 22px; margin: 16px 0 10px;" />
            <el-skeleton-item variant="text" style="width: 95%; height: 15px; margin-bottom: 6px;" />
            <el-skeleton-item variant="text" style="width: 70%; height: 15px; margin-bottom: 14px;" />
            <div class="skeleton-footer">
              <el-skeleton-item variant="text" style="width: 60px; height: 14px;" />
              <div class="skeleton-meta">
                <el-skeleton-item variant="text" style="width: 40px; height: 14px;" />
                <el-skeleton-item variant="text" style="width: 40px; height: 14px;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>
      <el-card
        v-for="post in postList"
        :key="post.id"
        class="post-card"
        shadow="hover"
        @click="goToDetail(post.id)"
      >
        <div class="post-card-top">
          <div class="post-author">
            <el-avatar :size="28" :src="getAuthorAvatar(post)">
              {{ getAuthorInitial(post) }}
            </el-avatar>
            <div class="post-author-copy">
              <span class="post-author-name" :class="{ 'owner-name': isSiteOwner(getAuthorName(post)) }">
                {{ getAuthorName(post) }}
                <SiteBadge v-if="isSiteOwner(getAuthorName(post))" />
              </span>
              <span class="post-date">{{ post.createTime }}</span>
            </div>
          </div>
        </div>

        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-content">{{ post.content }}</p>

        <div class="post-footer">
          <span class="post-link">继续阅读</span>
          <div class="post-meta">
            <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
            <span><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      class="pagination"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="loadPostList"
      @size-change="loadPostList"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Search, Star } from '@element-plus/icons-vue'
import { postAPI } from '@/api/post'
import { useUserStore } from '@/store/user'
import SiteBadge from '@/components/SiteBadge.vue'
import { isSiteOwner } from '@/composables/useSiteOwner'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const postList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: ''
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

const loadPostList = async () => {
  loading.value = true

  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    let data
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
      data = await postAPI.searchPost(params)
    } else {
      data = await postAPI.getPostList(params)
    }

    postList.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadPostList()
}

const handleReset = () => {
  searchForm.keyword = ''
  currentPage.value = 1
  loadPostList()
}

const goToDetail = (id) => {
  router.push(`/post/detail/${id}`)
}

onMounted(() => {
  loadPostList()
})
</script>

<style scoped>
.post-list-container {
  max-width: 1000px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 24px;
  border-radius: 24px;
  border: 1px solid rgba(208, 228, 245, 0.85);
  background: linear-gradient(180deg, #ffffff 0%, #f5faff 100%);
  box-shadow: 0 14px 28px rgba(176, 206, 230, 0.12);
}

.search-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.search-title {
  margin: 0;
  color: #213142;
  font-size: 28px;
}

.post-list {
  min-height: 400px;
}

.post-card {
  margin-bottom: 18px;
  border-radius: 22px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(217, 233, 247, 0.85);
  background: #ffffff;
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 34px rgba(156, 189, 218, 0.18);
}

.post-card-top {
  margin-bottom: 16px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-author-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.post-author-name {
  color: #2c3f53;
  font-size: 14px;
  font-weight: 600;
}

.post-author-name.owner-name {
  color: #e67e22;
}

.post-date {
  color: #8b9bae;
  font-size: 12px;
}

.post-title {
  margin: 0 0 12px;
  overflow: hidden;
  color: #1f2a36;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.45;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-content {
  display: -webkit-box;
  overflow: hidden;
  margin: 0 0 20px;
  color: #596c7f;
  font-size: 15px;
  line-height: 1.8;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(234, 241, 247, 0.9);
}

.post-link {
  color: #6798c9;
  font-size: 14px;
  font-weight: 700;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #8a99ab;
  font-size: 13px;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 26px;
}

.post-skeleton-card {
  margin-bottom: 18px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  padding: 20px;
}

.skeleton-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.skeleton-author-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid rgba(234, 241, 247, 0.9);
}

.skeleton-meta {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .search-card {
    border-radius: 20px;
  }

  .search-panel {
    align-items: stretch;
  }

  .search-form {
    width: 100%;
  }

  .search-form :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }

  .search-form :deep(.el-input) {
    width: 100% !important;
  }

  .search-form :deep(.el-form-item__content) {
    width: 100%;
  }

  .post-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .search-card {
    margin-bottom: 18px;
    border-radius: 18px;
  }

  .search-panel {
    gap: 14px;
  }

  .search-title {
    font-size: 22px;
  }

  .search-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .search-form :deep(.el-form-item) {
    margin-bottom: 0;
  }

  .search-form :deep(.el-form-item:last-child .el-form-item__content) {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .search-form :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }

  .post-card {
    border-radius: 18px;
  }

  .post-title {
    font-size: 19px;
  }

  .post-content {
    font-size: 14px;
    line-height: 1.7;
  }

  .post-meta {
    width: 100%;
    justify-content: space-between;
  }

  .pagination :deep(.el-pagination__sizes),
  .pagination :deep(.el-pagination__jump),
  .pagination :deep(.btn-prev),
  .pagination :deep(.btn-next),
  .pagination :deep(.el-pager) {
    display: none;
  }
}
</style>
