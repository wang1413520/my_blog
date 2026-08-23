package com.wang.mycampus.dto;

import lombok.Data;

@Data
public class AddCommentDTO {
    private Long postId;
    private String content; // <= 500字
    /**
     * 可选：被直接回复的记录 id。
     * 顶层评论被回复时传 comment.id；楼中楼被回复时传 comment_reply.id。
     * 为空 = 直接评论文章（走原 comment 表）。
     */
    private Long parentId;
    /**
     * 可选：被@的用户ID。不传则默认取被直接回复记录的作者ID。
     */
    private Long replyToUserId;
    /**
     * 内部回填用：新记录自增ID（顶层=comment.id，楼中楼=comment_reply.id），请求方无需传。
     */
    private Long id;
}
