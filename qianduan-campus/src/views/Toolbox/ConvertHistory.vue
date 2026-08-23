<template>
  <div class="tool-page">
    <el-card class="tool-shell" shadow="hover">
      <div class="tool-header">
        <div>
          <h1 class="tool-title">转换历史</h1>
          <p class="tool-desc">登录后可以查看自己的后端转换记录，并清理不再需要的结果。</p>
        </div>
      </div>

      <div v-loading="loading" class="history-list">
        <el-empty v-if="historyList.length === 0 && !loading" description="暂无转换记录" />
        <el-card v-for="item in historyList" :key="item.id" class="history-card" shadow="hover">
          <div class="history-main">
            <div>
              <h3 class="history-title">{{ item.sourceName }}</h3>
              <p class="history-meta">{{ item.sourceType }} → {{ item.targetType }} · {{ item.createdAt }}</p>
            </div>
            <div class="history-actions">
              <el-tag :type="item.status === 1 ? 'success' : item.status === 2 ? 'danger' : 'warning'">
                {{ item.status === 1 ? '成功' : item.status === 2 ? '失败' : '转换中' }}
              </el-tag>
              <el-button v-if="item.resultUrl" type="primary" @click="openResult(item.resultUrl)">打开结果</el-button>
              <el-button type="danger" plain @click="removeRecord(item.id)">删除</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="size"
        class="pagination"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadHistory"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toolboxAPI } from '@/api/toolbox'

const loading = ref(false)
const historyList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const loadHistory = async () => {
  loading.value = true
  try {
    const data = await toolboxAPI.getConvertHistory({
      page: page.value,
      size: size.value
    })
    historyList.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const openResult = (url) => {
  window.open(url, '_blank')
}

const removeRecord = async (id) => {
  await ElMessageBox.confirm('确定删除这条转换记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })

  await toolboxAPI.deleteConvertRecord(id)
  ElMessage.success('删除成功')
  loadHistory()
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
@import '@/views/Toolbox/toolbox.css';

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 220px;
}

.history-card {
  border-radius: 22px;
  border: 1px solid rgba(210, 230, 246, 0.86);
}

.history-main {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.history-title {
  margin: 0 0 8px;
  color: #1f2a36;
  font-size: 17px;
  font-weight: 700;
}

.history-meta {
  margin: 0;
  color: #68788b;
  font-size: 14px;
}

.history-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 900px) {
  .history-main {
    flex-direction: column;
    align-items: flex-start;
  }

  .history-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .history-title {
    font-size: 15px;
  }

  .history-actions {
    width: 100%;
  }

  .history-actions :deep(.el-button) {
    flex: 1 1 auto;
  }

  .pagination {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
