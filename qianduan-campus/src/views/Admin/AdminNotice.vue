<template>
  <div class="notice-admin-manager">
    <div class="manager-toolbar">
      <div class="manager-copy">
        <p class="manager-kicker">Notice Admin</p>
        <h3 class="manager-title">公告管理</h3>
        <p class="manager-desc">
          维护公告板内容。新增或编辑公告后，用户进入首页会自动看到最新公告（不受"今日关闭"限制）。启用状态下才会展示给用户。
        </p>
      </div>
      <div class="manager-actions">
        <el-button @click="loadList">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增公告</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item class="filter-item-keyword">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="搜索公告标题"
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item class="filter-item-status">
          <el-select v-model="queryForm.status" clearable placeholder="状态" style="width: 120px">
            <el-option label="全部状态" value="" />
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="tableLoading" :data="tableData" stripe>
      <el-table-column prop="id" label="ID" min-width="64" />
      <el-table-column label="标题" min-width="200">
        <template #default="{ row }">
          <div class="notice-title-cell">{{ row.title || '（无标题）' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="内容预览" min-width="280">
        <template #default="{ row }">
          <div class="notice-content-cell">{{ row.content || '（无内容）' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="84">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.status === 1 ? 'success' : 'info'"
            :effect="row.status === 1 ? 'light' : 'plain'"
          >
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="150" />
      <el-table-column prop="updateTime" label="更新时间" min-width="150" />
      <el-table-column label="操作" fixed="right" min-width="168">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button size="small" text bg type="primary" @click="openEditDialog(row.id)">
              编辑
            </el-button>
            <el-button size="small" text bg :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" text bg type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!tableLoading && !tableData.length" description="暂无公告数据" />

    <el-pagination
      v-if="pagination.total > 0"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      class="pagination"
      :page-sizes="[10, 20, 50]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="loadList"
      @size-change="handleSizeChange"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增公告' : '编辑公告'"
      width="620px"
      destroy-on-close
      @closed="resetDialogState"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="72px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="formModel.title"
            maxlength="100"
            show-word-limit
            placeholder="公告标题"
          />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formModel.content"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="公告内容，支持换行，将按原文展示给用户"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
          <span class="status-hint">仅启用状态的公告会展示在公告板</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          {{ dialogMode === 'create' ? '保存公告' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { noticeAPI } from '@/api/notice'

const tableLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const formRef = ref(null)

const tableData = ref([])
const editingId = ref(null)

const queryForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formModel = reactive({
  title: '',
  content: '',
  status: 1
})

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const resetForm = () => {
  formModel.title = ''
  formModel.content = ''
  formModel.status = 1
  formRef.value?.clearValidate?.()
}

const resetDialogState = () => {
  editingId.value = null
  resetForm()
}

const fillForm = detail => {
  formModel.title = detail.title || ''
  formModel.content = detail.content || ''
  formModel.status = detail.status ?? 1
}

const buildQueryParams = () => {
  const params = {
    page: pagination.page,
    size: pagination.size
  }

  if (queryForm.keyword) {
    params.keyword = queryForm.keyword
  }
  if (queryForm.status !== '') {
    params.status = queryForm.status
  }

  return params
}

const loadList = async () => {
  tableLoading.value = true
  try {
    const data = await noticeAPI.getAdminNoticeList(buildQueryParams())
    tableData.value = data.records || []
    pagination.total = data.total || 0
    pagination.page = data.page || pagination.page
    pagination.size = data.size || pagination.size
  } catch (error) {
    console.error('加载公告列表失败:', error)
  } finally {
    tableLoading.value = false
  }
}

const fetchDetail = async id => {
  return noticeAPI.getNoticeDetail(id)
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  dialogVisible.value = true
  resetForm()
}

const openEditDialog = async id => {
  dialogMode.value = 'edit'
  dialogVisible.value = true
  submitLoading.value = false
  editingId.value = id

  try {
    const detail = await fetchDetail(id)
    fillForm(detail)
  } catch (error) {
    dialogVisible.value = false
    console.error('加载公告详情失败:', error)
  }
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const payload = {
      title: formModel.title,
      content: formModel.content,
      status: formModel.status
    }

    if (dialogMode.value === 'create') {
      await noticeAPI.addNotice(payload)
      ElMessage.success('新增公告成功')
    } else {
      await noticeAPI.updateNotice(editingId.value, payload)
      ElMessage.success('公告更新成功')
    }

    dialogVisible.value = false
    await loadList()
  } catch (error) {
    console.error('提交公告表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const toggleStatus = async row => {
  const next = row.status === 1 ? 0 : 1
  const actionText = next === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(
      `确定${actionText}公告“${row.title}”吗？${next === 1 ? '' : '停用后用户将不再看到该公告。'}`,
      `${actionText}确认`,
      {
        confirmButtonText: actionText,
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await noticeAPI.updateNotice(row.id, { status: next })
    ElMessage.success(`公告已${actionText}`)
    await loadList()
  } catch (error) {
    console.error('更新公告状态失败:', error)
  }
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(
      `确定删除公告“${row.title}”吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await noticeAPI.deleteNotice(row.id)
    ElMessage.success('删除公告成功')

    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }

    await loadList()
  } catch (error) {
    console.error('删除公告失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  pagination.page = 1
  loadList()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadList()
}

loadList()
</script>

<style scoped>
.notice-admin-manager {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.manager-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.manager-copy {
  min-width: 0;
}

.manager-kicker {
  margin: 0 0 8px;
  color: #bd8b1a;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.manager-title {
  margin: 0 0 6px;
  color: #203142;
  font-size: 24px;
}

.manager-desc {
  margin: 0;
  color: #75889b;
  font-size: 13px;
  line-height: 1.7;
}

.manager-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.filter-card {
  border-radius: 18px;
  border: 1px solid rgba(229, 237, 247, 0.92);
}

:deep(.el-table .cell) {
  padding-top: 6px;
  padding-bottom: 6px;
}

:deep(.el-table th.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
}

:deep(.el-table td.el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
}

.notice-title-cell {
  color: #26384a;
  font-size: 13px;
  font-weight: 700;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-content-cell {
  color: #70859b;
  font-size: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

:deep(.row-actions .el-button) {
  margin-left: 0;
  min-width: 0;
  padding: 4px 8px;
  border-radius: 999px;
}

.status-hint {
  margin-left: 14px;
  color: #9aabba;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}

@media (max-width: 900px) {
  .manager-toolbar {
    flex-direction: column;
  }

  .manager-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .manager-actions :deep(.el-button) {
    flex: 1 1 160px;
  }
}
</style>
