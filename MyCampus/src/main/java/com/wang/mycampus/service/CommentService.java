package com.wang.mycampus.service;

import com.wang.mycampus.dto.AddCommentDTO;
import com.wang.mycampus.vo.CommentItemVO;
import com.wang.mycampus.vo.PageVO;

import java.util.List;

public interface CommentService {

    /*
     * 发表评论（顶层走 comment 表，楼中楼走 comment_reply 表，返回新记录ID）
     * */
    Long addComment(Long userId, AddCommentDTO addCommentDTO);

    /*
     * 获取评论列表（分页，顶层带 children 楼中楼）
     * currentUserId 可为 null（未登录），用于判断 isOwner
     * */
    PageVO<CommentItemVO> getCommentList(Long postId, Integer page, Integer size, Long currentUserId);

    /*
     * 删除评论（顶层，同时级联清理楼中楼回复；仅作者或管理员）
     * */
    void deleteComment(Long commentId, Long userId);

    /*
     * 删除楼中楼回复（软删，仅作者或管理员）
     * */
    void deleteCommentReply(Long replyId, Long userId);

    /*
    * 查询评论所有数量
    * */
    Integer selectCommentNumber();

    /*
    * 为顶层评论组装楼中楼 children 与 isOwner（一次 IN 查询带回，供评论列表与帖子详情复用）
    * */
    void fillCommentTree(List<CommentItemVO> topComments, Long postId, Long currentUserId);

}
