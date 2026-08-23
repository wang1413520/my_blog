<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :xs="24" :md="8">
        <el-card class="user-card" shadow="hover">
          <div class="user-avatar-section">
            <template v-if="userInfo">
              <el-avatar :size="100" :src="userInfo.avatar">
                {{ userInfo.nickname?.charAt(0) || userInfo.username?.charAt(0) || 'U' }}
              </el-avatar>
              <h2 class="user-nickname">{{ userInfo.nickname || userInfo.username }}</h2>
              <p class="user-username">@{{ userInfo.username }}</p>
              <el-tag :type="userInfo.role === 1 ? 'danger' : 'primary'">
                {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
              <p class="join-time">加入时间：{{ userInfo.createTime }}</p>
            </template>
            <template v-else>
              <el-skeleton animated>
                <template #template>
                  <div class="profile-skeleton">
                    <el-skeleton-item variant="circle" style="width: 100px; height: 100px; margin: 0 auto 16px;" />
                    <el-skeleton-item variant="h3" style="width: 120px; height: 28px; margin: 0 auto 12px;" />
                    <el-skeleton-item variant="text" style="width: 90px; margin: 0 auto 12px;" />
                    <el-skeleton-item variant="button" style="width: 84px; height: 28px; margin: 0 auto 12px;" />
                    <el-skeleton-item variant="text" style="width: 140px; margin: 0 auto;" />
                  </div>
                </template>
              </el-skeleton>
            </template>
          </div>
        </el-card>

        <el-card class="menu-card" shadow="hover">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
            <el-menu-item index="profile">
              <el-icon><Document /></el-icon>
              <span>我的资料</span>
            </el-menu-item>
            <el-menu-item index="resources">
              <el-icon><Files /></el-icon>
              <span>我的资源</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card v-if="activeMenu === 'info'" class="content-card" shadow="hover">
          <template #header>
            <span class="card-title">个人信息</span>
          </template>
          <template v-if="userInfo">
            <el-form
              ref="infoFormRef"
              :model="infoForm"
              :rules="infoRules"
              label-width="100px"
            >
              <el-form-item label="用户名">
                <el-input :model-value="userInfo.username || ''" disabled />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="头像" prop="avatar">
                <div class="avatar-upload-section">
                  <div class="avatar-preview-wrap">
                    <el-avatar :size="84" :src="infoForm.avatar || userInfo.avatar">
                      {{ infoForm.nickname?.charAt(0) || userInfo.username?.charAt(0) || 'U' }}
                    </el-avatar>
                  </div>
                  <div class="avatar-upload-actions">
                    <el-upload
                      class="avatar-uploader"
                      :show-file-list="false"
                      :auto-upload="false"
                      accept=".jpg,.jpeg,.png,.webp"
                      :on-change="handleAvatarChange"
                    >
                      <el-button :loading="avatarUploading" type="primary" plain>
                        {{ avatarUploading ? '上传中...' : '选择头像' }}
                      </el-button>
                    </el-upload>
                    <p class="avatar-tip">支持 JPG、PNG、WEBP，建议 5MB 以内。</p>
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </template>
          <el-skeleton v-else animated :rows="4" />
        </el-card>

        <el-card v-if="activeMenu === 'password'" class="content-card" shadow="hover">
          <template #header>
            <span class="card-title">修改密码</span>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="passwordLoading" @click="handleUpdatePassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card v-if="activeMenu === 'profile'" class="content-card" shadow="hover">
          <template #header>
            <span class="card-title">我的资料</span>
          </template>

          <el-skeleton v-if="profileDetailLoading" animated :rows="8" />

          <el-form
            v-else
            ref="profileFormRef"
            class="profile-detail-form"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="性别" prop="gender">
                  <el-select v-model="profileForm.gender" placeholder="请选择性别" clearable>
                    <el-option label="男" value="male" />
                    <el-option label="女" value="female" />
                    <el-option label="保密" value="unknown" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="出生日期" prop="birthday">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="请选择出生日期"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="学校" prop="school">
                  <el-input v-model="profileForm.school" placeholder="请输入学校" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="学院" prop="college">
                  <el-input v-model="profileForm.college" placeholder="请输入学院" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="专业" prop="major">
                  <el-input v-model="profileForm.major" placeholder="请输入专业" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :xs="24" :sm="12">
                <el-form-item label="年级" prop="grade">
                  <el-input v-model="profileForm.grade" placeholder="请输入年级" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="所在城市" prop="location">
                  <el-input v-model="profileForm.location" placeholder="请输入所在城市" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="个人简介" prop="bio" class="profile-detail-bio">
              <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="5"
                maxlength="300"
                show-word-limit
                placeholder="介绍一下你自己吧"
              />
            </el-form-item>

            <el-form-item>
              <div class="profile-detail-actions">
                <el-button type="primary" :loading="profileDetailSaving" @click="handleUpdateProfileDetail">
                  保存资料
                </el-button>
                <el-button @click="resetProfileForm">重置</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <div v-if="activeMenu === 'resources'" class="resources-panel">
          <h3 class="resources-panel-title">我的资源</h3>

          <el-form :inline="true" :model="myResourceSearchForm" class="my-resource-search">
            <el-form-item>
              <el-input
                v-model="myResourceSearchForm.keyword"
                placeholder="搜索资源名称"
                clearable
                style="width: 260px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-select v-model="myResourceSearchForm.fileType" placeholder="文件类型" clearable style="width: 130px">
                <el-option label="PDF" value="pdf" />
                <el-option label="DOC" value="doc" />
                <el-option label="DOCX" value="docx" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-select v-model="myResourceSearchForm.uploadType" placeholder="上传类型" clearable style="width: 150px">
                <el-option label="单文件上传" value="single" />
                <el-option label="文件夹上传" value="folder" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleMyResourceSearch">搜索</el-button>
              <el-button @click="handleMyResourceReset">重置</el-button>
            </el-form-item>
          </el-form>

          <div v-loading="myResourceLoading" class="my-resource-list">
            <el-empty
              v-if="myResourceDisplayList.length === 0 && !myResourceLoading"
              description="还没有上传过资源，去资源页上传一份试试吧"
            />

            <div
              v-for="item in myResourceDisplayList"
              :key="item.key"
              class="resource-row"
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
                    <span class="meta-item">{{ item.fileCount }} 个文件</span>
                    <span class="meta-item">{{ formatFileSize(item.totalSize) }}</span>
                    <span class="meta-item">下载 {{ item.downloadCount }} 次</span>
                  </template>
                  <template v-else>
                    <el-tag size="small">{{ getDisplayFileType(item.fileType) }}</el-tag>
                    <el-tag v-if="item.uploadType === 'folder'" type="warning" effect="plain" size="small">
                      文件夹上传
                    </el-tag>
                    <span class="meta-item">{{ formatFileSize(item.fileSize) }}</span>
                    <span class="meta-item">下载 {{ item.downloadCount }} 次</span>
                    <span v-if="item.folderName" class="meta-item">文件夹 {{ item.folderName }}</span>
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
                  <el-button type="primary" link :icon="Download" @click="handleResourceDownload(item.id)">下载</el-button>
                  <el-button type="danger" link :icon="Delete" @click="handleResourceDelete(item.id)">删除</el-button>
                </template>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="myResourceTotal > 0"
            class="pagination my-resource-pagination"
            v-model:current-page="myResourceCurrentPage"
            v-model:page-size="myResourcePageSize"
            :total="myResourceTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadMyResourceList"
            @size-change="loadMyResourceList"
          />
        </div>
      </el-col>
    </el-row>

    <el-dialog
      v-model="folderViewerVisible"
      :title="activeFolderGroup?.folderName || '文件夹内容'"
      width="760px"
      destroy-on-close
    >
      <template v-if="activeFolderGroup">
        <div class="folder-viewer-toolbar">
          <div class="folder-viewer-breadcrumb">
            <el-button link type="primary" @click="goToFolderRoot">根目录</el-button>
            <template v-for="(segment, index) in folderViewerPathSegments" :key="segment.id">
              <span class="folder-breadcrumb-separator">/</span>
              <el-button link type="primary" @click="goToFolderLevel(index)">{{ segment.label }}</el-button>
            </template>
          </div>
          <el-button :disabled="folderViewerPath.length === 0" @click="goToParentFolder">返回上级</el-button>
        </div>

        <div class="folder-current-desc">
          <span>{{ currentFolderNode?.label || activeFolderGroup.folderName }}</span>
          <span>{{ getFolderNodeDescription(currentFolderNode || folderViewerRootNode) }}</span>
        </div>

        <div v-if="currentFolderEntries.length" class="folder-entry-list">
          <div
            v-for="entry in currentFolderEntries"
            :key="entry.id"
            class="folder-entry"
            :class="{ clickable: entry.nodeType === 'folder' }"
            @click="entry.nodeType === 'folder' && enterFolderNode(entry)"
          >
            <template v-if="entry.nodeType === 'folder'">
              <img class="folder-entry-cover" :src="folderCover" alt="文件夹" />
              <div class="folder-entry-main">
                <h4 class="folder-entry-title">{{ entry.label }}</h4>
                <p class="folder-entry-desc">{{ getFolderNodeDescription(entry) }}</p>
              </div>
              <el-button text type="primary" :icon="ArrowRight">进入</el-button>
            </template>

            <template v-else>
              <img
                class="folder-entry-cover"
                :src="getResourceCover(entry.fileType)"
                :alt="`${getDisplayFileType(entry.fileType)} 封面`"
              />
              <div class="folder-entry-main">
                <h4 class="folder-entry-title">{{ entry.label }}</h4>
                <p class="folder-entry-desc">{{ formatFileSize(entry.fileSize) }}</p>
              </div>
              <div class="folder-entry-actions">
                <el-tag size="small" effect="plain">{{ getDisplayFileType(entry.fileType) }}</el-tag>
                <el-button type="primary" text :icon="Download" @click.stop="handleResourceDownload(entry.resourceId)">
                  下载
                </el-button>
              </div>
            </template>
          </div>
        </div>

        <el-empty v-else description="当前目录暂无内容" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  Delete,
  Document,
  Download,
  Files,
  FolderOpened,
  Lock,
  Search,
  User
} from '@element-plus/icons-vue'
import { userAPI } from '@/api/user'
import { resourceAPI } from '@/api/resource'
import { useUserStore } from '@/store/user'
import pdfCover from '@/assets/resource-covers/pdf.svg'
import docCover from '@/assets/resource-covers/doc.svg'
import docxCover from '@/assets/resource-covers/docx.svg'
import genericCover from '@/assets/resource-covers/generic.svg'
import folderCover from '@/assets/resource-covers/folder.svg'

