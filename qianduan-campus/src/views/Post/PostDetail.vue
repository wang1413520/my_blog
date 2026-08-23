<template>
  <div class="post-detail-container">
    <el-card v-loading="loading" class="detail-card">
      <template #header>
        <div class="header-actions">
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
          <el-button
            v-if="isAuthor"
            type="danger"
            icon="Delete"
            @click="handleDelete"
          >
            删除文章
          </el-button>
        </div>
      </template>

      <div v-if="postDetail" class="post-content">
        <p class="post-kicker">ARTICLE</p>
        <h1 class="post-title">{{ postDetail.title }}</h1>

        <div class="post-meta">
          <div class="author-info">
            <el-avatar :size="40" :src="getAuthorAvatar(postDetail)">
              {{ getAuthorInitial(postDetail) }}
            </el-avatar>
            <div>
              <div class="author-name" :class="{ 'owner-name': isSiteOwnerAuthor }">
                {{ getAuthorName(postDetail) }}
                <SiteBadge v-if="isSiteOwnerAuthor" />
              </div>
              <div class="post-time">{{ postDetail.createTime }}</div>
            </div>
          </div>
          <div class="post-actions">
            <el-button @click="handleLike" :type="hasLiked ? 'primary' : 'default'" icon="Star">
              喜欢 {{ postDetail.likeCount || 0 }}
            </el-button>
          </div>
        </div>

        <div class="post-body">
          {{ postDetail.content }}
        </div>
      </div>
    </el-card>

    <el-card class="comment-card">
      <template #header>
        <div class="comment-header">
          <span class="comment-title">评论 / 留言 ({{ commentTotal }})</span>
        </div>
      </template>

      <div v-if="userStore.isLogin()" class="comment-input">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的想法..."
          maxlength="500"
          show-word-limit
        />
        <el-button
          type="primary"
          :loading="commentLoading"
          style="margin-top: 12px"
          @click="handleComment"
        >
          发布评论
        </el-button>
      </div>
      <div v-else class="login-tip">
        <el-alert type="info" :closable="false">
          <template #default>
            请先<el-button type="primary" link @click="goToLogin">登录</el-button>后再评论
          </template>
        </el-alert>
      </div>

      <div class="comment-list">
        <el-empty v-if="comments.length === 0" description="还没有评论，来留下第一条留言吧" />
        <div v-for="comment in comments" :key="comment.id" class="comment-item-wrap">
          <div class="comment-item">
            <el-avatar :size="36" :src="getCommentAvatar(comment)">
              {{ getCommentInitial(comment) }}
            </el-avatar>
            <div class="comment-content">
              <div class="comment-author" :class="{ 'owner-name': isSiteOwner(comment.authorName) }">
                {{ getCommentAuthorName(comment) }}
                <SiteBadge v-if="isSiteOwner(comment.authorName)" />
              </div>
              <div class="comment-text">{{ comment.content }}</div>
              <div class="comment-footer">
                <span class="comment-time">{{ comment.createTime }}</span>
                <div class="comment-footer-actions">
                  <el-button type="primary" link @click="startReply(comment, 'top')">回复</el-button>
                  <el-button
                    v-if="isCommentAuthor(comment)"
                    type="danger"
                    link
                    @click="handleDeleteComment(comment.id)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 楼中楼回复列表 -->
          <div v-if="comment.children && comment.children.length" class="reply-area">
            <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
              <el-avatar :size="28" :src="getCommentAvatar(reply)">
                {{ getCommentInitial(reply) }}
              </el-avatar>
              <div class="reply-content">
                <div class="reply-author">
                  <span class="reply-author-name" :class="{ 'owner-name': isSiteOwner(reply.authorName) }">
                    {{ getCommentAuthorName(reply) }}
                  </span>
                  <SiteBadge v-if="isSiteOwner(reply.authorName)" />
                  <span class="reply-to">回复 @{{ getReplyToName(reply, comment) }}</span>
                </div>
                <div class="reply-text">{{ reply.content }}</div>
                <div class="reply-footer">
                  <span class="comment-time">{{ reply.createTime }}</span>
                  <div class="comment-footer-actions">
                    <el-button type="primary" link @click="startReply(reply, 'reply')">回复</el-button>
                    <el-button
                      v-if="isCommentAuthor(reply)"
                      type="danger"
                      link
                      @click="handleDeleteReply(reply.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 就地回复输入框 -->
          <div v-if="isReplyingHere(comment)" class="reply-input">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              :placeholder="`回复 @${replyTargetName}`"
            />
            <div class="reply-input-actions">
              <el-button @click="cancelReply">取消</el-button>
              <el-button type="primary" :loading="replyLoading" @click="handleReply(comment)">
                回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { commentAPI } from '@/api/comment'
import { postAPI } from '@/api/post'
import { useUserStore } from '@/store/user'
import SiteBadge from '@/components/SiteBadge.vue'
import { isSiteOwner } from '@/composables/useSiteOwner'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const commentLoading = ref(false)
const postDetail = ref(null)
const comments = ref([])
const commentContent = ref('')
const hasLiked = ref(false)

// 楼中楼回复状态
const replyLoading = ref(false)
const replyContent = ref('')
const replyTarget = ref(null) // { id, name, type: 'top' | 'reply' }

const postId = computed(() => route.params.id)

const replyTargetName = computed(() => replyTarget.value?.name || '')

