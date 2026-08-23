import request from '@/utils/request'

export const noticeAPI = {
  // 前台：获取启用的公告列表（后端按创建时间倒序，最新在前），前端取第一条为最新公告
  getActiveNotices(params) {
    return request.get('/notice/list', { params })
  },

  // 后台：分页列表（含停用）
  getAdminNoticeList(params) {
    return request.get('/notice/admin/list', { params })
  },

  getNoticeDetail(id) {
    return request.get(`/notice/${id}`)
  },

  addNotice(data) {
    return request.post('/notice', data)
  },

  updateNotice(id, data) {
    return request.put(`/notice/${id}`, data)
  },

  deleteNotice(id) {
    return request.delete(`/notice/${id}`)
  }
}
