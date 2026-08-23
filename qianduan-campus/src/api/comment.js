import request from '@/utils/request'

export const commentAPI = {
  // data 支持：{ postId, content, parentId?, replyToUserId? }
  // parentId 有值 = 楼中楼回复（comment_reply.id），为空 = 顶层评论
  addComment(data) {
    return request.post('/comment/add', data)
  },

  // 返回 records，每项顶层评论含 children 数组（楼中楼）
  getCommentList(params) {
    return request.get('/comment/list', { params })
  },

  // 删除顶层评论
  deleteComment(id) {
    return request.delete(`/comment/${id}`)
  },

  // 删除楼中楼回复
  deleteCommentReply(id) {
    return request.delete(`/comment/reply/${id}`)
  }
}