// 评论总数 = 顶层评论 + 楼中楼回复
const commentTotal = computed(() => {
  return comments.value.reduce((sum, comment) => {
    return sum + 1 + (comment.children?.length || 0)
  }, 0)
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

const getCommentAuthorName = (comment) => {
  return comment?.authorName || '访客'
}

const getCommentInitial = (comment) => {
  return getCommentAuthorName(comment).charAt(0)
}

const getCommentAvatar = (comment) => {
  return comment?.authorAvatar || (isCurrentUserName(comment?.authorName) ? (userStore.userInfo?.avatar || '') : '')
}

const isSiteOwnerAuthor = computed(() => {
  if (!postDetail.value) return false
  return isSiteOwner(postDetail.value.authorName)
})

const isAuthor = computed(() => {
  if (!userStore.isLogin() || !postDetail.value) {
    return false
  }

  return postDetail.value.authorName === userStore.userInfo?.nickname ||
    postDetail.value.authorName === userStore.userInfo?.username
})

const isCommentAuthor = (comment) => {
  if (!userStore.isLogin()) {
    return false
  }

  return comment.authorName === userStore.userInfo?.nickname ||
    comment.authorName === userStore.userInfo?.username
}

const loadPostDetail = async () => {
  loading.value = true

  try {
    const data = await postAPI.getPostDetail(postId.value)
    postDetail.value = data
    comments.value = data.comments || []
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('文章不存在或已被删除')
    goBack()
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!userStore.isLogin()) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await postAPI.likePost(postId.value)
    postDetail.value.likeCount = (postDetail.value.likeCount || 0) + 1
    hasLiked.value = true
    ElMessage.success('已添加喜欢')
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  commentLoading.value = true

  try {
    await commentAPI.addComment({
      postId: postId.value,
      content: commentContent.value
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadPostDetail()
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

const getReplyToName = (reply, comment) => {
  if (reply?.replyToUserName) {
    return reply.replyToUserName
  }
  // 后端未返回被@人时，回退显示顶层评论作者名
  return getCommentAuthorName(comment)
}

const startReply = (item, type) => {
  replyTarget.value = {
    id: item.id,
    name: getCommentAuthorName(item),
    type
  }
  replyContent.value = ''
}

const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
}

const isReplyingHere = (comment) => {
  if (!replyTarget.value) {
    return false
  }
  return replyTarget.value.id === comment.id || (comment.children || []).some(reply => reply.id === replyTarget.value.id)
}

const handleReply = async (comment) => {
  const target = replyTarget.value
  if (!target || !replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replyLoading.value = true

  try {
    // replyToUserId 由后端根据被回复记录推断，前端不传
    await commentAPI.addComment({
      postId: postId.value,
      content: replyContent.value,
      parentId: target.id
    })
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyTarget.value = null
    loadPostDetail()
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replyLoading.value = false
  }
}

const handleDeleteReply = async (replyId) => {
  ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await commentAPI.deleteCommentReply(replyId)
      ElMessage.success('删除成功')
      loadPostDetail()
    } catch (error) {
      console.error('删除回复失败:', error)
    }
  }).catch(() => {})
}

const handleDelete = async () => {
  ElMessageBox.confirm('确定要删除这篇文章吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await postAPI.deletePost(postId.value)
      ElMessage.success('删除成功')
      router.push('/post/list')
    } catch (error) {
      console.error('删除文章失败:', error)
    }
  }).catch(() => {})
}

const handleDeleteComment = async (commentId) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await commentAPI.deleteComment(commentId)
      ElMessage.success('删除成功')
      loadPostDetail()
    } catch (error) {
      console.error('删除评论失败:', error)
    }
  }).catch(() => {})
}

const goBack = () => {
  router.back()
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  loadPostDetail()
})
</script>

<style scoped>
.post-detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.detail-card,
.comment-card {
  border-radius: 20px;
  border: 1px solid rgba(214, 230, 245, 0.88);
}

.detail-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
}

.post-content {
  padding: 12px 0 8px;
}

.post-kicker {
  margin: 0 0 10px;
  color: #7ea9d0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.post-title {
  margin: 0 0 20px;
  color: #1f2a36;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.35;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  padding: 16px 0;
  border-top: 1px solid rgba(232, 240, 247, 0.9);
  border-bottom: 1px solid rgba(232, 240, 247, 0.9);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2e3f52;
  font-size: 15px;
  font-weight: 600;
}

.owner-name {
  color: #e67e22;
}

.post-time {
  margin-top: 4px;
  color: #8b9cad;
  font-size: 13px;
}

.post-body {
  color: #31404e;
  font-size: 16px;
  line-height: 1.95;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-title {
  font-size: 18px;
  font-weight: 700;
}

.login-tip {
  margin-bottom: 20px;
}

.comment-input {
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item-wrap {
  border-bottom: 1px solid rgba(232, 240, 247, 0.9);
  padding-bottom: 18px;
}

.comment-item-wrap:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.comment-text {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
  line-height: 1.7;
}

.comment-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-footer-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

/* ===== 楼中楼回复区 ===== */
.reply-area {
  margin: 12px 0 0 48px;
  padding: 8px 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(220, 230, 240, 0.8);
}

.reply-item:last-child {
  border-bottom: none;
  padding-bottom: 4px;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-author {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 6px;
}

.reply-author-name {
  color: #333;
  font-size: 13px;
  font-weight: 500;
}

.reply-to {
  color: #5f9fe0;
  font-size: 13px;
}

.reply-text {
  margin-bottom: 6px;
  color: #666;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.reply-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ===== 就地回复输入框 ===== */
.reply-input {
  margin: 12px 0 0 48px;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .header-actions,
  .post-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .post-title {
    font-size: 26px;
  }

  .reply-area,
  .reply-input {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .post-title {
    font-size: 22px;
  }

  .comment-item {
    align-items: flex-start;
  }

  .comment-footer,
  .reply-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
