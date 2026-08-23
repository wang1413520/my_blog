import request from '@/utils/request'

export const postAPI = {
  publishPost(data) {
    return request.post('/post/publish', data)
  },

  getPostList(params) {
    return request.get('/post/list', { params })
  },

  getPostDetail(id) {
    return request.get(`/post/${id}`)
  },

  likePost(id) {
    return request.post(`/post/like/${id}`)
  },

  deletePost(id) {
    return request.delete(`/post/${id}`)
  },

  searchPost(params) {
    return request.get('/post/search', { params })
  }
}
