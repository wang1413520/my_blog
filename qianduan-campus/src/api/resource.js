import request from '@/utils/request'

export const resourceAPI = {
  // 上传请求使用更长的超时时间（5分钟），避免大文件上传时超时
  uploadResource(formData) {
    return request.post('/resource/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 300000 // 5分钟
    })
  },

  uploadResourceFolder(formData) {
    return request.post('/resource/upload/folder', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 600000 // 10分钟，文件夹上传可能包含多个文件
    })
  },

  getResourceList(params) {
    return request.get('/resource/list', { params })
  },

  getMyResourceList(params) {
    return request.get('/resource/my/list', { params })
  },

  getResourceDetail(id) {
    return request.get(`/resource/${id}`)
  },

  downloadResource(id) {
    return `/api/resource/download/${id}`
  },

  deleteResource(id) {
    return request.delete(`/resource/${id}`)
  }
}
