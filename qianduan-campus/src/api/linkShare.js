import request from '@/utils/request'

export const linkShareAPI = {
  getLinkList(params) {
    return request.get('/link-share/list', { params })
  },

  getLinkDetail(id) {
    return request.get(`/link-share/${id}`)
  },

  createLink(data) {
    return request.post('/link-share', data)
  },

  updateLink(id, data) {
    return request.put(`/link-share/${id}`, data)
  },

  updateLinkStatus(id, status) {
    return request.patch(`/link-share/${id}/status`, { status })
  },

  deleteLink(id) {
    return request.delete(`/link-share/${id}`)
  }
}
