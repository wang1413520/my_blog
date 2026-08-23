 <template>
  <div v-loading="pageLoading" class="admin-dashboard">
    <section class="dashboard-hero">
      <div>
        <p class="dashboard-kicker">Admin Dashboard</p>
        <h1 class="dashboard-title">后台数据总览</h1>
        <p class="dashboard-desc">
          聚焦注册、发帖、资料上传和文件夹批次表现，帮助管理员快速判断全站运行情况。
        </p>
      </div>
      <el-button type="primary" @click="loadDashboardData">
        刷新数据
      </el-button>
    </section>

    <section class="overview-grid">
      <el-card
        v-for="item in overviewCards"
        :key="item.key"
        class="overview-card"
        shadow="hover"
      >
        <div class="overview-card-head">
          <div class="overview-card-icon" :class="`overview-card-icon-${item.key}`">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <span class="overview-card-label">{{ item.label }}</span>
        </div>
        <div class="overview-card-value" :class="{ 'value-error': moduleErrors.overview }">
          {{ moduleErrors.overview ? '加载失败' : item.value }}
        </div>
      </el-card>
    </section>

    <section class="chart-grid chart-grid-double">
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>近 7 天用户注册趋势</span>
          </div>
        </template>
        <div ref="registerChartRef" class="chart-body"></div>
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>近 7 天发帖趋势</span>
          </div>
        </template>
        <div ref="postChartRef" class="chart-body"></div>
      </el-card>
    </section>

    <section class="chart-grid chart-grid-double">
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>近 7 天资料上传趋势</span>
          </div>
        </template>
        <div ref="resourceChartRef" class="chart-body"></div>
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>文件类型分布</span>
          </div>
        </template>
        <div ref="fileTypeChartRef" class="chart-body"></div>
      </el-card>
    </section>

    <section class="chart-grid chart-grid-double">
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>热门资料 Top10</span>
          </div>
        </template>
        <div ref="hotResourceChartRef" class="chart-body"></div>
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="chart-card-header">
            <span>文件夹上传成功/失败统计</span>
          </div>
        </template>
        <div ref="folderUploadChartRef" class="chart-body"></div>
      </el-card>
    </section>

    <el-card class="batch-card" shadow="hover">
      <template #header>
        <div class="chart-card-header">
          <span>最新上传批次列表</span>
        </div>
      </template>

      <el-table :data="latestUploadBatches" stripe>
        <el-table-column prop="batchNo" label="批次号" min-width="160" />
        <el-table-column prop="folderName" label="文件夹名" min-width="140" />
        <el-table-column prop="uploaderName" label="上传人" min-width="100" />
        <el-table-column prop="totalCount" label="文件数" min-width="90" />
        <el-table-column prop="successCount" label="成功数" min-width="90" />
        <el-table-column prop="failCount" label="失败数" min-width="90" />
        <el-table-column label="总大小" min-width="110">
          <template #default="{ row }">
            {{ formatFileSize(row.totalSize || 0) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getBatchStatusTagType(row.status)">
              {{ row.statusText || '未知状态' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" min-width="180" />
      </el-table>

      <el-empty v-if="!latestUploadBatches.length" description="暂无上传批次数据" />
    </el-card>
    <el-card class="featured-manager-card" shadow="hover">
      <AdminFeaturedResourceManager />
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import AdminFeaturedResourceManager from '@/components/AdminFeaturedResourceManager.vue'
import { adminAPI } from '@/api/admin'
import { formatFileSize } from '@/utils/format'
import {
  DataAnalysis,
  ChatDotRound,
  Reading,
  Download
} from '@element-plus/icons-vue'

const pageLoading = ref(false)
const moduleErrors = reactive({
  overview: false,
  register: false,
  post: false,
  resource: false,
  fileType: false,
  hotResource: false,
  folderStat: false,
  latestBatch: false
})

const overview = reactive({
  userCount: 0,
  postCount: 0,
  resourceCount: 0,
  totalDownloadCount: 0
})

const registerTrend = ref([])
const postTrend = ref([])
const resourceTrend = ref([])
const fileTypeDistribution = ref([])
const hotResources = ref([])
const folderUploadStat = reactive({
  totalBatchCount: 0,
  successBatchCount: 0,
  partialSuccessBatchCount: 0,
  failedBatchCount: 0
})
const latestUploadBatches = ref([])

const registerChartRef = ref(null)
const postChartRef = ref(null)
const resourceChartRef = ref(null)
const fileTypeChartRef = ref(null)
const hotResourceChartRef = ref(null)
const folderUploadChartRef = ref(null)

const chartInstances = new Map()

const overviewCards = computed(() => {
  return [
    {
      key: 'user',
      label: '用户总数',
      value: overview.userCount,
      icon: DataAnalysis
    },
    {
      key: 'post',
      label: '帖子总数',
      value: overview.postCount,
      icon: ChatDotRound
    },
    {
      key: 'resource',
      label: '资料总数',
      value: overview.resourceCount,
      icon: Reading
    },
    {
      key: 'download',
      label: '下载总次数',
      value: overview.totalDownloadCount,
      icon: Download
    }
  ]
})

const getShortDate = (dateString) => {
  return String(dateString || '').slice(5)
}

const buildLineOption = (title, data, color, area = false) => {
  return {
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: {
      left: 36,
      right: 20,
      top: 30,
      bottom: 28
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(item => getShortDate(item.date)),
      axisLine: {
        lineStyle: { color: '#d6e4f2' }
      },
      axisLabel: { color: '#7a8ca1' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: {
        lineStyle: { color: '#edf3f9' }
      },
      axisLabel: { color: '#7a8ca1' }
    },
    series: [
      {
        name: title,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        data: data.map(item => item.count),
        areaStyle: area ? { color: 'rgba(103, 157, 219, 0.16)' } : undefined,
        lineStyle: { width: 3 }
      }
    ]
  }
}

const buildBarOption = (data, color) => {
  return {
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: {
      left: 42,
      right: 20,
      top: 30,
      bottom: 28
    },
    xAxis: {
      type: 'category',
      data: data.map(item => getShortDate(item.date)),
      axisLine: {
        lineStyle: { color: '#d6e4f2' }
      },
      axisLabel: { color: '#7a8ca1' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: {
        lineStyle: { color: '#edf3f9' }
      },
      axisLabel: { color: '#7a8ca1' }
    },
    series: [
      {
        type: 'bar',
        barWidth: 26,
        data: data.map(item => item.count),
        itemStyle: {
          borderRadius: [10, 10, 0, 0]
        }
      }
    ]
  }
}

const buildPieOption = (data, colorList) => {
  return {
    color: colorList,
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 4,
      icon: 'circle',
      textStyle: { color: '#708297' }
    },
    series: [
      {
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['50%', '44%'],
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 4
        },
        label: {
          color: '#516273'
        },
        data
      }
    ]
  }
}

const buildHorizontalBarOption = (data) => {
  return {
    color: ['#77aee2'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: {
      left: 120,
      right: 20,
      top: 20,
      bottom: 24
    },
    xAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: {
        lineStyle: { color: '#edf3f9' }
      },
      axisLabel: { color: '#7a8ca1' }
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.title),
      axisLabel: {
        color: '#56687a',
        overflow: 'truncate',
        width: 110
      },
      axisLine: { show: false }
    },
    series: [
      {
        type: 'bar',
        data: data.map(item => item.downloadCount),
        barWidth: 18,
        itemStyle: {
          borderRadius: [0, 9, 9, 0]
        }
      }
    ]
  }
}

const ensureChart = (key, element) => {
  if (!element) {
    return null
  }

  const existing = chartInstances.get(key)
  if (existing) {
    return existing
  }

  const instance = echarts.init(element)
  chartInstances.set(key, instance)
  return instance
}

const renderCharts = () => {
  ensureChart('register', registerChartRef.value)?.setOption(
    buildLineOption('注册人数', registerTrend.value, '#6ca7e0')
  )

  ensureChart('post', postChartRef.value)?.setOption(
    buildBarOption(postTrend.value, '#8bc38d')
  )

  ensureChart('resource', resourceChartRef.value)?.setOption(
    buildLineOption('上传数', resourceTrend.value, '#f1a95c', true)
  )

  ensureChart('fileType', fileTypeChartRef.value)?.setOption(
    buildPieOption(
      fileTypeDistribution.value.map(item => ({
        name: String(item.fileType || '').toUpperCase(),
        value: item.count
      })),
      ['#6ca7e0', '#f0ba61', '#8bc38d', '#ef8f8f']
    )
  )

  ensureChart('hotResource', hotResourceChartRef.value)?.setOption(
    buildHorizontalBarOption(hotResources.value)
  )

  ensureChart('folderUpload', folderUploadChartRef.value)?.setOption(
    buildPieOption(
      [
        { name: '全部成功', value: folderUploadStat.successBatchCount },
        { name: '部分成功', value: folderUploadStat.partialSuccessBatchCount },
        { name: '全部失败', value: folderUploadStat.failedBatchCount }
      ],
      ['#7ab97d', '#f0ba61', '#ef8f8f']
    )
  )
}

const resizeCharts = () => {
  chartInstances.forEach(instance => instance.resize())
}

const getBatchStatusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  if (status === 3) return 'danger'
  return 'info'
}

const loadDashboardData = async () => {
  pageLoading.value = true
  Object.keys(moduleErrors).forEach(key => { moduleErrors[key] = false })

  const results = await Promise.allSettled([
    adminAPI.getDashboardOverview(),
    adminAPI.getUserRegisterTrend({ days: 7 }),
    adminAPI.getPostPublishTrend({ days: 7 }),
    adminAPI.getResourceUploadTrend({ days: 7 }),
    adminAPI.getResourceFileTypeDistribution(),
    adminAPI.getResourceDownloadTop({ limit: 10 }),
    adminAPI.getFolderUploadStat(),
    adminAPI.getLatestUploadBatches({ limit: 10 })
  ])

  const [
    overviewResult,
    registerResult,
    postResult,
    resourceResult,
    fileTypeResult,
    hotResourceResult,
    folderStatResult,
    latestBatchResult
  ] = results

  if (overviewResult.status === 'fulfilled') {
    Object.assign(overview, {
      userCount: overviewResult.value.userCount || 0,
      postCount: overviewResult.value.postCount || 0,
      resourceCount: overviewResult.value.resourceCount || 0,
      totalDownloadCount: overviewResult.value.totalDownloadCount || 0
    })
  } else {
    moduleErrors.overview = true
  }

  if (registerResult.status === 'fulfilled') {
    registerTrend.value = registerResult.value || []
  } else {
    registerTrend.value = []
    moduleErrors.register = true
  }

  if (postResult.status === 'fulfilled') {
    postTrend.value = postResult.value || []
  } else {
    postTrend.value = []
    moduleErrors.post = true
  }

  if (resourceResult.status === 'fulfilled') {
    resourceTrend.value = resourceResult.value || []
  } else {
    resourceTrend.value = []
    moduleErrors.resource = true
  }

  if (fileTypeResult.status === 'fulfilled') {
    fileTypeDistribution.value = fileTypeResult.value || []
  } else {
    fileTypeDistribution.value = []
    moduleErrors.fileType = true
  }

  if (hotResourceResult.status === 'fulfilled') {
    hotResources.value = hotResourceResult.value || []
  } else {
    hotResources.value = []
    moduleErrors.hotResource = true
  }

  if (folderStatResult.status === 'fulfilled') {
    Object.assign(folderUploadStat, {
      totalBatchCount: folderStatResult.value.totalBatchCount || 0,
      successBatchCount: folderStatResult.value.successBatchCount || 0,
      partialSuccessBatchCount: folderStatResult.value.partialSuccessBatchCount || 0,
      failedBatchCount: folderStatResult.value.failedBatchCount || 0
    })
  } else {
    Object.assign(folderUploadStat, {
      totalBatchCount: 0,
      successBatchCount: 0,
      partialSuccessBatchCount: 0,
      failedBatchCount: 0
    })
    moduleErrors.folderStat = true
  }

  if (latestBatchResult.status === 'fulfilled') {
    latestUploadBatches.value = latestBatchResult.value || []
  } else {
    latestUploadBatches.value = []
    moduleErrors.latestBatch = true
  }

  pageLoading.value = false
  await nextTick()
  renderCharts()

  const failedCount = Object.values(moduleErrors).filter(Boolean).length
  if (failedCount > 0) {
    ElMessage.warning(`后台首页部分数据加载失败（${failedCount} 个模块），图表可能不完整`)
  }
}

onMounted(async () => {
  await loadDashboardData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  chartInstances.forEach(instance => instance.dispose())
  chartInstances.clear()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 28px;
  background:
    radial-gradient(circle at 85% 18%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0) 20%),
    linear-gradient(135deg, rgba(243, 250, 255, 0.98), rgba(220, 235, 250, 0.95));
  box-shadow: 0 18px 34px rgba(158, 193, 223, 0.16);
  border: 1px solid rgba(210, 228, 244, 0.86);
}

.dashboard-kicker {
  margin: 0 0 10px;
  color: #79a7d2;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.dashboard-title {
  margin: 0 0 12px;
  font-size: 34px;
  line-height: 1.2;
  color: #203142;
}

.dashboard-desc {
  margin: 0;
  max-width: 680px;
  color: #688098;
  font-size: 15px;
  line-height: 1.8;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.overview-card {
  border-radius: 22px;
  border: 1px solid rgba(221, 234, 246, 0.88);
}

.overview-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.overview-card-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 20px;
}

.overview-card-icon-user {
  background: rgba(112, 178, 233, 0.16);
  color: #5f9fda;
}

.overview-card-icon-post {
  background: rgba(137, 194, 140, 0.16);
  color: #6db172;
}

.overview-card-icon-resource {
  background: rgba(240, 182, 98, 0.18);
  color: #dc9b3b;
}

.overview-card-icon-download {
  background: rgba(242, 144, 144, 0.16);
  color: #e07f7f;
}

.overview-card-label {
  color: #72879b;
  font-size: 14px;
  font-weight: 600;
}

.overview-card-value {
  margin-top: 18px;
  font-size: 34px;
  font-weight: 800;
  color: #203142;
}

.overview-card-value.value-error {
  font-size: 16px;
  color: #e07f7f;
  font-weight: 600;
}

.chart-grid {
  display: grid;
  gap: 20px;
}

.chart-grid-double {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.chart-card,
.batch-card {
  border-radius: 24px;
  border: 1px solid rgba(221, 234, 246, 0.88);
}

.chart-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #2d4256;
  font-weight: 700;
}

.chart-body {
  height: 320px;
}

:deep(.batch-card .el-table) {
  --el-table-header-bg-color: #f7fbff;
  --el-table-border-color: #ebf2f8;
}

@media (max-width: 1100px) {
  .overview-grid,
  .chart-grid-double {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-hero {
    padding: 22px 20px;
    flex-direction: column;
  }

  .overview-grid,
  .chart-grid-double {
    grid-template-columns: 1fr;
  }

  .chart-body {
    height: 280px;
  }
}

@media (max-width: 480px) {
  .dashboard-hero {
    padding: 18px 16px;
    gap: 14px;
  }

  .dashboard-title {
    font-size: 26px;
  }

  .dashboard-desc {
    font-size: 14px;
  }

  /* 4 个统计卡两列紧凑排列 */
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .overview-card-icon {
    width: 38px;
    height: 38px;
    border-radius: 12px;
    font-size: 18px;
  }

  .overview-card-value {
    margin-top: 14px;
    font-size: 28px;
  }

  .chart-body {
    height: 240px;
  }
}
</style>
