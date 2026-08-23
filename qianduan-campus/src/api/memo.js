import request from '@/utils/request'

export const memoAPI = {
  getMemoList() {
    return request.get('/memo/list')
  },

  addMemo(data) {
    return request.post('/memo/add', data)
  },

  updateMemo(id, data) {
    return request.put(`/memo/${id}`, data)
  },

  updateMemoStatus(id, data) {
    return request.put(`/memo/${id}/status`, data)
  },

  updateMemoPin(id, data) {
    return request.put(`/memo/${id}/pin`, data)
  },

  deleteMemo(id) {
    return request.delete(`/memo/${id}`)
  }
}