const userStore = useUserStore()

const activeMenu = ref('info')
const userInfo = ref(null)
const infoLoading = ref(false)
const passwordLoading = ref(false)
const avatarUploading = ref(false)
const profileDetailLoading = ref(false)
const profileDetailSaving = ref(false)
const profileDetailLoaded = ref(false)
const myResourceLoading = ref(false)
const myResourceLoaded = ref(false)
const myResourceList = ref([])
const myResourceCurrentPage = ref(1)
const myResourcePageSize = ref(10)
const myResourceTotal = ref(0)
const folderViewerVisible = ref(false)
const activeFolderGroupKey = ref('')
const activeFolderGroup = ref(null)
const folderViewerTreeData = ref([])
const folderViewerPath = ref([])

const infoFormRef = ref(null)
const passwordFormRef = ref(null)
const profileFormRef = ref(null)

const emptyProfileForm = () => ({
  realName: '',
  gender: '',
  birthday: '',
  phone: '',
  email: '',
  school: '',
  college: '',
  major: '',
  grade: '',
  location: '',
  bio: ''
})

const infoForm = reactive({
  nickname: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileForm = reactive(emptyProfileForm())

const myResourceSearchForm = reactive({
  keyword: '',
  fileType: '',
  uploadType: ''
})

const coverMap = {
  pdf: pdfCover,
  doc: docCover,
  docx: docxCover
}

const normalizeProfileData = (data = {}) => ({
  realName: data.realName || '',
  gender: data.gender || '',
  birthday: data.birthday || '',
  phone: data.phone || '',
  email: data.email || '',
  school: data.school || '',
  college: data.college || '',
  major: data.major || '',
  grade: data.grade || '',
  location: data.location || '',
  bio: data.bio || ''
})

const trimProfilePayload = () => ({
  realName: profileForm.realName.trim(),
  gender: profileForm.gender || '',
  birthday: profileForm.birthday || '',
  phone: profileForm.phone.trim(),
  email: profileForm.email.trim(),
  school: profileForm.school.trim(),
  college: profileForm.college.trim(),
  major: profileForm.major.trim(),
  grade: profileForm.grade.trim(),
  location: profileForm.location.trim(),
  bio: profileForm.bio.trim()
})

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
    return `共 ${item.fileCount} 个文件，点击后可逐层展开目录`
  }
  return item.description || '暂无描述'
}

