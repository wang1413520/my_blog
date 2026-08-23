import request from '@/utils/request'

export const adminAPI = {
  getDashboardOverview() {
    return request.get('/admin/dashboard/overview')
  },

  getUserRegisterTrend(params) {
    return request.get('/admin/dashboard/user-register-trend', { params })
  },

  getPostPublishTrend(params) {
    return request.get('/admin/dashboard/post-publish-trend', { params })
  },

  getResourceUploadTrend(params) {
    return request.get('/admin/dashboard/resource-upload-trend', { params })
  },

  getResourceFileTypeDistribution() {
    return request.get('/admin/dashboard/resource-file-type-distribution')
  },

  getResourceDownloadTop(params) {
    return request.get('/admin/dashboard/resource-download-top', { params })
  },

  getFolderUploadStat() {
    return request.get('/admin/dashboard/folder-upload-stat')
  },

  getLatestUploadBatches(params) {
    return request.get('/admin/dashboard/latest-upload-batches', { params })
  }
}
