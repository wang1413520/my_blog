<template>
  <div class="featured-admin-manager">
    <div class="manager-toolbar">
      <div class="manager-copy">
        <p class="manager-kicker">Featured Resource Admin</p>
        <h3 class="manager-title">站长主推管理</h3>
        <p class="manager-desc">
          在后台直接维护主推资源的排序、状态、封面和说明，前台主推页会同步使用这里的配置。        </p>
      </div>
      <div class="manager-actions">
        <el-button @click="loadList">刷新列表</el-button>
        <el-button type="primary" @click="openCreateDialog">新增主推</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item class="filter-item-keyword">
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="搜索主推标题、说明或资源标题"
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item class="filter-item-status">
          <el-select
            v-model="queryForm.status"
            clearable
            placeholder="状态"
            style="width: 120px"
          >
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
      <el-table-column prop="id" label="ID" min-width="72" />
      <el-table-column prop="resourceId" label="资源ID" min-width="82" />
      <el-table-column label="主推信息" min-width="260">
        <template #default="{ row }">
          <div class="resource-cell">
            <div class="resource-cover-wrap">
              <img :src="getDisplayCover(row)" alt="cover" class="resource-cover" />
            </div>
            <div class="resource-copy">
              <p class="resource-title">{{ row.title || '未命名主题' }}</p>
              <p class="resource-desc">{{ row.description || '暂无主推说明' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="fileType" label="类型" min-width="72">
        <template #default="{ row }">
          {{ formatFileType(row.fileType) }}
        </template>
      </el-table-column>
      <el-table-column prop="downloadCount" label="下载量" min-width="82" />
      <el-table-column prop="sort" label="排序" min-width="72" />
      <el-table-column label="状态" min-width="86">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.status === 1 ? 'success' : 'danger'"
            :effect="row.status === 1 ? 'light' : 'dark'"
          >
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploaderName" label="上传者" min-width="92" />
      <el-table-column prop="createTime" label="创建时间" min-width="132" />
      <el-table-column label="操作" fixed="right" min-width="156">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button size="small" text bg type="primary" @click="openDetailDrawer(row.id)">
              查看
            </el-button>
            <el-button size="small" text bg type="primary" @click="openEditDialog(row.id)">
              编辑
            </el-button>
            <el-button size="small" text bg type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!tableLoading && !tableData.length" description="暂无站长主推数据" />

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
      :title="dialogMode === 'create' ? '新增站长主推' : '编辑站长主推'"
      width="680px"
      destroy-on-close
      @closed="resetDialogState"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <el-form-item label="选择资源" prop="resourceId">
          <el-select
            v-model="formModel.resourceId"
            filterable
            remote
            reserve-keyword
            clearable
            :disabled="dialogMode === 'edit'"
            :remote-method="loadResourceOptions"
            :loading="resourceOptionsLoading"
            placeholder="输入资源标题搜索并选择"
            style="width: 100%"
          >
            <el-option
              v-for="item in resourceOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <div class="option-row">
                <span class="option-title">{{ item.title }}</span>
                <span class="option-meta">
                  {{ formatFileType(item.fileType) }} · {{ formatFileSize(item.fileSize || 0) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="主推标题" prop="featuredTitle">
          <el-input
            v-model="formModel.featuredTitle"
            maxlength="255"
            show-word-limit
            placeholder="可为空，留空时由后端回退为资源标题"
          />
        </el-form-item>

        <el-form-item label="主推说明" prop="featuredDesc">
          <el-input
            v-model="formModel.featuredDesc"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="补充这份资源为什么值得推荐"
          />
        </el-form-item>

        <el-form-item label="封面地址" prop="coverUrl">
          <el-input
            v-model="formModel.coverUrl"
            placeholder="可选，填写主推卡片封面 URL"
          />
        </el-form-item>

        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="formModel.sort" :min="0" :max="9999" style="width: 180px" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          {{ dialogMode === 'create' ? '保存主推' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailDrawerVisible"
      title="主推详情"
      size="460px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-panel">
        <template v-if="detailData">
          <div class="detail-cover-block">
            <img :src="getDisplayCover(detailData)" alt="cover" class="detail-cover" />
          </div>

          <div class="detail-list">
            <div class="detail-item">
              <span class="detail-label">主推ID</span>
              <span class="detail-value">{{ detailData.id }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">资源ID</span>
              <span class="detail-value">{{ detailData.resourceId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">标题</span>
              <span class="detail-value">{{ detailData.title || '暂无标题' }}</span>
            </div>
            <div class="detail-item detail-item-block">
              <span class="detail-label">说明</span>
              <span class="detail-value detail-text">{{ detailData.description || '暂无说明' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">文件类型</span>
              <span class="detail-value">{{ formatFileType(detailData.fileType) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">文件大小</span>
              <span class="detail-value">{{ formatFileSize(detailData.fileSize || 0) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">下载量</span>
              <span class="detail-value">{{ detailData.downloadCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">上传者</span>
              <span class="detail-value">{{ detailData.uploaderName || '未知' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">排序</span>
              <span class="detail-value">{{ detailData.sort ?? 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <span class="detail-value">{{ detailData.status === 1 ? '启用' : '停用' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ detailData.createTime || '-' }}</span>
            </div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { featuredResourceAPI } from '@/api/featuredResource'
import { resourceAPI } from '@/api/resource'
import { formatFileSize } from '@/utils/format'
import pdfCover from '@/assets/resource-covers/pdf.svg'
import docCover from '@/assets/resource-covers/doc.svg'
import docxCover from '@/assets/resource-covers/docx.svg'
import genericCover from '@/assets/resource-covers/generic.svg'

const tableLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const detailDrawerVisible = ref(false)
const detailLoading = ref(false)
const resourceOptionsLoading = ref(false)
const formRef = ref(null)

const tableData = ref([])
const resourceOptions = ref([])
const detailData = ref(null)
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
  resourceId: null,
  featuredTitle: '',
  featuredDesc: '',
  coverUrl: '',
  sort: 0,
  status: 1
})

const formRules = {
  resourceId: [
    { required: true, message: '请选择要加入主推的资源', trigger: 'change' }
  ]
}

const coverMap = {
  pdf: pdfCover,
  doc: docCover,
  docx: docxCover
}

const formatFileType = fileType => {
  const normalized = String(fileType || '').trim().toUpperCase()
  return normalized || 'FILE'
}

const getDisplayCover = item => {
  const customCover = String(item?.coverUrl || '').trim()
  if (customCover) {
    return customCover
  }

  const normalizedType = String(item?.fileType || '').trim().toLowerCase()
  return coverMap[normalizedType] || genericCover
}

const resetForm = () => {
  formModel.resourceId = null
  formModel.featuredTitle = ''
  formModel.featuredDesc = ''
  formModel.coverUrl = ''
  formModel.sort = 0
  formModel.status = 1
  formRef.value?.clearValidate?.()
}

const resetDialogState = () => {
  editingId.value = null
  resourceOptions.value = []
  resetForm()
}

const fillForm = detail => {
  formModel.resourceId = detail.resourceId ?? null
  formModel.featuredTitle = detail.title || ''
  formModel.featuredDesc = detail.description || ''
  formModel.coverUrl = detail.coverUrl || ''
  formModel.sort = detail.sort ?? 0
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
    const data = await featuredResourceAPI.getAdminFeaturedResourceList(buildQueryParams())
    tableData.value = data.records || []
    pagination.total = data.total || 0
    pagination.page = data.page || pagination.page
    pagination.size = data.size || pagination.size
  } catch (error) {
    console.error('加载主推管理列表失败:', error)
  } finally {
    tableLoading.value = false
  }
}

const loadResourceOptions = async (keyword = '') => {
  resourceOptionsLoading.value = true
  try {
    const data = await resourceAPI.getResourceList({
      page: 1,
      size: 20,
      keyword: keyword || undefined
    })
    resourceOptions.value = data.records || []
  } catch (error) {
    console.error('加载可选资源失败', error)
  } finally {
    resourceOptionsLoading.value = false
  }
}

const fetchDetail = async id => {
  return featuredResourceAPI.getFeaturedResourceDetail(id)
}

const openCreateDialog = async () => {
  dialogMode.value = 'create'
  dialogVisible.value = true
  resetForm()
  await loadResourceOptions('')
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
    console.error('加载主推详情失败:', error)
  }
}

const openDetailDrawer = async id => {
  detailDrawerVisible.value = true
  detailLoading.value = true
  detailData.value = null

  try {
    detailData.value = await fetchDetail(id)
  } catch (error) {
    detailDrawerVisible.value = false
    console.error('加载主推详情失败:', error)
  } finally {
    detailLoading.value = false
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
      featuredTitle: formModel.featuredTitle || undefined,
      featuredDesc: formModel.featuredDesc || undefined,
      coverUrl: formModel.coverUrl || undefined,
      sort: formModel.sort ?? 0,
      status: formModel.status
    }

    if (dialogMode.value === 'create') {
      await featuredResourceAPI.addFeaturedResource({
        resourceId: formModel.resourceId,
        ...payload
      })
      ElMessage.success('新增主推成功')
    } else {
      await featuredResourceAPI.updateFeaturedResource(editingId.value, payload)
      ElMessage.success('主推更新成功')
    }

    dialogVisible.value = false
    await loadList()
  } catch (error) {
    console.error('提交主推表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async row => {
  try {
    await ElMessageBox.confirm(
      `确定删除主推“${row.title || row.resourceId}”吗？`,
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
    await featuredResourceAPI.deleteFeaturedResource(row.id)
    ElMessage.success('删除主推成功')

    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }

    await loadList()
  } catch (error) {
    console.error('删除主推失败:', error)
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
.featured-admin-manager {
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

.resource-cell {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.resource-cover-wrap {
  width: 50px;
  height: 66px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(180deg, #fff7de 0%, #f5dfab 100%);
  box-shadow: inset 0 0 0 1px rgba(212, 180, 102, 0.22);
}

.resource-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.resource-title {
  margin: 0 0 4px;
  color: #26384a;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-desc {
  margin: 0;
  color: #70859b;
  font-size: 11px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}

.option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.option-title {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.option-meta {
  color: #8899ab;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-cover-block {
  display: flex;
  justify-content: center;
}

.detail-cover {
  width: 180px;
  height: 240px;
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #fff7de 0%, #f5dfab 100%);
  box-shadow: inset 0 0 0 1px rgba(212, 180, 102, 0.22);
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef3f8;
}

.detail-item-block {
  align-items: flex-start;
}

.detail-label {
  color: #7d91a7;
  font-size: 13px;
  flex-shrink: 0;
}

.detail-value {
  color: #243446;
  font-size: 14px;
  text-align: right;
}

.detail-text {
  text-align: left;
  white-space: pre-wrap;
  line-height: 1.7;
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

@media (max-width: 768px) {
  .resource-cell {
    align-items: flex-start;
  }

  .detail-item {
    flex-direction: column;
  }

  .detail-value {
    text-align: left;
  }
}
</style>

