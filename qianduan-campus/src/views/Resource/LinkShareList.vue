<template>
  <div class="link-page">
    <el-card class="link-toolbar" shadow="never">
      <div class="section-heading">
        <div>
          <h3>链接分享</h3>
        </div>
        <el-tag type="warning" effect="plain" round>当前为资源二级栏目</el-tag>
      </div>

      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            clearable
            placeholder="搜索标题、简介、来源或标签"
            class="keyword-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="searchForm.sourceName"
            clearable
            placeholder="来源名称，如 GitHub / Bilibili"
            class="source-input"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item v-if="isAdmin" class="admin-toolbar-item">
          <el-button type="success" :icon="Plus" @click="openCreateDialog">新增链接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading" class="link-list">
      <el-empty v-if="!loading && projectList.length === 0" description="暂时还没有可分享的链接" />

      <el-card v-for="project in projectList" :key="project.id" class="link-card" shadow="hover">
        <div class="link-card-inner">
          <div class="link-info">
            <div class="link-heading">
              <div class="link-title-wrap">
                <h3>{{ project.title }}</h3>
                <p class="link-source">
                  {{ getDisplaySource(project) }}
                  <span v-if="getLinkKind(project.linkUrl) !== '通用链接'">· {{ getLinkKind(project.linkUrl) }}</span>
                </p>
              </div>
              <el-tag effect="plain" round size="small">{{ getLinkDomain(project.linkUrl) }}</el-tag>
            </div>

            <a
              v-if="isValidLinkUrl(project.linkUrl)"
              class="link-url"
              :href="project.linkUrl"
              target="_blank"
              rel="noopener noreferrer"
            >
              {{ project.linkUrl }}
            </a>

            <div class="link-meta">
              <span>{{ project.sourceName || '未标注来源' }}</span>
              <span>排序值 {{ Number(project.sort || 0) }}</span>
              <span>更新于 {{ project.updateTime || project.createTime || '未知时间' }}</span>
            </div>
          </div>

          <div class="link-side-actions">
            <el-button text type="primary" @click="openDetail(project)">
              <span class="action-icon-placeholder"></span>
              <span>详情</span>
            </el-button>
            <el-button v-if="isValidLinkUrl(project.linkUrl)" text type="primary" @click="openLink(project.linkUrl)">
              <el-icon class="action-icon"><Link /></el-icon>
              <span>打开链接</span>
            </el-button>
            <template v-if="isAdmin">
              <el-button text @click="openEditDialog(project)">
                <el-icon class="action-icon"><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button text class="status-action is-static">
                <span class="status-dot" :class="project.status === 1 ? 'is-enabled' : 'is-disabled'"></span>
                <span>{{ project.status === 1 ? '已启用' : '已禁用' }}</span>
              </el-button>
              <el-button text type="danger" @click="handleDelete(project)">
                <el-icon class="action-icon"><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </template>
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
      @current-change="loadProjects"
      @size-change="loadProjects"
    />

    <el-dialog v-model="detailVisible" class="link-detail-dialog" title="链接详情" width="560px" destroy-on-close>
      <div v-if="activeProject" class="detail-panel">
        <div class="detail-title-row">
          <div>
            <div class="eyebrow">LINK DETAIL</div>
            <h3>{{ activeProject.title }}</h3>
          </div>
        </div>
        <div class="detail-description-scroll">
          <p class="detail-description">{{ activeProject.description || '暂无简介' }}</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="formVisible" class="link-form-dialog" :title="formMode === 'create' ? '新增链接' : '编辑链接'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="projectForm" :rules="formRules" label-width="100px">
        <el-form-item label="链接标题" prop="title">
          <el-input v-model="projectForm.title" maxlength="255" show-word-limit placeholder="请输入展示标题" />
        </el-form-item>
        <el-form-item label="链接地址" prop="linkUrl">
          <el-input v-model="projectForm.linkUrl" placeholder="https://example.com/article-or-video" />
        </el-form-item>
        <el-form-item label="链接说明" prop="description">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="来源名称">
            <el-input v-model="projectForm.sourceName" placeholder="如 GitHub、Bilibili、掘金" />
          </el-form-item>
          <el-form-item label="排序值">
            <el-input-number v-model="projectForm.sort" :min="0" :max="999999" />
          </el-form-item>
        </div>
        <el-form-item label="标签">
          <el-select v-model="projectForm.tags" multiple filterable allow-create default-first-option style="width: 100%" placeholder="输入标签后回车">
            <el-option v-for="tag in projectForm.tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="projectForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存链接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Link, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { linkShareAPI } from '@/api/linkShare'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => Number(userStore.userInfo?.role) === 1)

const loading = ref(false)
const submitLoading = ref(false)
const projectList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const formVisible = ref(false)
const activeProject = ref(null)
const formRef = ref(null)
const formMode = ref('create')

const searchForm = reactive({ keyword: '', sourceName: '' })
const projectForm = reactive(createEmptyForm())