const formatFileSize = (size) => {
  const numericSize = Number(size || 0)
  if (!numericSize) {
    return '0 B'
  }

  const units = ['B', 'KB', 'MB', 'GB']
  let value = numericSize
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  return `${value >= 100 || unitIndex === 0 ? value.toFixed(0) : value.toFixed(1)} ${units[unitIndex]}`
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
  if (!node) {
    return '暂无内容'
  }
  const directChildrenCount = node.children?.length || 0
  const fileCount = countFolderNodeFiles(node)
  if (directChildrenCount === 0) {
    return '空文件夹'
  }
  return `包含 ${directChildrenCount} 项，累计 ${fileCount} 个文件`
}

const myResourceDisplayList = computed(() => {
  const result = []
  const folderGroupMap = new Map()

  myResourceList.value.forEach(item => {
    if (!(item.uploadType === 'folder' && item.folderName)) {
      result.push({
        ...item,
        displayType: 'file',
        key: `file-${item.id}`
      })
      return
    }

    const groupIdentity = item.batchNo || `${item.folderName}-${item.createTime || ''}`
    const groupKey = `folder-${groupIdentity}`

    if (!folderGroupMap.has(groupKey)) {
      const group = {
        key: groupKey,
        displayType: 'folder',
        folderName: item.folderName,
        batchNo: item.batchNo || '',
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
})

const refreshActiveFolderViewer = () => {
  if (!folderViewerVisible.value || !activeFolderGroupKey.value) {
    return
  }

  const group = myResourceDisplayList.value.find(item => item.key === activeFolderGroupKey.value)
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

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度为 6-20 位'))
  } else {
    if (passwordForm.confirmPassword && passwordFormRef.value) {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }

  if (!/^1\d{10}$/.test(value.trim())) {
    callback(new Error('请输入正确的手机号'))
    return
  }

  callback()
}

const infoRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const profileRules = {
  realName: [
    { max: 30, message: '真实姓名不能超过 30 个字符', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  school: [
    { max: 100, message: '学校不能超过 100 个字符', trigger: 'blur' }
  ],
  college: [
    { max: 100, message: '学院不能超过 100 个字符', trigger: 'blur' }
  ],
  major: [
    { max: 100, message: '专业不能超过 100 个字符', trigger: 'blur' }
  ],
  grade: [
    { max: 30, message: '年级不能超过 30 个字符', trigger: 'blur' }
  ],
  location: [
    { max: 100, message: '所在城市不能超过 100 个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 300, message: '个人简介不能超过 300 个字符', trigger: 'blur' }
  ]
}

const loadUserInfo = async () => {
  try {
    const data = await userAPI.getUserInfo()
    userInfo.value = data
    infoForm.nickname = data.nickname || ''
    infoForm.avatar = data.avatar || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadProfileDetail = async (force = false) => {
  if (profileDetailLoaded.value && !force) {
    return
  }

  profileDetailLoading.value = true
  try {
    const data = await userAPI.getProfileDetail()
    Object.assign(profileForm, normalizeProfileData(data))
    profileDetailLoaded.value = true
  } catch (error) {
    console.error('加载我的资料失败:', error)
  } finally {
    profileDetailLoading.value = false
  }
}

const loadMyResourceList = async () => {
  myResourceLoading.value = true
  try {
    const params = {
      page: myResourceCurrentPage.value,
      size: myResourcePageSize.value
    }

    if (myResourceSearchForm.keyword) {
      params.keyword = myResourceSearchForm.keyword
    }
    if (myResourceSearchForm.fileType) {
      params.fileType = myResourceSearchForm.fileType
    }
    if (myResourceSearchForm.uploadType) {
      params.uploadType = myResourceSearchForm.uploadType
    }

    const data = await resourceAPI.getMyResourceList(params)
    myResourceList.value = data.records || []
    myResourceTotal.value = data.total || 0
    myResourceLoaded.value = true
    refreshActiveFolderViewer()
  } catch (error) {
    console.error('加载我的资源失败:', error)
  } finally {
    myResourceLoading.value = false
  }
}

const handleMenuSelect = async (index) => {
  activeMenu.value = index

  if (index === 'profile') {
    await loadProfileDetail()
  }

  if (index === 'resources' && !myResourceLoaded.value) {
    await loadMyResourceList()
  }
}

const handleAvatarChange = async (file) => {
  const rawFile = file.raw

  if (!rawFile) {
    return
  }

  const isImage = ['image/jpeg', 'image/png', 'image/webp'].includes(rawFile.type)
  if (!isImage) {
    ElMessage.error('头像仅支持 JPG、PNG、WEBP 格式')
    return
  }

  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB')
    return
  }

  const formData = new FormData()
  formData.append('file', rawFile)

  avatarUploading.value = true
  try {
    const avatarUrl = await userAPI.uploadAvatar(formData)
    infoForm.avatar = avatarUrl
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
  } finally {
    avatarUploading.value = false
  }
}

const handleUpdateInfo = async () => {
  if (!infoFormRef.value) {
    return
  }

  await infoFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    infoLoading.value = true
    try {
      await userAPI.updateUserInfo({
        nickname: infoForm.nickname,
        avatar: infoForm.avatar || undefined
      })

      ElMessage.success('修改成功')

      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: infoForm.nickname,
        avatar: infoForm.avatar
      })

      await loadUserInfo()
    } catch (error) {
      console.error('修改失败:', error)
    } finally {
      infoLoading.value = false
    }
  })
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) {
    return
  }

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    passwordLoading.value = true
    try {
      await userAPI.updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      userStore.clearUserInfo()
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      passwordLoading.value = false
    }
  })
}

const handleUpdateProfileDetail = async () => {
  if (!profileFormRef.value) {
    return
  }

  await profileFormRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    profileDetailSaving.value = true
    try {
      await userAPI.updateProfileDetail(trimProfilePayload())
      ElMessage.success('资料保存成功')
      profileDetailLoaded.value = true
    } catch (error) {
      console.error('保存我的资料失败:', error)
    } finally {
      profileDetailSaving.value = false
    }
  })
}

