<template>
  <div class="resource-container">
    <el-card class="action-card" shadow="never">
      <div class="section-summary">
        <div>
          <h3 class="section-summary-title">{{ currentSectionMeta.label }}</h3>
        </div>
        <el-tag v-if="currentSection !== 'all'" type="warning" effect="plain" round>
          当前为资源二级栏目        </el-tag>
      </div>

      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索资料名称"
            clearable
            style="width: 300px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.fileType" placeholder="文件类型" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="pdf" value="pdf" />
            <el-option label="doc" value="doc" />
            <el-option label="docx" value="docx" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="Search">搜索</el-button>
          <el-button @click="handleReset" icon="Refresh">重置</el-button>
        </el-form-item>
        <el-form-item v-if="currentSection === 'featured'" style="margin-left: auto">
          <el-button
            v-if="isAdmin"
            type="warning"
            @click="openFeaturedDialog"
            :icon="Upload"
          >
            添加主推
          </el-button>
        </el-form-item>
        <el-form-item v-if="currentSection === 'all'" style="margin-left: auto">
          <div v-if="userStore.isLogin()" class="upload-entry-group">
            <el-button type="success" @click="showUploadDialog" :icon="Upload">
              上传资料
            </el-button>
            <el-button type="warning" @click="openFolderSelector" :icon="FolderOpened">
              上传文件夹            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <input
      ref="folderInputRef"
      class="folder-input"
      type="file"
      webkitdirectory
      multiple
      @change="handleFolderSelect"
    />

    <div v-loading="loading" class="resource-list">
      <el-empty
        v-if="pagedDisplayResourceList.length === 0 && !loading"
        :description="currentSectionMeta.emptyText"
      />
      <div
        v-for="(column, columnIndex) in resourceColumns"
        :key="columnIndex"
        class="resource-column"
      >
      <div
        v-for="item in column"
        :key="item.key"
        class="resource-row"
        :class="{ 'featured-resource-row': currentSection === 'featured' }"
      >
        <img
          class="resource-row-cover"
          :src="item.displayType === 'folder' ? folderCover : getResourceCover(item.fileType)"
          :alt="`${item.displayType === 'folder' ? '文件夹' : getDisplayFileType(item.fileType)} 封面`"
        />

        <div class="resource-row-main">
          <h4 class="resource-row-title">
            {{ item.displayType === 'folder' ? item.folderName : item.title }}
          </h4>
          <p class="resource-row-desc">{{ getResourceDescription(item) }}</p>

          <div class="resource-row-meta">
            <template v-if="item.displayType === 'folder'">
              <el-tag type="warning" effect="plain" size="small">文件夹上传</el-tag>
              <span class="meta-item">📁 {{ item.folderName }}</span>
              <span class="meta-item">{{ item.fileCount }} 个文件</span>
              <span class="meta-item">{{ formatFileSize(item.totalSize) }}</span>
              <span class="meta-item">📥 {{ item.downloadCount }} 次下载</span>
              <span class="meta-item">👤 {{ item.uploaderName }}</span>
            </template>
            <template v-else>
              <el-tag size="small">{{ getDisplayFileType(item.fileType) }}</el-tag>
              <el-tag v-if="item.uploadType === 'folder'" type="warning" effect="plain" size="small">
                文件夹上传
              </el-tag>
              <span class="meta-item">{{ formatFileSize(item.fileSize) }}</span>
              <span class="meta-item">📥 {{ item.downloadCount }} 次下载</span>
              <span class="meta-item">👤 {{ item.uploaderName }}</span>
              <span v-if="item.folderName" class="meta-item">📁 {{ item.folderName }}</span>
              <span v-if="item.relativePath" class="meta-item meta-path" :title="item.relativePath">
                {{ item.relativePath }}
              </span>
            </template>
          </div>

          <div v-if="item.displayType === 'folder'" class="resource-row-paths">
            <span
              v-for="path in item.previewPaths"
              :key="path"
              class="folder-path-chip"
              :title="path"
            >{{ path }}</span>
            <span v-if="item.remainingCount > 0" class="folder-path-chip folder-path-more">
              还有 {{ item.remainingCount }} 个文件
            </span>
          </div>
        </div>

        <div class="resource-row-actions">
          <template v-if="item.displayType === 'folder'">
            <el-button type="primary" link :icon="FolderOpened" @click="openFolderViewer(item)">打开</el-button>
          </template>
          <template v-else>
            <el-button type="primary" link icon="Download" @click="handleDownload(item.downloadId || item.id)">下载</el-button>
            <el-button
              v-if="isUploader(item)"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(item.id)"
            >删除</el-button>
          </template>
        </div>
      </div>
      </div>
    </div>

    <el-pagination
      v-if="displayTotal > 0"
      class="pagination"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="displayTotal"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @current-change="loadResourceList"
      @size-change="loadResourceList"
    />

    <el-dialog
      v-model="uploadDialogVisible"
      class="upload-dialog"
      title="上传资料"
      width="500px"
      :close-on-click-modal="!uploadLoading"
      :close-on-press-escape="!uploadLoading"
      :show-close="!uploadLoading"
    >
      <div class="upload-loading-overlay" v-if="uploadLoading">
        <div class="upload-loading-content">
          <img class="upload-loading-gif" :src="uploadMascot" alt="上传中" />
          <p class="upload-loading-text">
            正在上传请稍后<span class="ellipsis-anim"></span>
          </p>
        </div>
      </div>
      <el-form v-else ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="80px">
        <el-form-item label="资料名称" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入资料名称" />
        </el-form-item>
        <el-form-item label="资料描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资料描述（可选）"
          />
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            accept=".pdf,.doc,.docx"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="upload-tip">支持 PDF、Word 格式，最大 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false" :disabled="uploadLoading">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploadLoading">
          上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="featuredDialogVisible"
      class="featured-dialog"
      title="添加站长主推"
      width="620px"
      destroy-on-close
    >
      <el-form ref="featuredFormRef" :model="featuredForm" :rules="featuredRules" label-width="96px">
        <el-form-item label="选择资源" prop="resourceId">
          <el-select
            v-model="featuredForm.resourceId"
            filterable
            remote
            reserve-keyword
            clearable
            placeholder="输入资源标题搜索并选择"
            :remote-method="loadFeaturedResourceOptions"
            :loading="featuredResourceOptionsLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in featuredResourceOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <div class="featured-option-row">
                <span class="featured-option-title">{{ item.title }}</span>
                <span class="featured-option-meta">
                  {{ item.fileType?.toUpperCase() || 'FILE' }} · {{ formatFileSize(item.fileSize || 0) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主推标题" prop="featuredTitle">
          <el-input
            v-model="featuredForm.featuredTitle"
            maxlength="255"
            show-word-limit
            placeholder="可自定义主推标题，不填时由后端回退到资源标题"
          />
        </el-form-item>
        <el-form-item label="主推说明" prop="featuredDesc">
          <el-input
            v-model="featuredForm.featuredDesc"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="补充这份资源为什么值得推荐"
          />
        </el-form-item>
        <el-form-item label="封面地址" prop="coverUrl">
          <el-input
            v-model="featuredForm.coverUrl"
            placeholder="可选，填写主推卡片封面 URL"
          />
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="featuredForm.sort" :min="0" :max="9999" style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="featuredForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeFeaturedDialog">取消</el-button>
        <el-button type="primary" :loading="featuredSubmitLoading" @click="submitFeaturedResource">
          保存主推
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="folderUploadDialogVisible"
      title="上传资料文件夹"
      width="560px"
      destroy-on-close
      :close-on-click-modal="!folderUploadLoading"
      :close-on-press-escape="!folderUploadLoading"
      :show-close="!folderUploadLoading"
    >
      <div class="upload-loading-overlay" v-if="folderUploadLoading">
        <div class="upload-loading-content">
          <img class="upload-loading-gif" :src="uploadMascot" alt="上传中" />
          <div class="scroll-text-container">
            <p class="upload-loading-text scroll-text-item">
              小黑正在帮你打包上传<span class="ellipsis-anim"></span>
            </p>
            <p class="upload-loading-text scroll-text-item">
              文件夹有点大，耐心等等<span class="ellipsis-anim"></span>
            </p>
            <p class="upload-loading-text scroll-text-item">
              马上就传好啦<span class="ellipsis-anim"></span>
            </p>
          </div>
          <p class="upload-loading-sub-text">文件正在传输中，请稍后</p>
        </div>
      </div>
      <el-form v-else label-width="88px">
        <el-form-item label="文件夹名">
          <el-input :model-value="folderUploadForm.folderName" disabled />
        </el-form-item>
        <el-form-item label="文件数量">
          <div class="folder-upload-summary">
            <span>{{ folderUploadForm.files.length }} 个文件</span>
            <span>{{ formatFileSize(folderUploadForm.totalSize) }}</span>
          </div>
        </el-form-item>
        <el-form-item label="资料描述">
          <el-input
            v-model="folderUploadForm.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入整个文件夹的资料描述（可选）"
          />
        </el-form-item>
        <el-form-item label="文件预览">
          <div class="folder-file-preview">
            <div
              v-for="(path, index) in folderUploadForm.relativePaths.slice(0, 8)"
              :key="`${path}-${index}`"
              class="folder-file-preview-item"
              :title="path"
            >
              {{ path }}
            </div>
            <div
              v-if="folderUploadForm.relativePaths.length > 8"
              class="folder-file-preview-more"
            >
              还有 {{ folderUploadForm.relativePaths.length - 8 }} 个文件未展开
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeFolderUploadDialog" :disabled="folderUploadLoading">取消</el-button>
        <el-button type="warning" @click="submitFolderUpload" :loading="folderUploadLoading">
          上传文件夹        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="folderViewerVisible"
      class="folder-viewer-dialog"
      width="820px"
      destroy-on-close
      :title="activeFolderGroup?.folderName || '文件夹内容'"
    >
      <div v-if="activeFolderGroup" class="folder-viewer">
        <div class="folder-viewer-summary">
          <span>文件夹：{{ activeFolderGroup.folderName }}</span>
          <span>{{ activeFolderGroup.fileCount }} 个文件</span>
          <span>{{ formatFileSize(activeFolderGroup.totalSize) }}</span>
        </div>
        <div class="folder-browser-toolbar">
          <div class="folder-breadcrumbs">
            <el-button text @click="goToFolderRoot">根目录</el-button>
            <template v-for="(segment, index) in folderViewerPathSegments" :key="segment.id">
              <span class="folder-breadcrumb-separator">/</span>
              <el-button text @click="goToFolderLevel(index)">
                {{ segment.label }}
              </el-button>
            </template>
          </div>
          <el-button
            text
            :disabled="folderViewerPathSegments.length === 0"
            @click="goToParentFolder"
          >
            返回上一级          </el-button>
        </div>
        <div v-if="currentFolderEntries.length" class="folder-browser-grid">
          <el-card
            v-for="entry in currentFolderEntries"
            :key="entry.id"
            class="folder-browser-card"
            shadow="hover"
            @click="entry.nodeType === 'folder' && enterFolderNode(entry)"
          >
            <template v-if="entry.nodeType === 'folder'">
              <div class="folder-browser-folder">
                <div class="folder-browser-folder-cover">
                  <img class="folder-browser-folder-cover-img" :src="folderCover" alt="文件夹" />
                </div>
                <div class="folder-browser-folder-body">
                  <h4 class="folder-browser-title">{{ entry.label }}</h4>
                  <p class="folder-browser-desc">
                    {{ getFolderNodeDescription(entry) }}
                  </p>
                  <div class="folder-browser-footer">
                    <span class="folder-browser-meta">{{ entry.children?.length || 0 }} 项</span>
                    <el-button type="primary" link @click.stop="enterFolderNode(entry)">
                      打开
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="folder-browser-file">
                <div class="folder-browser-file-cover-wrap">
                  <img
                    class="folder-browser-file-cover"
                    :src="getResourceCover(entry.fileType)"
                    :alt="`${getDisplayFileType(entry.fileType)} 封面`"
                  />
                </div>
                <div class="folder-browser-file-body">
                  <h4 class="folder-browser-title">{{ entry.label }}</h4>
                  <div class="folder-browser-file-tags">
                    <el-tag size="small" effect="plain">{{ getDisplayFileType(entry.fileType) }}</el-tag>
                    <span class="folder-browser-meta">{{ formatFileSize(entry.fileSize) }}</span>
                  </div>
                  <div class="folder-browser-footer">
                    <el-button type="primary" link @click.stop="handleDownload(entry.resourceId)">
                      下载
                    </el-button>
                    <el-button
                      v-if="isUploader(entry.raw)"
                      type="danger"
                      link
                      @click.stop="handleDelete(entry.resourceId)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
          </el-card>
        </div>
        <el-empty v-else description="当前目录为空" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { resourceAPI } from '@/api/resource'
import { featuredResourceAPI } from '@/api/featuredResource'
import { useUserStore } from '@/store/user'
import { formatFileSize } from '@/utils/format'
import { Search, Upload, Download, Delete, FolderOpened } from '@element-plus/icons-vue'
import pdfCover from '@/assets/resource-covers/pdf.svg'
import docCover from '@/assets/resource-covers/doc.svg'
import docxCover from '@/assets/resource-covers/docx.svg'
import genericCover from '@/assets/resource-covers/generic.svg'
import folderCover from '@/assets/resource-covers/folder.svg'
import uploadMascot from '@/assets/lxh_011_hd.gif'

const MAX_FILE_SIZE = 50 * 1024 * 1024
const MAX_FOLDER_SIZE = 500 * 1024 * 1024
const MAX_FOLDER_FILE_COUNT = 100
const MAX_SECTION_FETCH_SIZE = 1000
const SUPPORTED_FILE_TYPES = new Set(['pdf', 'doc', 'docx'])

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const uploadLoading = ref(false)
const folderUploadLoading = ref(false)
const featuredSubmitLoading = ref(false)
const featuredResourceOptionsLoading = ref(false)
const resourceList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const uploadDialogVisible = ref(false)
const folderUploadDialogVisible = ref(false)
const featuredDialogVisible = ref(false)
const folderViewerVisible = ref(false)
const uploadFormRef = ref(null)
const featuredFormRef = ref(null)
const uploadRef = ref(null)
const folderInputRef = ref(null)
const activeFolderGroupKey = ref('')
const activeFolderGroup = ref(null)
const folderViewerTreeData = ref([])
const folderViewerPath = ref([])
const featuredResourceOptions = ref([])

const resourceSections = [
  {
    key: 'all',
    label: '全部资源',
    description: '浏览站内全部共享资料',
    helper: '这里保留原来的资源列表，支持搜索、下载、上传单文件和上传文件夹。',
    emptyText: '暂无资源'
  },
  {
    key: 'featured',
    label: '站长主推',
    description: '集中展示站长重点推荐内容',
    helper: '这一栏现在走后端主推接口，管理员可以从已有资源中选择内容加入主推。',
    emptyText: '暂时还没有站长主推资源'
  },
  {
    key: 'github',
    label: '链接分享',
    description: '收集值得收藏的网站、文档、仓库与视频链接',
    helper: '这一栏现在用于承接通用链接分享场景，不再限定为 GitHub 仓库资源。',
    emptyText: '暂时还没有链接分享资源'
  }
]

const searchForm = reactive({
  keyword: '',
  fileType: ''
})

const uploadForm = reactive({
  title: '',
  description: '',
  file: null
})

const folderUploadForm = reactive({
  folderName: '',
  description: '',
  files: [],
  relativePaths: [],
  totalSize: 0
})

const featuredForm = reactive({
  resourceId: null,
  featuredTitle: '',
  featuredDesc: '',
  coverUrl: '',
  sort: 0,
  status: 1
})

const uploadRules = {
  title: [
    { required: true, message: '请输入资料名称', trigger: 'blur' }
  ],
  file: [
    { required: true, message: '请选择文件', trigger: 'change' }
  ]
}

const featuredRules = {
  resourceId: [
    { required: true, message: '请选择要加入主推的资源', trigger: 'change' }
  ]
}

const coverMap = {
  pdf: pdfCover,
  doc: docCover,
  docx: docxCover
}

const currentSection = computed(() => {
  if (route.path.startsWith('/resource/featured')) {
    return 'featured'
  }
  if (route.path.startsWith('/resource/links')) {
    return 'github'
  }
  return 'all'
})

const currentSectionMeta = computed(() => {
  return resourceSections.find(item => item.key === currentSection.value) || resourceSections[0]
})

const isAdmin = computed(() => Number(userStore.userInfo?.role) === 1)

const getNormalizedFileType = (fileType) => {
  return String(fileType || '').trim().toLowerCase()
}

const getResourceCover = (fileType) => {
  return coverMap[getNormalizedFileType(fileType)] || genericCover
}

const getDisplayFileType = (fileType) => {
  const normalized = getNormalizedFileType(fileType)
  return normalized ? normalized.toUpperCase() : 'FILE'
}

const getResourceDescription = (item) => {
  if (item.displayType === 'folder') {
    if (item.description) {
      return item.description
    }
    return `共${item.fileCount} 个文件，点击后可逐层展开目录`
  }
  return item.description || '暂无描述'
}

const getFileExtension = (fileName) => {
  const normalizedName = String(fileName || '').trim()
  const lastDotIndex = normalizedName.lastIndexOf('.')
  if (lastDotIndex === -1) {
    return ''
  }
  return normalizedName.slice(lastDotIndex + 1).toLowerCase()
}

const getFileTitle = (fileName) => {
  const normalizedName = String(fileName || '').trim()
  const lastDotIndex = normalizedName.lastIndexOf('.')
  if (lastDotIndex === -1) {
    return normalizedName
  }
  return normalizedName.slice(0, lastDotIndex)
}

const resetUploadForm = () => {
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.file = null
  uploadRef.value?.clearFiles()
}

const resetFolderUploadForm = () => {
  folderUploadForm.folderName = ''
  folderUploadForm.description = ''
  folderUploadForm.files = []
  folderUploadForm.relativePaths = []
  folderUploadForm.totalSize = 0
  if (folderInputRef.value) {
    folderInputRef.value.value = ''
  }
}

const resetFeaturedForm = () => {
  featuredForm.resourceId = null
  featuredForm.featuredTitle = ''
  featuredForm.featuredDesc = ''
  featuredForm.coverUrl = ''
  featuredForm.sort = 0
  featuredForm.status = 1
  featuredFormRef.value?.clearValidate?.()
}

const escapeHtml = (value) => {
  return String(value || '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

const showFolderUploadErrors = async (errors) => {
  await ElMessageBox.alert(
    `<div class="folder-error-dialog">${errors.map(item => `<p>${escapeHtml(item)}</p>`).join('')}</div>`,
    '文件夹校验未通过',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了'
    }
  )
}

const showFailedFilesDetail = async (failedFiles) => {
  if (!failedFiles.length) {
    return
  }

  const html = failedFiles
    .slice(0, 10)
    .map(item => {
      const name = escapeHtml(item.relativePath || item.fileName || '未知文件')
      const reason = escapeHtml(item.reason || '上传失败')
      return `<p><strong>${name}</strong><br/>${reason}</p>`
    })
    .join('')

  const moreCount = failedFiles.length - 10
  const moreHtml = moreCount > 0 ? `<p>其余 ${moreCount} 个失败文件请联系后端排查。</p>` : ''

  await ElMessageBox.alert(
    `<div class="folder-error-dialog">${html}${moreHtml}</div>`,
    '部分文件上传失败',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了'
    }
  )
}

const validateFolderFiles = (files, relativePaths) => {
  const errors = []
  if (!files.length) {
    errors.push('所选文件夹中没有可上传文件')
    return errors
  }

  if (files.length > MAX_FOLDER_FILE_COUNT) {
    errors.push(`文件夹中文件数量不能超过 ${MAX_FOLDER_FILE_COUNT} 个`)
  }

  if (files.length !== relativePaths.length) {
    errors.push('文件数量与路径数量不一致')
  }

  const totalSize = files.reduce((sum, file) => sum + (file.size || 0), 0)
  if (totalSize > MAX_FOLDER_SIZE) {
    errors.push('整个文件夹总大小不能超过 500MB')
  }

  files.forEach((file, index) => {
    const extension = getFileExtension(file.name)
    const relativePath = String(relativePaths[index] || '').trim()

    if (!SUPPORTED_FILE_TYPES.has(extension)) {
      errors.push(`文件类型不支持：${file.name}`)
    }

    if (file.size > MAX_FILE_SIZE) {
      errors.push(`单个文件不能超过 50MB：${file.name}`)
    }

    if (!relativePath) {
      errors.push(`文件路径不能为空：${file.name}`)
    }
  })

  return [...new Set(errors)]
}

const buildFolderTree = (group) => {
  const rootNode = {
    id: `root-${group.key}`,
    label: group.folderName,
    nodeType: 'folder',
    children: []
  }

  group.items.forEach(item => {
    const normalizedPath = String(item.relativePath || item.title || '').trim()
    const segments = normalizedPath
      ? normalizedPath.split('/').filter(Boolean)
      : [item.title || `文件-${item.id}`]

    let currentNode = rootNode
    let currentPath = ''

    segments.forEach((segment, index) => {
      currentPath = currentPath ? `${currentPath}/${segment}` : segment
      const isFileNode = index === segments.length - 1

      if (isFileNode) {
        currentNode.children.push({
          id: `file-${item.id}`,
          label: segment,
          nodeType: 'file',
          fileType: item.fileType,
          fileSize: item.fileSize,
          resourceId: item.id,
          raw: item
        })
        return
      }

      let nextNode = currentNode.children.find(child => child.id === `dir-${currentPath}`)
      if (!nextNode) {
        nextNode = {
          id: `dir-${currentPath}`,
          label: segment,
          nodeType: 'folder',
          children: []
        }
        currentNode.children.push(nextNode)
      }
      currentNode = nextNode
    })
  })

  return [rootNode]
}

const countFolderNodeFiles = (node) => {
  if (!node?.children?.length) {
    return 0
  }

  return node.children.reduce((sum, child) => {
    if (child.nodeType === 'file') {
      return sum + 1
    }
    return sum + countFolderNodeFiles(child)
  }, 0)
}

const folderViewerRootNode = computed(() => {
  return folderViewerTreeData.value[0] || null
})

const folderViewerPathSegments = computed(() => {
  const segments = []
  let currentChildren = folderViewerRootNode.value?.children || []

  folderViewerPath.value.forEach(nodeId => {
    const nextNode = currentChildren.find(child => child.id === nodeId && child.nodeType === 'folder')
    if (!nextNode) {
      return
    }
    segments.push({
      id: nextNode.id,
      label: nextNode.label
    })
    currentChildren = nextNode.children || []
  })

  return segments
})

const currentFolderNode = computed(() => {
  let currentNode = folderViewerRootNode.value
  if (!currentNode) {
    return null
  }

  for (const nodeId of folderViewerPath.value) {
    const nextNode = (currentNode.children || []).find(child => child.id === nodeId && child.nodeType === 'folder')
    if (!nextNode) {
      break
    }
    currentNode = nextNode
  }

  return currentNode
})

const currentFolderEntries = computed(() => {
  const children = currentFolderNode.value?.children || []
  return [...children].sort((left, right) => {
    if (left.nodeType !== right.nodeType) {
      return left.nodeType === 'folder' ? -1 : 1
    }
    return String(left.label || '').localeCompare(String(right.label || ''), 'zh-CN')
  })
})

const getFolderNodeDescription = (node) => {
  const directChildrenCount = node.children?.length || 0
  const fileCount = countFolderNodeFiles(node)
  if (directChildrenCount === 0) {
    return '空文件夹'
  }
  return `包含 ${directChildrenCount} 项，累计 ${fileCount} 个文件`
}

const buildDisplayResourceList = sourceList => {
  const result = []
  const folderGroupMap = new Map()

  sourceList.forEach(item => {
    if (!(item.uploadType === 'folder' && item.folderName)) {
      result.push({
        ...item,
        displayType: 'file',
        key: `file-${item.id}`
      })
      return
    }

    const groupIdentity = item.batchNo || `${item.folderName}-${item.uploaderName || 'anonymous'}-${item.createTime || ''}`
    const groupKey = `folder-${groupIdentity}`

    if (!folderGroupMap.has(groupKey)) {
      const group = {
        key: groupKey,
        displayType: 'folder',
        folderName: item.folderName,
        batchNo: item.batchNo || '',
        uploaderName: item.uploaderName,
        description: item.description || '',
        createTime: item.createTime,
        items: [],
        totalSize: 0,
        downloadCount: 0
      }
      folderGroupMap.set(groupKey, group)
      result.push(group)
    }

    const group = folderGroupMap.get(groupKey)
    group.items.push(item)
    group.totalSize += Number(item.fileSize || 0)
    group.downloadCount += Number(item.downloadCount || 0)

    if (!group.description && item.description) {
      group.description = item.description
    }
    if ((item.createTime || '') > (group.createTime || '')) {
      group.createTime = item.createTime
    }
  })

  return result.map(item => {
    if (item.displayType !== 'folder') {
      return item
    }

    const previewPaths = item.items
      .map(child => child.relativePath)
      .filter(Boolean)
      .slice(0, 3)

    return {
      ...item,
      fileCount: item.items.length,
      previewPaths,
      remainingCount: Math.max(item.items.length - previewPaths.length, 0)
    }
  })
}

const getResourceText = (item) => {
  return [
    item.title,
    item.description,
    item.folderName,
    item.relativePath
  ].join(' ').toLowerCase()
}

const currentSectionKeywords = computed(() => {
  if (currentSection.value === 'github') {
    return ['github', 'git hub', '开源', 'opensource', 'open source', 'repo', '仓库']
  }
  return []
})

const filteredSectionResources = computed(() => {
  let baseList = resourceList.value

  if (searchForm.fileType) {
    const normalizedType = getNormalizedFileType(searchForm.fileType)
    baseList = baseList.filter(item => getNormalizedFileType(item.fileType) === normalizedType)
  }

  if (currentSection.value === 'all' || currentSection.value === 'featured') {
    return baseList
  }

  const keywords = currentSectionKeywords.value
  return baseList.filter(item => {
    const text = getResourceText(item)
    return keywords.some(keyword => text.includes(String(keyword).toLowerCase()))
  })
})

const groupedDisplayResourceList = computed(() => {
  return buildDisplayResourceList(filteredSectionResources.value)
})

const displayTotal = computed(() => {
  if (currentSection.value === 'all') {
    return total.value
  }
  return groupedDisplayResourceList.value.length
})

const pagedDisplayResourceList = computed(() => {
  if (currentSection.value === 'all') {
    return groupedDisplayResourceList.value
  }

  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return groupedDisplayResourceList.value.slice(start, end)
})

const resourceColumns = computed(() => {
  const left = []
  const right = []
  pagedDisplayResourceList.value.forEach((item, index) => {
    if (index % 2 === 0) {
      left.push(item)
    } else {
      right.push(item)
    }
  })
  return [left, right]
})

const refreshActiveFolderViewer = () => {
  if (!folderViewerVisible.value || !activeFolderGroupKey.value) {
    return
  }

  const group = groupedDisplayResourceList.value.find(item => item.key === activeFolderGroupKey.value)
  if (!group || group.displayType !== 'folder') {
    folderViewerVisible.value = false
    activeFolderGroup.value = null
    activeFolderGroupKey.value = ''
    folderViewerTreeData.value = []
    return
  }

  activeFolderGroup.value = group
  folderViewerTreeData.value = buildFolderTree(group)

  const resolvedPath = []
  let currentNode = folderViewerTreeData.value[0]
  for (const nodeId of folderViewerPath.value) {
    const nextNode = (currentNode?.children || []).find(child => child.id === nodeId && child.nodeType === 'folder')
    if (!nextNode) {
      break
    }
    resolvedPath.push(nextNode.id)
    currentNode = nextNode
  }
  folderViewerPath.value = resolvedPath
}

const loadResourceList = async () => {
  loading.value = true
  try {
    if (currentSection.value === 'featured') {
      const data = await featuredResourceAPI.getFeaturedResourceList({
        page: 1,
        size: MAX_SECTION_FETCH_SIZE,
        keyword: searchForm.keyword || undefined
      })
      resourceList.value = (data.records || []).map(item => ({
        ...item,
        id: item.resourceId,
        featuredId: item.id,
        description: item.description || item.featuredDesc || '',
        createTime: item.createTime,
        downloadId: item.resourceId
      }))
      total.value = buildDisplayResourceList(filteredSectionResources.value).length
      refreshActiveFolderViewer()
      return
    }

    const params = {
      page: currentSection.value === 'all' ? currentPage.value : 1,
      size: currentSection.value === 'all' ? pageSize.value : MAX_SECTION_FETCH_SIZE
    }

    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    if (searchForm.fileType && currentSection.value === 'all') {
      params.fileType = searchForm.fileType
    }

    const data = await resourceAPI.getResourceList(params)
    resourceList.value = data.records || []
    total.value = currentSection.value === 'all'
      ? (data.total || 0)
      : buildDisplayResourceList(filteredSectionResources.value).length
    refreshActiveFolderViewer()
  } catch (error) {
    console.error('加载资源列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadResourceList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.fileType = ''
  currentPage.value = 1
  loadResourceList()
}

const showUploadDialog = () => {
  uploadDialogVisible.value = true
}

const openFeaturedDialog = async () => {
  if (!isAdmin.value) {
    return
  }
  featuredDialogVisible.value = true
  if (!featuredResourceOptions.value.length) {
    await loadFeaturedResourceOptions('')
  }
}

const closeFeaturedDialog = () => {
  featuredDialogVisible.value = false
  resetFeaturedForm()
}

const loadFeaturedResourceOptions = async (keyword = '') => {
  featuredResourceOptionsLoading.value = true
  try {
    const data = await resourceAPI.getResourceList({
      page: 1,
      size: 20,
      keyword: keyword || undefined
    })
    featuredResourceOptions.value = data.records || []
  } catch (error) {
    console.error('加载可选资源失败', error)
  } finally {
    featuredResourceOptionsLoading.value = false
  }
}

const submitFeaturedResource = async () => {
  if (!isAdmin.value) {
    ElMessage.error('只有管理员可以添加站长主推')
    return
  }

  try {
    await featuredFormRef.value?.validate()
  } catch {
    return
  }

  featuredSubmitLoading.value = true
  try {
    const payload = {
      resourceId: featuredForm.resourceId,
      featuredTitle: featuredForm.featuredTitle || undefined,
      featuredDesc: featuredForm.featuredDesc || undefined,
      coverUrl: featuredForm.coverUrl || undefined,
      sort: featuredForm.sort ?? 0,
      status: featuredForm.status
    }

    await featuredResourceAPI.addFeaturedResource(payload)
    ElMessage.success('添加主推成功')
    closeFeaturedDialog()
    await loadResourceList()
  } catch (error) {
    console.error('添加主推失败:', error)
  } finally {
    featuredSubmitLoading.value = false
  }
}

const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传一个文件')
}

const handleUpload = async () => {
  await uploadFormRef.value.validate(async (valid) => {
    if (valid) {
      if (!uploadForm.file) {
        ElMessage.warning('请选择文件')
        return
      }

      if (uploadForm.file.size > MAX_FILE_SIZE) {
        ElMessage.error('文件大小不能超过 50MB')
        return
      }

      uploadLoading.value = true
      try {
        const formData = new FormData()
        formData.append('file', uploadForm.file)
        formData.append('title', uploadForm.title)
        if (uploadForm.description) {
          formData.append('description', uploadForm.description)
        }

        await resourceAPI.uploadResource(formData)
        ElMessage.success('上传成功')
        uploadDialogVisible.value = false
        resetUploadForm()
        loadResourceList()
      } catch (error) {
        console.error('上传失败:', error)
      } finally {
        uploadLoading.value = false
      }
    }
  })
}

const openFolderSelector = () => {
  if (folderInputRef.value) {
    folderInputRef.value.value = ''
    folderInputRef.value.click()
  }
}

const handleFolderSelect = async (event) => {
  const selectedFiles = Array.from(event.target.files || [])
  if (!selectedFiles.length) {
    return
  }

  const firstRelativePath = String(selectedFiles[0].webkitRelativePath || '').trim()
  const folderName = firstRelativePath.split('/')[0] || getFileTitle(selectedFiles[0].name) || '未命名文件夹'
  const relativePaths = selectedFiles.map(file => {
    const currentPath = String(file.webkitRelativePath || file.name || '').trim()
    if (currentPath.startsWith(`${folderName}/`)) {
      return currentPath.slice(folderName.length + 1)
    }
    return currentPath
  })

  const errors = validateFolderFiles(selectedFiles, relativePaths)
  if (errors.length) {
    resetFolderUploadForm()
    await showFolderUploadErrors(errors)
    return
  }

  folderUploadForm.folderName = folderName
  folderUploadForm.description = ''
  folderUploadForm.files = selectedFiles
  folderUploadForm.relativePaths = relativePaths
  folderUploadForm.totalSize = selectedFiles.reduce((sum, file) => sum + (file.size || 0), 0)
  folderUploadDialogVisible.value = true
}

const closeFolderUploadDialog = () => {
  folderUploadDialogVisible.value = false
  resetFolderUploadForm()
}

const openFolderViewer = (group) => {
  activeFolderGroupKey.value = group.key
  activeFolderGroup.value = group
  folderViewerTreeData.value = buildFolderTree(group)
  folderViewerPath.value = []
  folderViewerVisible.value = true
}

const enterFolderNode = (node) => {
  if (node.nodeType !== 'folder') {
    return
  }
  folderViewerPath.value = [...folderViewerPath.value, node.id]
}

const goToFolderRoot = () => {
  folderViewerPath.value = []
}

const goToFolderLevel = (index) => {
  folderViewerPath.value = folderViewerPath.value.slice(0, index + 1)
}

const goToParentFolder = () => {
  if (!folderViewerPath.value.length) {
    return
  }
  folderViewerPath.value = folderViewerPath.value.slice(0, -1)
}

const submitFolderUpload = async () => {
  const errors = validateFolderFiles(folderUploadForm.files, folderUploadForm.relativePaths)
  if (!folderUploadForm.folderName) {
    errors.unshift('文件夹名称不能为空')
  }

  if (errors.length) {
    await showFolderUploadErrors(errors)
    return
  }

  folderUploadLoading.value = true
  try {
    const formData = new FormData()
    formData.append('folderName', folderUploadForm.folderName)

    if (folderUploadForm.description) {
      formData.append('description', folderUploadForm.description)
    }

    folderUploadForm.files.forEach(file => {
      formData.append('files', file)
    })

    folderUploadForm.relativePaths.forEach(path => {
      formData.append('relativePaths', path)
    })

    const result = await resourceAPI.uploadResourceFolder(formData)
    closeFolderUploadDialog()
    loadResourceList()

    if ((result.failCount || 0) > 0) {
      ElMessage.warning(`上传完成，成功${result.successCount || 0} 个，失败 ${result.failCount || 0} 个`)
      await showFailedFilesDetail(result.failedFiles || [])
      return
    }

    ElMessage.success(`文件夹上传成功，共${result.successCount || 0} 个文件`)
  } catch (error) {
    console.error('文件夹上传失败', error)
  } finally {
    folderUploadLoading.value = false
  }
}

const handleDownload = (id) => {
  const url = resourceAPI.downloadResource(id)
  const target = resourceList.value.find(item => item.id === id)

  if (target) {
    target.downloadCount = (target.downloadCount || 0) + 1
  }

  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const isUploader = (item) => {
  if (!userStore.isLogin()) return false
  return item.uploaderName === userStore.userInfo?.nickname ||
         item.uploaderName === userStore.userInfo?.username
}

const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除这个资料吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await resourceAPI.deleteResource(id)
      ElMessage.success('删除成功')
      await loadResourceList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadResourceList()
})

watch(
  () => route.path,
  () => {
    currentPage.value = 1
    loadResourceList()
  }
)

watch(displayTotal, value => {
  if (currentSection.value === 'all' || value === 0) {
    return
  }
  const maxPage = Math.max(Math.ceil(value / pageSize.value), 1)
  if (currentPage.value > maxPage) {
    currentPage.value = maxPage
  }
})
</script>

<style scoped>
.resource-container {
  max-width: 1080px;
  margin: 0 auto;
}

.action-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.section-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-summary-title {
  margin: 0 0 8px;
  font-size: 20px;
  color: #25384c;
}

.section-summary-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #7a8ea3;
}

.upload-dialog :deep(.el-textarea__inner),
.upload-dialog :deep(.el-input__inner) {
  letter-spacing: normal;
}

.featured-option-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.featured-option-title {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.featured-option-meta {
  flex-shrink: 0;
  font-size: 12px;
  color: #8a9bad;
}

.folder-input {
  display: none;
}

.upload-entry-group {
  display: flex;
  gap: 12px;
}

.resource-list {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  min-height: 400px;
}

.resource-column {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
  min-width: 0;
}

.resource-row {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 10px 16px;
  border: 1px solid #e6eef5;
  border-radius: 16px;
  background: #fff;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.resource-row:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(155, 194, 224, 0.16);
}

/* ===== 站长主推 · 金色描边贵气卡 ===== */
.featured-resource-row {
  border: none;
  background: #ffffff;
  padding: 18px;
}

.featured-resource-row::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  padding: 2px;
  background: linear-gradient(135deg, #f7d774 0%, #ffe9b0 28%, #f0b53a 55%, #e8a229 78%, #f7d774 100%);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}

.featured-resource-row::after {
  content: '';
  position: absolute;
  top: 16px;
  left: 16px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: radial-gradient(circle, #ffe9a8 0%, #e8a229 70%);
  box-shadow: 0 0 12px 3px rgba(240, 181, 58, 0.45);
  pointer-events: none;
}

.featured-resource-row:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 36px rgba(224, 162, 46, 0.22);
}

.featured-resource-row .resource-row-cover {
  width: 76px;
  height: 76px;
  margin: 0 16px 0 30px;
  background: linear-gradient(135deg, #fdf7ec 0%, #f7ecd4 100%);
  box-shadow: inset 0 0 0 1px rgba(224, 162, 46, 0.25);
}

.featured-resource-row .resource-row-title {
  font-size: 17px;
  color: #1f2f44;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.featured-resource-row .resource-row-desc {
  color: #2b5ea8;
  margin-bottom: 8px;
}

.featured-resource-row .meta-item {
  color: #6b7a8b;
}

.featured-resource-row :deep(.el-tag) {
  color: #b07a15;
  background: #fdf3d8;
  border-color: #f0ddab;
}

.featured-resource-row :deep(.el-button--primary.is-link) {
  color: #c08a1c;
}

.resource-row-cover,
.resource-row-main,
.resource-row-actions {
  position: relative;
}

.resource-row-cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 16px;
  flex-shrink: 0;
  background: #f8fbff;
}

.resource-row-main {
  flex: 1;
  min-width: 0;
}

.resource-row-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 700;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-row-desc {
  margin: 0 0 6px;
  font-size: 14px;
  color: #0b63b3;
  line-height: 1.7;
  overflow-wrap: break-word;
  word-break: break-word;
}

.resource-row-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #666;
}

.meta-item {
  font-size: 12px;
  color: #666;
}

.meta-path {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-row-paths {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.folder-path-chip {
  max-width: 100%;
  padding: 5px 9px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #e6eef5;
  color: #6a7b8f;
  font-size: 11px;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-path-more {
  color: #4f83cc;
  border-color: #dbe8ff;
  background: #f5f9ff;
}

.resource-row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  align-self: flex-start;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.upload-loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  padding: 40px 20px;
}

.upload-loading-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.upload-loading-gif {
  width: 120px;
  height: 120px;
  object-fit: contain;
}

.upload-loading-text {
  font-size: 16px;
  font-weight: 600;
  color: #4a5a6a;
  margin: 0;
  letter-spacing: 0.04em;
}

.upload-loading-sub-text {
  font-size: 13px;
  color: #8a9bb0;
  margin: 4px 0 0;
  letter-spacing: 0.03em;
}

.ellipsis-anim {
  display: inline-block;
  width: 0;
  overflow: hidden;
  vertical-align: bottom;
  text-align: left;
  animation: ellipsisSteps 1.5s steps(4, end) infinite;
}

.ellipsis-anim::before {
  content: '...';
  display: inline-block;
}

@keyframes ellipsisSteps {
  0%   { width: 0; }
  25%  { width: 0.3em; }
  50%  { width: 0.6em; }
  75%  { width: 1em; }
  100% { width: 1em; }
}

/* 小黑滚动文字效果 — 三条消息依次向上滚动 */
.scroll-text-container {
  height: 28px;
  overflow: hidden;
  position: relative;
}

.scroll-text-item {
  position: absolute;
  width: 100%;
  left: 0;
  top: 0;
  animation: textCarousel 6s ease-in-out infinite;
}

.scroll-text-item:nth-child(2) {
  animation-delay: 2s;
}

.scroll-text-item:nth-child(3) {
  animation-delay: 4s;
}

@keyframes textCarousel {
  0% {
    transform: translateY(100%);
    opacity: 0;
  }
  5% {
    transform: translateY(0);
    opacity: 1;
  }
  25% {
    transform: translateY(0);
    opacity: 1;
  }
  30% {
    transform: translateY(-100%);
    opacity: 0;
  }
  100% {
    transform: translateY(-100%);
    opacity: 0;
  }
}

.folder-upload-summary {
  display: flex;
  gap: 16px;
  color: #606266;
  flex-wrap: wrap;
}

.folder-file-preview {
  width: 100%;
  max-height: 220px;
  overflow: auto;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e5edf5;
}

.folder-file-preview-item {
  font-size: 13px;
  color: #4a5a6a;
  line-height: 1.7;
  word-break: break-all;
}

.folder-file-preview-more {
  margin-top: 8px;
  font-size: 12px;
  color: #8a97a6;
}

.folder-viewer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.folder-viewer-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafc;
  color: #5f6f82;
  font-size: 13px;
}

.folder-browser-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #e8eef5;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.folder-breadcrumbs {
  min-width: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
}

.folder-breadcrumb-separator {
  color: #9aa9b9;
}

.folder-browser-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 18px;
}

.folder-browser-card {
  border-radius: 18px;
  border: 1px solid #e8eef5;
}

.folder-browser-folder,
.folder-browser-file {
  display: flex;
  gap: 14px;
  min-height: 156px;
}

.folder-browser-folder-cover,
.folder-browser-file-cover-wrap {
  width: 72px;
  min-width: 72px;
  height: 72px;
  border-radius: 14px;
  overflow: hidden;
}

.folder-browser-folder-cover-img,
.folder-browser-file-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.folder-browser-folder-body,
.folder-browser-file-body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.folder-browser-title {
  margin: 0 0 10px;
  font-size: 16px;
  line-height: 1.4;
  color: #243446;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.folder-browser-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #75869a;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.folder-browser-file-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.folder-browser-meta {
  font-size: 12px;
  color: #7b8a9a;
}

.folder-browser-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

@media (max-width: 768px) {
  .section-summary {
    flex-direction: column;
  }

  .resource-list {
    flex-direction: column;
  }

  .resource-row {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .resource-row-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .upload-entry-group {
    width: 100%;
    flex-direction: column;
  }

  .folder-browser-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .folder-browser-grid {
    grid-template-columns: 1fr;
  }

  .folder-browser-folder,
  .folder-browser-file {
    flex-direction: row;
  }

  .folder-browser-folder-cover,
  .folder-browser-file-cover-wrap {
    width: 56px;
    min-width: 56px;
    height: 56px;
    border-radius: 12px;
  }

  .folder-browser-folder-cover-img,
  .folder-browser-file-cover {
    min-height: 0;
  }
}

@media (max-width: 480px) {
  .section-summary {
    gap: 10px;
    margin-bottom: 14px;
  }

  /* 搜索/操作区纵向堆叠 */
  .action-card :deep(.el-form--inline .el-form-item) {
    display: block;
    width: 100%;
    margin-right: 0;
    margin-bottom: 12px;
  }

  .action-card :deep(.el-form-item .el-input),
  .action-card :deep(.el-form-item .el-select) {
    width: 100% !important;
  }

  .action-card :deep(.el-form-item .el-button) {
    width: 48%;
  }

  .action-card :deep(.el-form-item .el-button + .el-button) {
    margin-left: 4%;
  }

  .upload-entry-group :deep(.el-button) {
    width: 100%;
    margin-left: 0 !important;
  }

  /* 资源行紧凑化 */
  .resource-row {
    padding: 12px;
    gap: 10px;
  }

  .resource-row-cover {
    width: 56px;
    height: 56px;
    border-radius: 12px;
  }

  .featured-resource-row {
    padding: 16px 14px;
  }

  .featured-resource-row .resource-row-cover {
    width: 52px;
    height: 52px;
    margin: 0 10px 0 14px;
  }

  .featured-resource-row::after {
    top: 10px;
    left: 10px;
    width: 6px;
    height: 6px;
  }

  .resource-row-title {
    font-size: 14px;
  }

  .resource-row-actions {
    flex-wrap: wrap;
    gap: 2px;
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

  /* 上传/主推/文件夹查看对话框小屏全宽 */
  .upload-dialog :deep(.el-dialog),
  .featured-dialog :deep(.el-dialog),
  .folder-viewer-dialog :deep(.el-dialog) {
    width: calc(100vw - 28px) !important;
    margin-top: 6vh !important;
  }
}
</style>