const formRules = {
  title: [{ required: true, message: '请输入链接标题', trigger: 'blur' }],
  linkUrl: [
    { required: true, message: '请输入链接地址', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!isValidLinkUrl(value)) {
          callback(new Error('请输入有效的 http 或 https 链接'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function createEmptyForm() {
  return {
    id: null,
    title: '',
    description: '',
    linkUrl: '',
    sourceName: '',
    tags: [],
    sort: 0,
    status: 1
  }
}

function normalizeProject(raw = {}) {
  return {
    ...raw,
    linkUrl: raw.linkUrl || raw.projectUrl || '',
    sourceName: raw.sourceName || raw.ownerName || inferSourceFromUrl(raw.linkUrl || raw.projectUrl),
    tags: Array.isArray(raw.tags) ? raw.tags : Array.isArray(raw.topics) ? raw.topics : [],
    sort: Number(raw.sort || 0)
  }
}

function inferSourceFromUrl(value) {
  try {
    const url = new URL(String(value || '').trim())
    const host = url.hostname.toLowerCase().replace(/^www\./, '')
    if (host.includes('github.com')) return 'GitHub'
    if (host.includes('bilibili.com')) return 'Bilibili'
    if (host.includes('youtube.com') || host.includes('youtu.be')) return 'YouTube'
    if (host.includes('juejin.cn')) return '掘金'
    if (host.includes('zhihu.com')) return '知乎'
    const [name] = host.split('.')
    return name ? name.charAt(0).toUpperCase() + name.slice(1) : '未标注来源'
  } catch {
    return '未标注来源'
  }
}

function getLinkKind(value) {
  try {
    const host = new URL(String(value || '').trim()).hostname.toLowerCase()
    if (host.includes('bilibili.com') || host.includes('youtube.com') || host.includes('youtu.be')) {
      return '视频链接'
    }
    if (host.includes('github.com') || host.includes('gitlab.com') || host.includes('gitee.com')) {
      return '代码仓库'
    }
    return '网站链接'
  } catch {
    return '通用链接'
  }
}

function getLinkDomain(value) {
  try {
    return new URL(String(value || '').trim()).hostname.replace(/^www\./, '')
  } catch {
    return '未识别域名'
  }
}

function getDisplaySource(project) {
  return project.sourceName || inferSourceFromUrl(project.linkUrl)
}

const isValidLinkUrl = value => {
  try {
    const url = new URL(String(value || '').trim())
    return ['http:', 'https:'].includes(url.protocol)
  } catch {
    return false
  }
}

const loadProjects = async () => {
  loading.value = true
  try {
    const data = await linkShareAPI.getLinkList({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      sourceName: searchForm.sourceName || undefined
    })
    projectList.value = (data.records || []).map(normalizeProject)
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadProjects()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.sourceName = ''
  currentPage.value = 1
  loadProjects()
}

const openLink = url => {
  if (isValidLinkUrl(url)) {
    window.open(url, '_blank', 'noopener,noreferrer')
  }
}

const openDetail = async project => {
  activeProject.value = project
  detailVisible.value = true
  try {
    activeProject.value = normalizeProject(await linkShareAPI.getLinkDetail(project.id))
  } catch {
    activeProject.value = project
  }
}

const openCreateDialog = () => {
  formMode.value = 'create'
  Object.assign(projectForm, createEmptyForm())
  formVisible.value = true
}

const openEditDialog = project => {
  formMode.value = 'edit'
  Object.assign(projectForm, createEmptyForm(), normalizeProject(project), { tags: [...(project.tags || [])] })
  formVisible.value = true
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
      title: projectForm.title,
      description: projectForm.description,
      linkUrl: projectForm.linkUrl,
      sourceName: projectForm.sourceName || inferSourceFromUrl(projectForm.linkUrl),
      tags: [...projectForm.tags],
      sort: Number(projectForm.sort || 0),
      status: projectForm.status
    }
    if (formMode.value === 'create') {
      await linkShareAPI.createLink(payload)
      ElMessage.success('链接新增成功')
    } else {
      await linkShareAPI.updateLink(projectForm.id, payload)
      ElMessage.success('链接更新成功')
    }
    formVisible.value = false
    await loadProjects()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async project => {
  await ElMessageBox.confirm(`确定要删除链接“${project.title}”吗？`, '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await linkShareAPI.deleteLink(project.id)
  ElMessage.success('链接删除成功')
  await loadProjects()
}

onMounted(loadProjects)
</script>

<style scoped>
.link-page {
  max-width: 1200px;
  margin: 0 auto;
}

.link-toolbar {
  margin-bottom: 20px;
  border-radius: 18px;
}

.section-heading,
.link-heading,
.detail-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.section-heading {
  margin-bottom: 22px;
}

.eyebrow {
  margin-bottom: 8px;
  color: #77a4d1;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.section-heading h3,
.detail-title-row h3 {
  margin: 0 0 8px;
  color: #263b53;
  font-size: 22px;
}

.section-heading p,
.detail-description {
  color: #8296ab;
  font-size: 13px;
  line-height: 1.75;
}

.keyword-input {
  width: 330px;
}

.source-input {
  width: 260px;
}

.admin-toolbar-item {
  margin-left: auto;
}

.link-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  align-items: start;
}

.link-card {
  border: 1px solid #e4edf6;
  border-radius: 18px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.link-card:hover {
  transform: translateY(-2px);
  border-color: #cadbed;
  box-shadow: 0 12px 26px rgba(77, 132, 183, 0.12);
}

.link-card :deep(.el-card__body) {
  padding: 0;
}

.link-card-inner {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 98px;
  min-height: 188px;
}

.link-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 18px 18px 16px;
}

.link-heading {
  gap: 10px;
}

.link-title-wrap {
  min-width: 0;
}

.link-title-wrap h3 {
  margin: 0 0 6px;
  overflow: hidden;
  color: #223a52;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-source {
  margin: 0;
  color: #86a0b9;
  font-size: 12px;
  line-height: 1.5;
}

.link-url {
  overflow: hidden;
  color: #d89a12;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.75;
  text-decoration: none;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-url:hover {
  color: #ad7200;
  text-decoration: underline;
}

.link-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
  margin-top: auto;
  padding-top: 12px;
  color: #8ba0b4;
  font-size: 11px;
}

.link-side-actions {
  display: grid;
  grid-auto-rows: 1fr;
  align-items: stretch;
  padding: 14px 12px;
  border-left: 1px solid #edf3f9;
  background: linear-gradient(180deg, rgba(245, 249, 255, 0.95), rgba(250, 252, 255, 0.98));
}

.link-side-actions :deep(.el-button) {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  align-items: center;
  column-gap: 8px;
  justify-content: flex-start;
  width: 100%;
  margin-left: 0;
  padding: 6px 8px;
  font-size: 13px;
}

.status-action {
  display: grid;
}

.link-side-actions :deep(.el-button.status-action.is-static) {
  cursor: default;
}

.link-side-actions :deep(.el-button.status-action.is-static:hover),
.link-side-actions :deep(.el-button.status-action.is-static:focus-visible) {
  background: transparent;
  box-shadow: none;
}

.action-icon,
.action-icon-placeholder,
.status-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  height: 14px;
}

.status-dot {
  position: relative;
  flex: 0 0 auto;
}

.status-dot::after {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  content: '';
}

.status-dot.is-enabled {
  color: #56b948;
}

.status-dot.is-disabled {
  color: #f05a5a;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 26px;
}

.detail-panel {
  display: grid;
  gap: 16px;
  padding: 6px 4px 2px;
}

.detail-description-scroll {
  max-height: 220px;
  padding: 4px;
  overflow-y: auto;
  border: 1px solid #ebf1f7;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(247, 251, 255, 0.98));
  scrollbar-width: thin;
  scrollbar-color: #c7d7e8 transparent;
}

.detail-description-scroll::-webkit-scrollbar {
  width: 8px;
}

.detail-description-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.detail-description-scroll::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #c7d7e8;
}

.detail-description {
  margin: 0;
  padding: 12px 14px;
  color: #5e7489;
  font-size: 14px;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-title-row {
  align-items: end;
}

.detail-title-row h3 {
  margin-bottom: 0;
  font-size: 20px;
  line-height: 1.35;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
}

@media (max-width: 1100px) {
  .link-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .section-heading,
  .link-heading,
  .detail-title-row {
    flex-direction: column;
  }

  .keyword-input,
  .source-input {
    width: 100%;
  }

  .admin-toolbar-item {
    margin-left: 0;
  }

  .link-list {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .link-card-inner {
    grid-template-columns: 1fr;
  }

  .link-side-actions {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    gap: 6px;
    border-left: 0;
    border-top: 1px solid #edf3f9;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .section-heading {
    gap: 10px;
    margin-bottom: 16px;
  }

  /* 搜索/操作区纵向堆叠 */
  .link-toolbar :deep(.el-form--inline .el-form-item) {
    display: block;
    width: 100%;
    margin-right: 0;
    margin-bottom: 12px;
  }

  .link-toolbar :deep(.el-form-item .el-button) {
    width: 48%;
  }

  .link-toolbar :deep(.el-form-item .el-button + .el-button) {
    margin-left: 4%;
  }

  .link-toolbar :deep(.el-form-item.admin-toolbar-item .el-button) {
    width: 100%;
  }

  .link-title-wrap h3 {
    font-size: 16px;
  }

  .link-side-actions :deep(.el-button) {
    flex: 1 1 calc(50% - 6px);
  }

  .form-grid :deep(.el-input-number) {
    width: 100%;
  }

  /* 分页精简 */
  .pagination {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .pagination :deep(.el-pagination__sizes),
  .pagination :deep(.el-pagination__jump) {
    display: none;
  }

  /* 详情/表单对话框小屏全宽 */
  .link-detail-dialog :deep(.el-dialog),
  .link-form-dialog :deep(.el-dialog) {
    width: calc(100vw - 28px) !important;
    margin-top: 6vh !important;
  }
}
</style>