const handleMyResourceSearch = () => {
  myResourceCurrentPage.value = 1
  loadMyResourceList()
}

const handleMyResourceReset = () => {
  myResourceSearchForm.keyword = ''
  myResourceSearchForm.fileType = ''
  myResourceSearchForm.uploadType = ''
  myResourceCurrentPage.value = 1
  loadMyResourceList()
}

const handleResourceDownload = (id) => {
  const url = resourceAPI.downloadResource(id)
  const target = myResourceList.value.find(item => item.id === id)

  if (target) {
    target.downloadCount = Number(target.downloadCount || 0) + 1
  }

  const link = document.createElement('a')
  link.href = url
  link.target = '_blank'
  link.rel = 'noopener noreferrer'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleResourceDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个资源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await resourceAPI.deleteResource(id)
    ElMessage.success('删除成功')
    await loadMyResourceList()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    console.error('删除资源失败:', error)
  }
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

const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

const resetProfileForm = async () => {
  if (profileDetailLoaded.value) {
    await loadProfileDetail(true)
    profileFormRef.value?.clearValidate()
    return
  }

  Object.assign(profileForm, emptyProfileForm())
  profileFormRef.value?.clearValidate()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 用户卡片 ===== */
.user-card {
  margin-bottom: var(--gap-lg);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(212, 230, 245, 0.6);
  box-shadow: 0 14px 28px rgba(176, 206, 230, 0.12);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.72) 0%, rgba(241, 249, 255, 0.6) 100%);
  backdrop-filter: blur(14px);
  overflow: hidden;
}

