import request from '@/utils/request'

export const toolboxAPI = {
  getSupportedTypes() {
    return request.get('/toolbox/convert/supported-types')
  },

  convertFile(formData, config = {}) {
    return request.post('/toolbox/convert', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 120000,
      ...config
    })
  },

  getConvertHistory(params) {
    return request.get('/toolbox/convert/history', { params })
  },

  deleteConvertRecord(id) {
    return request.delete(`/toolbox/convert/${id}`)
  }
}
