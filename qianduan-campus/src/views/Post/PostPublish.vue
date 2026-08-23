<template>
  <div class="publish-container">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">写文章</span>
          <el-button @click="goBack" icon="ArrowLeft">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="top"
      >
        <el-form-item label="文章标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入文章标题（不超过 100 字）"
            maxlength="100"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="文章内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入文章内容（不超过 2000 字）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="loading"
            size="large"
            style="width: 200px"
          >
            发布文章
          </el-button>
          <el-button @click="handleReset" size="large">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="tips-card" shadow="never">
      <template #header>
        <span class="tips-title">写作提示</span>
      </template>
      <ul class="tips-list">
        <li>先把想表达的核心观点写清楚，再补充细节。</li>
        <li>标题尽量简洁，方便后续自己回看和搜索。</li>
        <li>首版先用纯文本发布，等博客稳定后再升级 Markdown。</li>
        <li>如果这篇文章只是临时记录，也可以先短一些，后续再完善。</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { postAPI } from '@/api/post'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入文章内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await postAPI.publishPost({
          title: form.title,
          content: form.content,
          type: 1,
          isAnonymous: 0
        })
        ElMessage.success('文章发布成功')
        router.push('/post/list')
      } catch (error) {
        console.error('发布文章失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  formRef.value.resetFields()
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.publish-container {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--gap-lg);
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: var(--gap-lg);
}

/* ===== 卡片通用 ===== */
.publish-card,
.tips-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
}

.publish-card :deep(.el-card__header),
.tips-card :deep(.el-card__header) {
  border-bottom: 1px solid var(--border-color);
}

/* ===== 卡片头部 ===== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-title);
}

/* ===== 表单通用 ===== */
.publish-card :deep(.el-form-item__label) {
  color: var(--color-body);
  font-weight: 600;
}

.publish-card :deep(.el-input__wrapper),
.publish-card :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
}

/* ===== 发布按钮 ===== */
.submit-btn {
  width: 200px;
}

/* ===== 提示卡片 ===== */
.tips-card {
  height: fit-content;
}

.tips-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-title);
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  line-height: 2;
  color: var(--color-muted);
  font-size: 14px;
}

.tips-list li {
  margin-bottom: 8px;
}

/* ===== 移动端适配：平板 ===== */
@media (max-width: 768px) {
  .publish-container {
    grid-template-columns: 1fr;
    padding: var(--gap-md);
    gap: var(--gap-md);
  }

  .publish-card,
  .tips-card {
    border-radius: var(--radius-lg);
  }

  .card-title {
    font-size: 18px;
  }

  .publish-card :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

/* ===== 移动端适配：手机 ===== */
@media (max-width: 480px) {
  .publish-container {
    padding: var(--gap-sm);
    gap: var(--gap-sm);
  }

  .publish-card,
  .tips-card {
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-sm);
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--gap-xs);
  }

  .card-title {
    font-size: 16px;
  }

  /* textarea 在小屏幕减少高度 */
  .publish-card :deep(.el-textarea__inner) {
    min-height: 140px !important;
  }

  /* 按钮组全宽 */
  .publish-card :deep(.el-form-item:last-child .el-form-item__content) {
    display: flex;
    flex-direction: column;
    gap: var(--gap-sm);
  }

  .publish-card :deep(.el-form-item:last-child .el-form-item__content .el-button) {
    width: 100% !important;
    margin-left: 0;
  }

  .tips-title {
    font-size: 15px;
  }

  .tips-list {
    font-size: 13px;
    line-height: 1.8;
    padding-left: 16px;
  }
}
</style>