.user-card :deep(.el-card__body) {
  padding: 0;
}

.user-avatar-section {
  text-align: center;
  padding: 32px 24px 28px;
}

.user-nickname {
  font-size: 24px;
  font-weight: 800;
  margin: 16px 0 8px;
  color: var(--color-title);
}

.user-username {
  font-size: 14px;
  color: var(--color-muted);
  margin: 0 0 12px;
}

.join-time {
  font-size: 13px;
  color: var(--color-muted);
  margin-top: 12px;
}

/* ===== 菜单卡片 ===== */
.menu-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  background: var(--bg-card);
  overflow: hidden;
}

.menu-card .el-menu {
  border: none;
  background: transparent;
}

.menu-card :deep(.el-menu-item) {
  border-radius: var(--radius-sm);
  margin: 4px 8px;
  transition: color 0.25s ease, background 0.25s ease;
}

.menu-card :deep(.el-menu-item:hover) {
  color: #5d93c7;
  background: rgba(220, 238, 251, 0.72);
}

.menu-card :deep(.el-menu-item.is-active) {
  color: #5d93c7;
  background: linear-gradient(135deg, rgba(186, 220, 248, 0.36), rgba(234, 245, 255, 0.96));
  box-shadow: inset 0 0 0 1px rgba(202, 228, 247, 0.85);
}

