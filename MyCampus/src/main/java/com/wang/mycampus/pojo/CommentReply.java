package com.wang.mycampus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentReply {
    private Long id;
    private Long postId;
    private Long rootCommentId;   // 指向 comment 表顶层评论 id
    private Long parentId;        // 指向 comment_reply 表记录 id，NULL=直接回复顶层评论
    private Long userId;
    private Long replyToUserId;
    private String content;
    private Integer status;       // 1正常 0已删除
    private LocalDateTime createTime;
}
