import request from '@/utils/request'

export const homeAPI = {
  getHomeStats() {
    return request.get('/home/stats')
  }
}