/* ===== 内容卡片 ===== */
.content-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
  background: var(--bg-card);
  overflow: hidden;
}

.content-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(217, 234, 247, 0.8);
}

.card-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--color-title);
}

/* ===== 头像上传 ===== */
.avatar-upload-section {
  display: flex;
  align-items: center;
  gap: 18px;
}

.avatar-preview-wrap {
  flex-shrink: 0;
}

.avatar-upload-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-tip {
  font-size: 13px;
  color: var(--color-muted);
  margin: 0;
}

/* ===== 我的资料表单 ===== */
.profile-detail-form {
  max-width: 100%;
}

.profile-detail-form :deep(.el-select),
.profile-detail-form :deep(.el-date-editor) {
  width: 100%;
}

.profile-detail-actions {
  display: flex;
  gap: var(--gap-sm);
}

.profile-detail-bio :deep(.el-textarea__inner) {
  min-height: 120px;
}

/* ===== 我的资源面板（无外框） ===== */
.resources-panel-title {
  margin: 0 0 var(--gap-md);
  font-size: 18px;
  font-weight: 800;
  color: var(--color-title);
}

/* ===== 我的资源搜索 ===== */
.my-resource-search {
  margin-bottom: var(--gap-md);
}

/* ===== 资源列表 ===== */
.my-resource-list {
  display: flex;
  flex-direction: column;
  gap: var(--gap-md);
  min-height: 320px;
}

.resource-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 10px 16px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-card);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.resource-row:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(155, 194, 224, 0.16);
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
  color: var(--color-title);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-row-desc {
  margin: 0 0 6px;
  font-size: 14px;
  color: #0b63b3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-row-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--color-body);
}

.meta-item {
  font-size: 12px;
  color: var(--color-body);
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
  border: 1px solid var(--border-color);
  color: #6a7b8f;
  font-size: 11px;
  line-height: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-path-more {
  color: var(--color-primary);
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

.my-resource-pagination {
  justify-content: center;
  margin-top: 24px;
}

/* ===== 文件夹查看器 ===== */
.folder-viewer-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 14px;
  padding: 14px 18px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  flex-wrap: wrap;
}

.folder-viewer-breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  row-gap: 4px;
}

.folder-breadcrumb-separator {
  color: #9aa9b9;
  margin: 0 4px;
}

.folder-current-desc {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  border-radius: var(--radius-sm);
  background: #f7fbff;
  border: 1px solid var(--border-color);
  color: var(--color-body);
  font-size: 13px;
  flex-wrap: wrap;
}

.folder-entry-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.folder-entry {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-card);
}

.folder-entry.clickable {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.folder-entry.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(155, 194, 224, 0.16);
}

.folder-entry-cover {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
}

.folder-entry-main {
  min-width: 0;
  flex: 1;
}

.folder-entry-title {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-title);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-entry-desc {
  margin: 0;
  font-size: 12px;
  color: var(--color-muted);
}

.folder-entry-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* ===== 移动端适配 768px ===== */
@media (max-width: 768px) {
  .user-card {
    border-radius: var(--radius-md);
  }

  .menu-card {
    border-radius: var(--radius-md);
  }

  .content-card {
    border-radius: var(--radius-md);
  }

  .user-avatar-section {
    padding: 24px 18px 22px;
  }

  .avatar-upload-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-detail-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .resource-row {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .resource-row-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .my-resource-search :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }

  .my-resource-search :deep(.el-input),
  .my-resource-search :deep(.el-select) {
    width: 100% !important;
  }

  .folder-viewer-toolbar {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* ===== 移动端适配 480px ===== */
@media (max-width: 480px) {
  .user-card {
    border-radius: var(--radius-md);
  }

  .menu-card {
    border-radius: var(--radius-md);
  }

  .content-card {
    border-radius: var(--radius-md);
  }

  .user-avatar-section {
    padding: 20px 14px 18px;
  }

  .user-nickname {
    font-size: 20px;
  }

  .card-title {
    font-size: 16px;
  }

  .resource-row {
    border-radius: 14px;
    padding: 12px 14px;
  }

  .resource-row:hover {
    transform: none;
  }

  .resource-row-title {
    font-size: 14px;
  }

  .folder-entry {
    padding: 12px 14px;
  }

  .folder-entry-title {
    font-size: 14px;
  }
}
</style>

