import request from '@/utils/request'

export const featuredResourceAPI = {
  getFeaturedResourceList(params) {
    return request.get('/featured-resource/list', { params })
  },

  getAdminFeaturedResourceList(params) {
    return request.get('/featured-resource/admin/list', { params })
  },

  getFeaturedResourceDetail(id) {
    return request.get(`/featured-resource/${id}`)
  },

  addFeaturedResource(data) {
    return request.post('/featured-resource', data)
  },

  updateFeaturedResource(id, data) {
    return request.put(`/featured-resource/${id}`, data)
  },

  deleteFeaturedResource(id) {
    return request.delete(`/featured-resource/${id}`)
  }
}
