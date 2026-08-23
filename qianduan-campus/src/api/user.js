import request from '@/utils/request'

export const userAPI = {
  register(data) {
    return request.post('/user/register', data)
  },

  login(data) {
    return request.post('/user/login', data)
  },

  getUserInfo() {
    return request.get('/user/info')
  },

  uploadAvatar(formData) {
    return request.post('/user/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 60000 // 头像上传60秒超时（头像通常较小，1分钟足够）
    })
  },

  updateUserInfo(data) {
    return request.put('/user/update', data)
  },

  getProfileDetail() {
    return request.get('/user/profile/detail')
  },

  updateProfileDetail(data) {
    return request.put('/user/profile/detail', data)
  },

  updatePassword(data) {
    return request.put('/user/password', data)
  }
}
